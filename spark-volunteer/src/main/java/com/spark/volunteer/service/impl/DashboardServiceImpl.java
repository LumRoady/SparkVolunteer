/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.service.impl;

import com.spark.volunteer.config.WebSocketHandler;
import com.spark.volunteer.entity.Task;
import com.spark.volunteer.repository.TaskRepository;
import com.spark.volunteer.repository.UserRepository;
import com.spark.volunteer.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据大屏服务实现类
 * 将统计逻辑从 Controller 层抽离，遵循单一职责原则
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> data = new HashMap<>();

        // 今日零点
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date todayStart = cal.getTime();

        // --- 核心指标 ---
        long todayHelpCount = taskRepository.countTodayCreated(todayStart);
        long todayCompleted = taskRepository.countTodayCompleted(todayStart);
        long todayAccepted = taskRepository.countTodayAccepted(todayStart);

        data.put("todayHelpCount", todayHelpCount);
        data.put("todayCompletedCount", todayCompleted);

        // 响应率
        double responseRate = todayHelpCount > 0
                ? Math.round(todayAccepted * 10000.0 / todayHelpCount) / 100.0
                : 100.0;
        data.put("responseRate", responseRate);

        // 平均响应时间（使用数据库聚合查询，避免全量加载）
        Double avgMinutes = taskRepository.avgResponseMinutesToday(todayStart);
        data.put("avgResponseMinutes", avgMinutes != null ? Math.round(avgMinutes * 10) / 10.0 : 0);

        // 在线志愿者
        data.put("onlineVolunteers", WebSocketHandler.getSessionCount());

        // 注册统计（使用 COUNT 聚合，避免全量加载）
        data.put("totalElderly", userRepository.countByRole("ELDERLY"));
        data.put("totalVolunteers", userRepository.countByRole("VOLUNTEER"));

        // 覆盖社区数（使用 DISTINCT 查询，避免全量加载）
        List<String> communities = userRepository.findDistinctCommunities();
        data.put("communityCount", communities.size());

        // 待处理紧急求助
        data.put("emergencyPending", taskRepository.countEmergencyPending());

        // --- 最近5条动态 ---
        List<Task> recentTasks = taskRepository.findByCreateTimeBetween(
                new Date(System.currentTimeMillis() - 24 * 3600 * 1000), new Date());
        List<Map<String, Object>> recentList = recentTasks.stream()
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .limit(5)
                .map(t -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("type", t.getType());
                    m.put("title", t.getTitle());
                    // Task 实体无 community 字段，默认显示“星火社区”
                    m.put("community", "星火社区");
                    m.put("time", formatRelativeTime(t.getCreateTime()));
                    m.put("status", getStatusLabel(t.getStatus()));
                    return m;
                }).collect(Collectors.toList());
        data.put("recentTasks", recentList);

        // --- 近7天趋势（使用数据库 GROUP BY 聚合，避免全量加载） ---
        cal.setTime(todayStart);
        cal.add(Calendar.DAY_OF_YEAR, -6);
        Date sevenDaysAgo = cal.getTime();

        // 初始化7天日期框架
        Map<String, Long> dayCount = new LinkedHashMap<>();
        cal.setTime(sevenDaysAgo);
        for (int i = 0; i < 7; i++) {
            String label = new SimpleDateFormat("MM-dd").format(cal.getTime());
            dayCount.put(label, 0L);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }

        // 填入数据库聚合结果
        List<Object[]> dailyCounts = taskRepository.countByDaySince(sevenDaysAgo);
        for (Object[] row : dailyCounts) {
            String dateStr = row[0] != null ? row[0].toString() : "";
            long cnt = row[1] != null ? ((Number) row[1]).longValue() : 0L;
            // 数据库返回 yyyy-MM-dd，转换为 MM-dd
            if (dateStr.length() >= 10) {
                dateStr = dateStr.substring(5);
            }
            if (dayCount.containsKey(dateStr)) {
                dayCount.put(dateStr, cnt);
            }
        }

        List<Map<String, Object>> weeklyTrend = new ArrayList<>();
        dayCount.forEach((date, count) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("date", date);
            m.put("count", count);
            weeklyTrend.add(m);
        });
        data.put("weeklyTrend", weeklyTrend);

        return data;
    }

    private String formatRelativeTime(Date date) {
        long diff = System.currentTimeMillis() - date.getTime();
        long minutes = diff / 60000;
        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";
        long hours = minutes / 60;
        if (hours < 24) return hours + "小时前";
        return new SimpleDateFormat("MM-dd HH:mm").format(date);
    }

    private String getStatusLabel(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0:  return "待接单";
            case 1:  return "已接单";
            case 2:  return "已完成";
            case 3:  return "已取消";
            default: return "未知";
        }
    }
}
