/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.PageResponse;
import com.spark.volunteer.common.Result;
import com.spark.volunteer.dto.NotificationResponseDTO;
import com.spark.volunteer.dto.request.CreateNotificationRequest;
import com.spark.volunteer.entity.Notification;
import com.spark.volunteer.exception.NotFoundException;
import com.spark.volunteer.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 通知控制器
 * 处理通知相关的HTTP请求
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取用户的通知（支持分页）
     * GET /api/notifications?userId=1&page=0&size=10&type=SYSTEM
     */
    @GetMapping
    public Result<PageResponse<NotificationResponseDTO>> getNotifications(
            @RequestParam Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Notification> notificationPage;
        if (type != null) {
            notificationPage = notificationService.getNotificationsByUserIdAndType(userId, type, pageable);
        } else {
            notificationPage = notificationService.getNotificationsByUserId(userId, pageable);
        }
        PageResponse<NotificationResponseDTO> pageResponse = PageResponse.of(
                notificationPage, NotificationResponseDTO::fromEntity);
        return Result.success(pageResponse);
    }

    /**
     * 获取通知详情（自动标记为已读）
     * GET /api/notifications/{id}
     */
    @GetMapping("/{id}")
    public Result<NotificationResponseDTO> getNotificationById(@PathVariable Long id) {
        Notification notification = notificationService.getNotificationById(id);
        if (notification == null) {
            throw new NotFoundException("通知", id);
        }
        notificationService.markAsRead(id);
        return Result.success(NotificationResponseDTO.fromEntity(notification));
    }

    /**
     * 创建通知
     * POST /api/notifications
     */
    @PostMapping
    public Result<NotificationResponseDTO> createNotification(@Valid @RequestBody CreateNotificationRequest request) {
        Notification notification = request.toNotification();
        Notification createdNotification = notificationService.createNotification(notification);
        return Result.success(NotificationResponseDTO.fromEntity(createdNotification));
    }

    /**
     * 标记通知为已读
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public Result<NotificationResponseDTO> markAsRead(@PathVariable Long id) {
        Notification notification = notificationService.markAsRead(id);
        if (notification == null) {
            throw new NotFoundException("通知", id);
        }
        return Result.success(NotificationResponseDTO.fromEntity(notification));
    }

    /**
     * 批量标记通知为已读
     * PUT /api/notifications/read?userId=1&type=SYSTEM
     */
    @PutMapping("/read")
    public Result<Integer> markAllAsRead(
            @RequestParam Long userId,
            @RequestParam(required = false) String type) {
        int count;
        if (type != null) {
            count = notificationService.markAllAsReadByType(userId, type);
        } else {
            count = notificationService.markAllAsRead(userId);
        }
        return Result.success(count);
    }

    /**
     * 批量删除通知
     * DELETE /api/notifications
     */
    @DeleteMapping
    public Result<Integer> deleteNotifications(@RequestBody List<Long> ids) {
        int count = notificationService.deleteNotifications(ids);
        return Result.success(count);
    }

    /**
     * 删除通知
     * DELETE /api/notifications/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.success();
    }

    /**
     * 获取用户未读通知数量
     * GET /api/notifications/unread-count?userId=1
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(@RequestParam Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 获取用户未读通知
     * GET /api/notifications/unread?userId=1
     */
    @GetMapping("/unread")
    public Result<List<NotificationResponseDTO>> getUnreadNotifications(@RequestParam Long userId) {
        List<Notification> notifications = notificationService.getUnreadNotificationsByUserId(userId);
        List<NotificationResponseDTO> dtoList = notifications.stream()
                .map(NotificationResponseDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
        return Result.success(dtoList);
    }
}
