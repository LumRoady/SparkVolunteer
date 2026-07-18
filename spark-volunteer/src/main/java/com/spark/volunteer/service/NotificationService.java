package com.spark.volunteer.service;

import com.spark.volunteer.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 通知服务接口
 * 定义通知相关的服务方法
 */
public interface NotificationService {

    // 创建通知
    Notification createNotification(Notification notification);

    // 根据ID获取通知
    Notification getNotificationById(Long id);

    // 获取用户的所有通知
    List<Notification> getNotificationsByUserId(Long userId);

    // 根据类型获取用户的通知
    List<Notification> getNotificationsByUserIdAndType(Long userId, String type);

    // 获取用户的未读通知
    List<Notification> getUnreadNotificationsByUserId(Long userId);

    // 标记通知为已读
    Notification markAsRead(Long id);

    // 批量标记通知为已读
    int markAllAsRead(Long userId);

    // 批量标记指定类型的通知为已读
    int markAllAsReadByType(Long userId, String type);

    // 批量删除通知
    int deleteNotifications(List<Long> ids);

    // 删除通知
    void deleteNotification(Long id);

    // 统计用户未读通知数量
    long getUnreadCount(Long userId);

    // 根据类型获取通知列表
    List<Notification> getNotificationsByType(String type);

    // 分页获取用户的通知
    Page<Notification> getNotificationsByUserId(Long userId, Pageable pageable);

    // 分页获取用户指定类型的通知
    Page<Notification> getNotificationsByUserIdAndType(Long userId, String type, Pageable pageable);

    // 获取所有通知
    List<Notification> getAllNotifications();
}