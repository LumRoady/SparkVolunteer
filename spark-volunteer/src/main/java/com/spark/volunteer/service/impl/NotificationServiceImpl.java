package com.spark.volunteer.service.impl;

import com.spark.volunteer.entity.Notification;
import com.spark.volunteer.repository.NotificationRepository;
import com.spark.volunteer.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知服务实现类
 * 实现通知相关的服务方法
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }

    @Override
    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }

    @Override
    public List<Notification> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public List<Notification> getNotificationsByUserIdAndType(Long userId, String type) {
        return notificationRepository.findByUserIdAndType(userId, type);
    }

    @Override
    public List<Notification> getUnreadNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false);
    }

    @Override
    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null) {
            notification.setIsRead(true);
            return notificationRepository.save(notification);
        }
        return null;
    }

    @Override
    public int markAllAsRead(Long userId) {
        return notificationRepository.batchMarkAllAsRead(userId);
    }

    @Override
    public int markAllAsReadByType(Long userId, String type) {
        return notificationRepository.batchMarkAsReadByType(userId, type);
    }

    @Override
    public int deleteNotifications(List<Long> ids) {
        notificationRepository.deleteAllById(ids);
        return ids.size();
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Override
    public List<Notification> getNotificationsByType(String type) {
        return notificationRepository.findByType(type);
    }

    @Override
    public Page<Notification> getNotificationsByUserId(Long userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Notification> getNotificationsByUserIdAndType(Long userId, String type, Pageable pageable) {
        return notificationRepository.findByUserIdAndType(userId, type, pageable);
    }

    @Override
    public List<Notification> getAllNotifications() {
        return (List<Notification>) notificationRepository.findAll();
    }
}