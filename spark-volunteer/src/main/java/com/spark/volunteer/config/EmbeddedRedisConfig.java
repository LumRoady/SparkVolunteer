/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 */

package com.spark.volunteer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;

/**
 * 内嵌 Redis 配置（仅 dev 环境生效）
 *
 * 启动 Spring Boot 时自动拉起一个本地 Redis 实例，
 * 应用关闭时自动停止，无需手动安装/启动 Redis。
 *
 * 生产环境（prod profile）不加载此配置，需连接外部 Redis 服务。
 */
@Configuration
@Profile("dev")
public class EmbeddedRedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedRedisConfig.class);

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void start() throws IOException {
        // 检测端口是否已被占用（例如系统已安装并启动了 Redis）
        if (isPortInUse(redisPort)) {
            logger.info("端口 {} 已被占用，跳过内嵌 Redis 启动（使用系统已运行的 Redis）", redisPort);
            return;
        }

        logger.info("正在启动内嵌 Redis，端口: {}...", redisPort);
        redisServer = new RedisServer(redisPort);
        redisServer.start();
        logger.info("内嵌 Redis 启动成功，端口: {}", redisPort);
    }

    @PreDestroy
    public void stop() throws IOException {
        if (redisServer != null && redisServer.isActive()) {
            logger.info("正在停止内嵌 Redis...");
            redisServer.stop();
            logger.info("内嵌 Redis 已停止");
        }
    }

    /**
     * 检测指定端口是否正在被使用
     */
    private boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
