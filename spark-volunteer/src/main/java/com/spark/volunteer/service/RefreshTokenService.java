package com.spark.volunteer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Refresh Token 服务
 * 基于 Redis 存储刷新令牌，支持令牌轮换（使用后旧令牌立即失效）
 */
@Service
public class RefreshTokenService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final long REFRESH_TOKEN_TTL_DAYS = 7;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 为指定用户创建刷新令牌
     * @param userId 用户ID
     * @return 新生成的刷新令牌
     */
    public String createRefreshToken(Long userId) {
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, userId, REFRESH_TOKEN_TTL_DAYS, TimeUnit.DAYS);
        return refreshToken;
    }

    /**
     * 验证刷新令牌有效性
     * @param refreshToken 刷新令牌
     * @return 有效时返回关联的用户ID，无效返回 null
     */
    public Long validateRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        Object userId = redisTemplate.opsForValue().get(key);
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    /**
     * 删除刷新令牌（令牌轮换时使用）
     * @param refreshToken 要删除的刷新令牌
     */
    public void deleteRefreshToken(String refreshToken) {
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + refreshToken);
    }
}
