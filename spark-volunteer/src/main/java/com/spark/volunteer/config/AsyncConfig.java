/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置
 * 替代 Spring Boot 默认的 SimpleAsyncTaskExecutor（无界创建线程），
 * 防止高并发下 OOM
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);                        // 核心线程数
        executor.setMaxPoolSize(20);                        // 最大线程数
        executor.setQueueCapacity(100);                     // 队列容量
        executor.setKeepAliveSeconds(60);                   // 空闲线程存活时间
        executor.setThreadNamePrefix("async-");             // 线程名前缀
        executor.setRejectedExecutionHandler((r, e) -> {    // 拒绝策略：记日志 + 丢弃最旧任务
            logger.warn("异步任务队列已满({})，丢弃最旧任务", e.getQueue().size());
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        });
        executor.setWaitForTasksToCompleteOnShutdown(true); // 优雅关闭
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        logger.info("异步线程池已初始化: core=4, max=20, queue=100");
        return executor;
    }
}
