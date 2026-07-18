/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.config;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Redis Pub/Sub 消息桥接器
 * 解决多实例部署时 WebSocket 推送不可达的问题：
 * - 任何实例发布消息到 Redis channel
 * - 所有实例订阅 channel 并向本地 WebSocket 连接推送
 * - 替代直接调用 WebSocketHandler 的进程内广播
 */
@Component
public class WebSocketMessageBridge {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketMessageBridge.class);

    /** Redis channel 名称 */
    public static final String WS_BROADCAST_CHANNEL = "ws:broadcast";
    public static final String WS_USER_CHANNEL = "ws:user";

    private final RedisTemplate<String, Object> redisTemplate;

    public WebSocketMessageBridge(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 通过 Redis 广播消息给所有连接的客户端
     * 调用方（任何实例）→ 发布到 Redis → 所有实例订阅 → 本地 WebSocket 推送
     */
    public void broadcast(Object message) {
        try {
            String json = com.spark.volunteer.config.WebSocketHandler.toJson(message);
            redisTemplate.convertAndSend(WS_BROADCAST_CHANNEL, json);
        } catch (Exception e) {
            logger.error("Redis广播消息发布失败，降级为本地推送", e);
            WebSocketHandler.broadcastMessage(message);
        }
    }

    /**
     * 通过 Redis 向指定用户精准推送
     */
    public void sendToUsers(List<Long> userIds, Object message) {
        if (userIds == null || userIds.isEmpty()) return;
        try {
            String json = com.spark.volunteer.config.WebSocketHandler.toJson(message);
            // 将 userIds 和消息打包发送
            java.util.Map<String, Object> payload = new java.util.HashMap<>();
            payload.put("userIds", userIds);
            payload.put("message", json);
            redisTemplate.convertAndSend(WS_USER_CHANNEL, payload);
        } catch (Exception e) {
            logger.error("Redis精准推送发布失败，降级为本地推送", e);
            WebSocketHandler.broadcastToUsers(userIds, message);
        }
    }

    /**
     * Redis 订阅者：收到广播消息后推送给本地 WebSocket 连接
     */
    @Component
    public static class BroadcastSubscriber implements MessageListener {

        private static final Logger logger = LoggerFactory.getLogger(BroadcastSubscriber.class);

        @Override
        public void onMessage(Message message, byte[] pattern) {
            try {
                String json = new String(message.getBody());
                WebSocketHandler.broadcastLocal(json);
            } catch (Exception e) {
                logger.error("处理Redis广播消息失败", e);
            }
        }
    }

    /**
     * Redis 订阅者：收到精准推送消息后推送给本地 WebSocket 连接
     */
    @Component
    public static class UserMessageSubscriber implements MessageListener {

        private static final Logger logger = LoggerFactory.getLogger(UserMessageSubscriber.class);
        private static final com.fasterxml.jackson.databind.ObjectMapper objectMapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        @Override
        public void onMessage(Message message, byte[] pattern) {
            try {
                String json = new String(message.getBody());
                java.util.Map<String, Object> payload = objectMapper.readValue(json, java.util.Map.class);
                @SuppressWarnings("unchecked")
                java.util.List<Number> userIds = (java.util.List<Number>) payload.get("userIds");
                String msgJson = (String) payload.get("message");
                if (userIds != null && msgJson != null) {
                    List<Long> uidList = new java.util.ArrayList<>();
                    for (Number n : userIds) {
                        uidList.add(n.longValue());
                    }
                    WebSocketHandler.broadcastToLocalUsers(uidList, msgJson);
                }
            } catch (Exception e) {
                logger.error("处理Redis精准推送消息失败", e);
            }
        }
    }
}
