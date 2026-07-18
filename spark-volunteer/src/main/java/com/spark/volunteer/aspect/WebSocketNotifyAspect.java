/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.aspect;

import com.spark.volunteer.config.WebSocketMessageBridge;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket 通知切面
 * 在任务状态变更方法成功返回后，通过 Redis Pub/Sub 广播通知
 *
 * 单实例部署：Redis Pub/Sub 仍正常工作（自己发自己收）
 * 多实例部署：消息通过 Redis 跨实例传递，所有节点的本地连接都能收到
 */
@Aspect
@Component
public class WebSocketNotifyAspect {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotifyAspect.class);

    @Autowired
    private WebSocketMessageBridge messageBridge;

    /**
     * 任务创建后广播通知
     */
    @AfterReturning(
        pointcut = "execution(* com.spark.volunteer.controller.TaskController.createTask(..))",
        returning = "result"
    )
    public void onTaskCreated(Object result) {
        broadcastIfPresent(result, "new_task");
    }

    /**
     * 任务状态变更后广播通知（接单/完成/取消）
     */
    @AfterReturning(
        pointcut = "execution(* com.spark.volunteer.controller.TaskController.acceptTask(..)) || " +
                   "execution(* com.spark.volunteer.controller.TaskController.completeTask(..)) || " +
                   "execution(* com.spark.volunteer.controller.TaskController.cancelTask(..))",
        returning = "result"
    )
    public void onTaskStatusChanged(Object result) {
        broadcastIfPresent(result, "task_status_changed");
    }

    /**
     * 提取 Result.data 并通过 Redis 桥接器广播
     */
    @SuppressWarnings("unchecked")
    private void broadcastIfPresent(Object result, String eventType) {
        try {
            if (result instanceof com.spark.volunteer.common.Result) {
                com.spark.volunteer.common.Result<?> r = (com.spark.volunteer.common.Result<?>) result;
                if (r.getCode() == 200 && r.getData() != null) {
                    Map<String, Object> message = new HashMap<>();
                    message.put("type", eventType);
                    message.put("data", r.getData());
                    message.put("timestamp", System.currentTimeMillis());
                    // 通过 Redis Pub/Sub 广播，支持多实例部署
                    messageBridge.broadcast(message);
                }
            }
        } catch (Exception e) {
            logger.error("WebSocket通知广播失败: eventType={}", eventType, e);
        }
    }
}
