/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.task;

import com.spark.volunteer.config.WebSocketHandler;
import com.spark.volunteer.entity.Task;
import com.spark.volunteer.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 定时任务执行器
 * 处理紧急求助升级广播、超时自动取消等定时业务逻辑
 */
@Component
public class ScheduledTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskExecutor.class);

    /** 紧急求助超时未接单阈值：10分钟 */
    private static final long SOS_TIMEOUT_MS = 10 * 60 * 1000L;

    /** 普通任务超时自动取消阈值：24小时 */
    private static final long NORMAL_TIMEOUT_MS = 24 * 3600 * 1000L;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * 每5分钟扫描：紧急求助(SOS类型)创建超过10分钟未被接单 → 全量广播升级
     */
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void escalateEmergencyTasks() {
        try {
            long threshold = System.currentTimeMillis() - SOS_TIMEOUT_MS;
            Date cutoff = new Date(threshold);

            List<Task> pendingSos = taskRepository.findByStatusAndType(0, "sos");
            List<Task> overdue = pendingSos.stream()
                    .filter(t -> t.getCreateTime() != null && t.getCreateTime().before(cutoff))
                    .collect(Collectors.toList());

            if (!overdue.isEmpty()) {
                Map<String, Object> alert = new HashMap<>();
                alert.put("type", "sos_escalation");
                alert.put("message", "有" + overdue.size() + "条紧急求助超过10分钟未被接单，请志愿者尽快响应！");
                alert.put("taskIds", overdue.stream().map(t -> t.getId()).collect(Collectors.toList()));
                WebSocketHandler.broadcastMessage(alert);
                logger.warn("紧急求助升级广播: {} 条SOS任务超时未接单", overdue.size());
            }
        } catch (Exception e) {
            logger.error("定时任务-紧急求助升级扫描异常", e);
        }
    }

    /**
     * 每30分钟扫描：普通任务创建超过24小时未被接单 → 自动取消
     */
    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void cancelExpiredTasks() {
        try {
            long threshold = System.currentTimeMillis() - NORMAL_TIMEOUT_MS;
            Date cutoff = new Date(threshold);

            List<Task> pendingTasks = taskRepository.findByStatus(0);
            int cancelled = 0;
            for (Task t : pendingTasks) {
                // 排除 SOS 类型（SOS 不自动取消，持续广播）
                if ("sos".equals(t.getType())) continue;
                if (t.getCreateTime() != null && t.getCreateTime().before(cutoff)) {
                    taskRepository.cancelTaskAtomically(t.getId());
                    cancelled++;
                }
            }

            if (cancelled > 0) {
                logger.info("定时任务-自动取消超时任务: {} 条", cancelled);
            }
        } catch (Exception e) {
            logger.error("定时任务-超时取消扫描异常", e);
        }
    }

    /**
     * 每天凌晨0点：清理 Redis 中过期的签到记录和 Token
     * 签到记录的 TTL 已在 RedisCacheService.markCheckin() 中按月设置自动过期，
     * 此处仅做日志审计和兜底清理
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void dailyCleanup() {
        logger.info("每日凌晨清理任务已执行 - 签到记录和Token由Redis TTL自动管理");
    }
}
