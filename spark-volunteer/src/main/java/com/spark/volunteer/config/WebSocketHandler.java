/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * WebSocket 消息处理器
 * 负责管理客户端连接、心跳检测和消息广播
 *
 * 本类是系统实时通知功能的核心，支持：
 * - 多客户端并发连接管理
 * - 心跳保活与超时断开
 * - 任务创建/状态变更的实时广播
 */
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    private static final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private static final Map<WebSocketSession, Long> lastHeartbeatTime = new ConcurrentHashMap<>();
    /** session → userId 映射，用于精准推送 */
    private static final Map<WebSocketSession, Long> sessionUserMap = new ConcurrentHashMap<>();
    /** userId → session 反向映射 */
    private static final Map<Long, WebSocketSession> userSessionMap = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ws-heartbeat");
        t.setDaemon(true);
        return t;
    });
    private static final long HEARTBEAT_INTERVAL = 30; // 30秒
    private static final long HEARTBEAT_TIMEOUT = 60; // 60秒

    static {
        // 启动心跳检测任务
        scheduler.scheduleAtFixedRate(() -> {
            long currentTime = System.currentTimeMillis();
            for (WebSocketSession session : sessions) {
                if (!session.isOpen()) {
                    sessions.remove(session);
                    lastHeartbeatTime.remove(session);
                    continue;
                }
                Long lastTime = lastHeartbeatTime.get(session);
                if (lastTime == null || (currentTime - lastTime) > HEARTBEAT_TIMEOUT * 1000) {
                    // 超时，关闭连接
                    try {
                        session.close(CloseStatus.SESSION_NOT_RELIABLE);
                        sessions.remove(session);
                        lastHeartbeatTime.remove(session);
                        logger.info("WebSocket连接超时，已关闭");
                    } catch (IOException e) {
                        logger.error("关闭超时WebSocket连接失败", e);
                    }
                } else if ((currentTime - lastTime) > HEARTBEAT_INTERVAL * 1000) {
                    // 发送心跳
                    try {
                        synchronized (session) {
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage("{\"type\":\"PING\"}"));
                            }
                        }
                    } catch (IOException e) {
                        logger.error("发送WebSocket心跳失败", e);
                        try {
                            session.close(CloseStatus.SESSION_NOT_RELIABLE);
                            sessions.remove(session);
                            lastHeartbeatTime.remove(session);
                        } catch (IOException ex) {
                            logger.error("关闭异常WebSocket连接失败", ex);
                        }
                    }
                }
            }
        }, HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        lastHeartbeatTime.put(session, System.currentTimeMillis());
        logger.info("WebSocket连接建立，当前连接数：{}", sessions.size());

        // 发送欢迎消息
        Map<String, Object> welcomeMessage = new HashMap<>();
        welcomeMessage.put("type", "welcome");
        welcomeMessage.put("message", "WebSocket连接成功，实时通知已启用");
        synchronized (session) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(welcomeMessage)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.debug("收到WebSocket消息：{}", payload);

        // 心跳响应：解析 JSON 判断 type 字段
        try {
            Map<String, Object> heartMsg = objectMapper.readValue(payload, Map.class);
            if ("PONG".equals(heartMsg.get("type"))) {
                lastHeartbeatTime.put(session, System.currentTimeMillis());
                return;
            }
        } catch (Exception ignore) {
            // 非JSON，继续后续处理
        }

        // 用户注册：客户端连上后发送 {"type":"REGISTER","userId":4}
        try {
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            if ("REGISTER".equals(msg.get("type"))) {
                Long userId = Long.valueOf(msg.get("userId").toString());
                sessionUserMap.put(session, userId);
                userSessionMap.put(userId, session);
                logger.info("WebSocket用户注册: userId={}, 当前已注册: {} 人", userId, userSessionMap.size());
            }
        } catch (Exception ignore) {
            // 非JSON消息或格式不对，忽略
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        lastHeartbeatTime.remove(session);
        Long userId = sessionUserMap.remove(session);
        if (userId != null) {
            userSessionMap.remove(userId);
            logger.info("WebSocket连接关闭: userId={}, 当前连接数：{}", userId, sessions.size());
        } else {
            logger.info("WebSocket连接关闭，当前连接数：{}", sessions.size());
        }
    }

    /**
     * 向所有连接的客户端广播消息
     */
    public static void broadcastMessage(Object message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    synchronized (session) {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(json));
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("WebSocket广播消息失败", e);
        }
    }

    /**
     * 【核心】向指定用户列表精准推送消息
     * @param userIds  目标用户ID列表
     * @param message  消息对象
     */
    public static void broadcastToUsers(java.util.List<Long> userIds, Object message) {
        if (userIds == null || userIds.isEmpty()) return;
        try {
            String json = objectMapper.writeValueAsString(message);
            int sent = 0;
            for (Long uid : userIds) {
                WebSocketSession session = userSessionMap.get(uid);
                if (session != null && session.isOpen()) {
                    synchronized (session) {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(json));
                            sent++;
                        }
                    }
                }
            }
            logger.info("精准推送: {}/{} 人在线, 已发送", sent, userIds.size());
        } catch (IOException e) {
            logger.error("精准推送失败", e);
        }
    }

    /**
     * 将消息对象序列化为 JSON 字符串（供 Redis 桥接器使用）
     */
    public static String toJson(Object message) throws com.fasterxml.jackson.core.JsonProcessingException {
        return objectMapper.writeValueAsString(message);
    }

    /**
     * 向本地实例的所有 WebSocket 连接推送原始 JSON（由 Redis 订阅者调用）
     */
    public static void broadcastLocal(String json) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(json));
                        }
                    }
                } catch (IOException e) {
                    logger.error("本地广播推送失败", e);
                }
            }
        }
    }

    /**
     * 向本地实例的指定用户推送原始 JSON（由 Redis 订阅者调用）
     */
    public static void broadcastToLocalUsers(java.util.List<Long> userIds, String json) {
        int sent = 0;
        for (Long uid : userIds) {
            WebSocketSession session = userSessionMap.get(uid);
            if (session != null && session.isOpen()) {
                try {
                    synchronized (session) {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(json));
                            sent++;
                        }
                    }
                } catch (IOException e) {
                    logger.error("本地精准推送失败, userId={}", uid, e);
                }
            }
        }
        logger.debug("Redis桥接本地推送: {}/{} 人在线", sent, userIds.size());
    }

    /**
     * 获取当前连接数
     */
    public static int getSessionCount() {
        return sessions.size();
    }
}
