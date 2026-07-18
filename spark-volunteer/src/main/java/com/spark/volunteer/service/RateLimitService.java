package com.spark.volunteer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 分布式限流服务
 * 使用 Redis INCR + EXPIRE 实现滑动窗口限流
 * 替代原内存版 ConcurrentHashMap 限流，支持多实例部署
 */
@Service
public class RateLimitService {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final long WINDOW_SECONDS = 60;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查指定客户端 IP 是否被限流
     * @param clientIp 客户端 IP 地址
     * @return true 表示已被限流，应拒绝请求
     */
    public boolean isRateLimited(String clientIp) {
        String key = RATE_LIMIT_PREFIX + clientIp;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            // 第一次请求，设置窗口过期时间
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        return count != null && count > MAX_REQUESTS_PER_MINUTE;
    }
}
