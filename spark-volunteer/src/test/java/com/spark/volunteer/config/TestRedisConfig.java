package com.spark.volunteer.config;

import com.spark.volunteer.service.RedisCacheService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;

import static org.mockito.Mockito.*;

/**
 * 测试环境配置类
 * 在测试 profile 下提供 Redis 相关 Bean 的 Mock 实现，
 * 避免因排除 RedisAutoConfiguration 导致 ApplicationContext 加载失败
 */
@TestConfiguration
public class TestRedisConfig {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = mock(RedisTemplate.class);

        // ValueOperations
        ValueOperations<String, Object> valueOps = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(valueOps);
        doNothing().when(valueOps).set(anyString(), any(), anyLong(), any(java.util.concurrent.TimeUnit.class));
        doNothing().when(valueOps).set(anyString(), any());
        when(valueOps.setIfAbsent(anyString(), any(), anyLong(), any(java.util.concurrent.TimeUnit.class)))
                .thenReturn(true);
        when(valueOps.get(anyString())).thenReturn(null);

        // ZSetOperations
        ZSetOperations<String, Object> zsetOps = mock(ZSetOperations.class);
        when(template.opsForZSet()).thenReturn(zsetOps);
        when(zsetOps.add(anyString(), any(), anyDouble())).thenReturn(true);

        // HashOperations
        HashOperations<String, Object, Object> hashOps = mock(HashOperations.class);
        when(template.opsForHash()).thenReturn(hashOps);

        // SetOperations
        SetOperations<String, Object> setOps = mock(SetOperations.class);
        when(template.opsForSet()).thenReturn(setOps);

        // ListOperations
        ListOperations<String, Object> listOps = mock(ListOperations.class);
        when(template.opsForList()).thenReturn(listOps);

        // convertAndSend 不抛异常
        doNothing().when(template).convertAndSend(anyString(), any());
        // delete 返回 true
        when(template.delete(anyString())).thenReturn(true);

        return template;
    }

    @Bean
    @Primary
    public RedisCacheService redisCacheService(RedisTemplate<String, Object> redisTemplate) {
        return new RedisCacheService(redisTemplate);
    }

    @Bean
    @Primary
    public WebSocketMessageBridge webSocketMessageBridge(RedisTemplate<String, Object> redisTemplate) {
        return new WebSocketMessageBridge(redisTemplate);
    }
}
