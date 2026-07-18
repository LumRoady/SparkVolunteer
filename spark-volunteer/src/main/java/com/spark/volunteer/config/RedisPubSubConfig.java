/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.config;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis Pub/Sub 配置
 * 注册 WebSocket 广播和精准推送的订阅者
 * 仅在 spring.redis.pubsub.enabled=true 时启用（默认禁用，生产环境显式开启）
 */
@Configuration
@ConditionalOnProperty(name = "spring.redis.pubsub.enabled", havingValue = "true", matchIfMissing = false)
public class RedisPubSubConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            WebSocketMessageBridge.BroadcastSubscriber broadcastSubscriber,
            WebSocketMessageBridge.UserMessageSubscriber userMessageSubscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 订阅广播 channel
        container.addMessageListener(broadcastSubscriber,
                new org.springframework.data.redis.listener.ChannelTopic(WebSocketMessageBridge.WS_BROADCAST_CHANNEL));
        // 订阅精准推送 channel
        container.addMessageListener(userMessageSubscriber,
                new org.springframework.data.redis.listener.ChannelTopic(WebSocketMessageBridge.WS_USER_CHANNEL));
        return container;
    }
}
