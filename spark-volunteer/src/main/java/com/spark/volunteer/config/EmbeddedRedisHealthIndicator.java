/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 */

package com.spark.volunteer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * 自定义 Redis 健康指示器（仅 dev 环境生效）
 *
 * 解决问题：Windows 上内嵌 Redis 的 INFO 命令返回路径含反斜杠（如 C:\\Users\\...），
 * Spring 默认的 RedisHealthIndicator 将其解析为 Java Properties 时，
 * 反斜杠-U 被误认为 Unicode 转义，触发解析异常，导致健康检查失败（503）。
 *
 * 本类通过 ping 命令判断 Redis 可用性，绕过 INFO 解析问题。
 */
@Component
@Profile("dev")
public class EmbeddedRedisHealthIndicator implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedRedisHealthIndicator.class);

    private final RedisConnectionFactory connectionFactory;

    public EmbeddedRedisHealthIndicator(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Health health() {
        try (RedisConnection connection = connectionFactory.getConnection()) {
            String result = connection.ping();
            if ("PONG".equalsIgnoreCase(result)) {
                return Health.up()
                        .withDetail("redis", "embedded")
                        .withDetail("ping", "PONG")
                        .build();
            } else {
                return Health.down()
                        .withDetail("ping", result)
                        .build();
            }
        } catch (Exception e) {
            logger.warn("Redis 健康检查失败: {}", e.getMessage());
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
}
