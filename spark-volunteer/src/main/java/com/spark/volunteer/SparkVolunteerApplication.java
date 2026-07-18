package com.spark.volunteer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.actuate.autoconfigure.redis.RedisHealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.redis.RedisReactiveHealthContributorAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

// 排除默认 Redis 健康检查（Windows 上 INFO 命令返回路径含反斜杠-U 会触发解析异常）
// 由 EmbeddedRedisHealthIndicator（ping 方式）接管 Redis 健康检查
@SpringBootApplication(exclude = {
    RedisHealthContributorAutoConfiguration.class,
    RedisReactiveHealthContributorAutoConfiguration.class
})
@EnableCaching
@EnableScheduling
public class SparkVolunteerApplication {

    private static final Logger logger = LoggerFactory.getLogger(SparkVolunteerApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SparkVolunteerApplication.class, args);

        String port = context.getEnvironment().getProperty("server.port", "8084");
        String address = context.getEnvironment().getProperty("server.address", "localhost");

        logger.info("=========================================");
        logger.info("        星火志愿服务平台 API 启动完成");
        logger.info("=========================================");
        logger.info("HTTP服务运行在: http://{}:{}", address, port);
        logger.info("WebSocket服务运行在: ws://{}:{}/ws", address, port);
        logger.info("当前激活的Profile: {}",
            String.join(", ", context.getEnvironment().getActiveProfiles()));
        logger.info("");
        logger.info("核心API接口:");
        logger.info("  GET  /api/hello                    - 健康检查");
        logger.info("  POST /api/auth/login               - 用户登录");
        logger.info("  POST /api/auth/register            - 用户注册");
        logger.info("  GET  /api/task/list                - 获取任务列表");
        logger.info("  POST /api/task/create              - 创建任务");
        logger.info("  POST /api/task/accept/{taskId}     - 志愿者接单");
        logger.info("  POST /api/task/complete/{taskId}   - 完成任务");
        logger.info("  POST /api/task/cancel/{taskId}     - 取消任务");
        logger.info("  POST /api/emergency/button/{type}  - 硬件按钮触发");
        logger.info("=========================================");
    }

}
