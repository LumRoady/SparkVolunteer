package com.spark.volunteer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @InjectMocks
    private RedisCacheService redisCacheService;

    @Test
    void cacheTask_success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Object task = new Object();
        redisCacheService.cacheTask(task, 1L);

        verify(valueOperations).set("task:cache:1", task, 5L, TimeUnit.MINUTES);
    }

    @Test
    void getCachedTask_success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Object cachedTask = new Object();
        when(valueOperations.get("task:cache:1")).thenReturn(cachedTask);

        Object result = redisCacheService.getCachedTask(1L);

        assertNotNull(result);
        assertEquals(cachedTask, result);
        verify(valueOperations).get("task:cache:1");
    }

    @Test
    void getCachedTask_notFound_returnsNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("task:cache:999")).thenReturn(null);

        Object result = redisCacheService.getCachedTask(999L);

        assertNull(result);
    }

    @Test
    void evictTaskCache_success() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        redisCacheService.evictTaskCache(1L);

        verify(redisTemplate).delete("task:cache:1");
        verify(zSetOperations).remove("task:pending:sorted", "1");
    }

    @Test
    void addPendingTask_success() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        long createTime = System.currentTimeMillis();

        redisCacheService.addPendingTask(1L, createTime);

        verify(zSetOperations).add("task:pending:sorted", "1", createTime);
    }

    @Test
    void removePendingTask_success() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        redisCacheService.removePendingTask(1L);

        verify(zSetOperations).remove("task:pending:sorted", "1");
    }
}
