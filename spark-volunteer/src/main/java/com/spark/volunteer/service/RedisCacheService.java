package com.spark.volunteer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 * 封装任务缓存、登录限流、签到去重等操作
 * 基于 Spring Data Redis + RedisTemplate 实现
 */
@Service
public class RedisCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheService.class);

    // ==================== Key 前缀常量 ====================

    private static final String TASK_CACHE_PREFIX = "task:cache:";
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final String CHECKIN_PREFIX = "checkin:";
    private static final String PENDING_TASKS_KEY = "task:pending:sorted";
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";
    private static final String VERIFY_CODE_PREFIX = "verify:code:";

    // Task 缓存过期时间：5分钟
    private static final long TASK_CACHE_TTL_MINUTES = 5;
    // 登录失败锁定时间：15分钟
    private static final long LOGIN_LOCK_MINUTES = 15;
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== 任务缓存 ====================

    /**
     * 缓存任务详情
     * @param task   任务对象
     * @param taskId 任务ID
     */
    public void cacheTask(Object task, Long taskId) {
        String key = TASK_CACHE_PREFIX + taskId;
        redisTemplate.opsForValue().set(key, task, TASK_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        logger.debug("任务已缓存到Redis: taskId={}", taskId);
    }

    /**
     * 获取缓存的任务详情
     * @param taskId 任务ID
     * @return 缓存的任务对象，未命中返回null
     */
    public Object getCachedTask(Long taskId) {
        String key = TASK_CACHE_PREFIX + taskId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 将待接单任务加入有序集合（按创建时间排序）
     * @param taskId     任务ID
     * @param createTime 创建时间戳（毫秒）
     */
    public void addPendingTask(Long taskId, long createTime) {
        redisTemplate.opsForZSet().add(PENDING_TASKS_KEY, taskId.toString(), createTime);
        logger.debug("待接单任务已加入Redis ZSET: taskId={}, createTime={}", taskId, createTime);
    }

    /**
     * 从待接单中移除任务
     * @param taskId 任务ID
     */
    public void removePendingTask(Long taskId) {
        redisTemplate.opsForZSet().remove(PENDING_TASKS_KEY, taskId.toString());
        logger.debug("待接单任务已从Redis ZSET移除: taskId={}", taskId);
    }

    /**
     * 获取待接单任务列表（按时间倒序，最新的在前）
     * @param start 起始索引（含）
     * @param end   结束索引（含）
     * @return 任务ID列表
     */
    public List<Long> getPendingTaskIds(int start, int end) {
        // 倒序获取（最新创建的在前）
        Set<Object> ids = redisTemplate.opsForZSet().reverseRange(PENDING_TASKS_KEY, start, end);
        List<Long> result = new ArrayList<>();
        if (ids != null) {
            for (Object id : ids) {
                try {
                    result.add(Long.valueOf(id.toString()));
                } catch (NumberFormatException e) {
                    logger.warn("无效的任务ID: {}", id);
                }
            }
        }
        return result;
    }

    /**
     * 获取待接单任务总数
     * @return 任务数量
     */
    public long getPendingTaskCount() {
        Long count = redisTemplate.opsForZSet().size(PENDING_TASKS_KEY);
        return count != null ? count : 0;
    }

    /**
     * 任务状态变更后清理相关缓存
     * @param taskId 任务ID
     */
    public void evictTaskCache(Long taskId) {
        String key = TASK_CACHE_PREFIX + taskId;
        redisTemplate.delete(key);
        removePendingTask(taskId);
        logger.debug("任务缓存已从Redis清除: taskId={}", taskId);
    }

    // ==================== 登录限流 ====================

    /**
     * 记录登录失败，返回当前失败次数
     * @param username 用户名
     * @return 当前累计失败次数
     */
    public int recordLoginFailure(String username) {
        String key = LOGIN_FAIL_PREFIX + username;

        // 自增计数，首次设置时自动添加过期时间
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            // 首次失败，设置过期时间
            redisTemplate.expire(key, LOGIN_LOCK_MINUTES, TimeUnit.MINUTES);
        }

        logger.warn("登录失败: username={}, 当前失败次数: {}/{}", username, count, MAX_LOGIN_ATTEMPTS);
        return count != null ? count.intValue() : 0;
    }

    /**
     * 检查账号是否被锁定
     * @param username 用户名
     * @return true=已锁定（连续失败达到上限）
     */
    public boolean isLoginLocked(String username) {
        String key = LOGIN_FAIL_PREFIX + username;
        Object countObj = redisTemplate.opsForValue().get(key);
        if (countObj == null) {
            return false;
        }
        int count;
        try {
            count = Integer.parseInt(countObj.toString());
        } catch (NumberFormatException e) {
            return false;
        }
        return count >= MAX_LOGIN_ATTEMPTS;
    }

    /**
     * 获取账号剩余锁定时间（秒）
     * @param username 用户名
     * @return 剩余秒数，0表示未锁定
     */
    public long getLoginLockRemainingSeconds(String username) {
        String key = LOGIN_FAIL_PREFIX + username;
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl <= 0) {
            return 0;
        }
        return ttl;
    }

    /**
     * 登录成功后清除失败记录
     * @param username 用户名
     */
    public void clearLoginFailure(String username) {
        String key = LOGIN_FAIL_PREFIX + username;
        redisTemplate.delete(key);
        logger.debug("登录失败记录已清除: username={}", username);
    }

    // ==================== 签到去重 ====================

    /**
     * 检查今天是否已签到
     * @param userId     用户ID
     * @param dayOfMonth 今天在当月中的第几天 (1-31)
     * @return true=已签到
     */
    public boolean hasCheckedInToday(Long userId, int dayOfMonth) {
        String key = getCheckinKey(userId);
        Boolean isMember = redisTemplate.opsForSet().isMember(key, dayOfMonth);
        return Boolean.TRUE.equals(isMember);
    }

    /**
     * 标记今天已签到
     * @param userId     用户ID
     * @param dayOfMonth 今天在当月中的第几天 (1-31)
     */
    public void markCheckin(Long userId, int dayOfMonth) {
        String key = getCheckinKey(userId);
        redisTemplate.opsForSet().add(key, dayOfMonth);
        // 设置过期时间为当月剩余天数 + 1天缓冲
        LocalDate now = LocalDate.now();
        long daysRemaining = now.lengthOfMonth() - now.getDayOfMonth() + 2;
        redisTemplate.expire(key, daysRemaining, TimeUnit.DAYS);
        logger.debug("签到已标记: userId={}, dayOfMonth={}", userId, dayOfMonth);
    }

    /**
     * 获取连续签到天数
     * @param userId 用户ID
     * @param today  今天在本月中的第几天 (1-31)
     * @return 连续签到天数
     */
    public long getCheckinStreak(Long userId, int today) {
        String key = getCheckinKey(userId);
        Set<Object> members = redisTemplate.opsForSet().members(key);

        if (members == null || members.isEmpty()) {
            return 0;
        }

        // 将签到日期转换为整数集合
        Set<Integer> days = new HashSet<>();
        for (Object m : members) {
            try {
                days.add(Integer.parseInt(m.toString()));
            } catch (NumberFormatException ignored) {}
        }

        long streak = 0;
        // 从今天往前数，直到遇到未签到的日子
        for (int day = today; day >= 1; day--) {
            if (days.contains(day)) {
                streak++;
            } else {
                break;
            }
        }
        return streak;
    }

    // ==================== 私有辅助 ====================

    // ==================== 幂等去重 ====================

    /**
     * 幂等性检查：尝试获取幂等锁
     * @param key      幂等键
     * @param ttlSeconds 锁过期时间（秒）
     * @return true=获取成功（首次请求），false=重复请求
     */
    public boolean tryAcquireIdempotency(String key, long ttlSeconds) {
        String idempotencyKey = IDEMPOTENCY_PREFIX + key;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 释放幂等锁（任务创建失败时调用，允许重试）
     * @param key 幂等键
     */
    public void releaseIdempotency(String key) {
        String idempotencyKey = IDEMPOTENCY_PREFIX + key;
        redisTemplate.delete(idempotencyKey);
    }

    // ==================== 验证码 ====================

    /**
     * 存储验证码（用于忘记密码/绑定手机）
     * @param phone 手机号
     * @param code  验证码
     * @param ttlMinutes 过期时间（分钟）
     */
    public void storeVerifyCode(String phone, String code, long ttlMinutes) {
        String key = VERIFY_CODE_PREFIX + phone;
        redisTemplate.opsForValue().set(key, code, ttlMinutes, TimeUnit.MINUTES);
        logger.debug("验证码已存储: phone={}, ttl={}min", phone, ttlMinutes);
    }

    /**
     * 校验验证码
     * @param phone 手机号
     * @param code  用户输入的验证码
     * @return true=验证通过
     */
    public boolean verifyCode(String phone, String code) {
        String key = VERIFY_CODE_PREFIX + phone;
        Object stored = redisTemplate.opsForValue().get(key);
        if (stored == null || code == null) {
            return false;
        }
        boolean match = stored.toString().equals(code);
        if (match) {
            redisTemplate.delete(key); // 验证通过后删除
        }
        return match;
    }

    /**
     * 生成签到记录的Redis Key
     * 格式: checkin:{userId}:{yyyyMM}
     */
    private String getCheckinKey(Long userId) {
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        return CHECKIN_PREFIX + userId + ":" + yearMonth;
    }
}
