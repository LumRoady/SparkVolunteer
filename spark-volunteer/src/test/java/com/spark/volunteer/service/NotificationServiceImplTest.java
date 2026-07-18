package com.spark.volunteer.service;

import com.spark.volunteer.entity.Notification;
import com.spark.volunteer.repository.NotificationRepository;
import com.spark.volunteer.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotification_success() {
        Notification notification = new Notification();
        notification.setUserId(1L);
        notification.setTitle("测试通知");
        notification.setType("SYSTEM");

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.createNotification(notification);

        assertNotNull(result);
        assertEquals("测试通知", result.getTitle());
        verify(notificationRepository).save(notification);
    }

    @Test
    void getNotificationById_found() {
        Notification notification = new Notification();
        notification.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        Notification result = notificationService.getNotificationById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getNotificationById_notFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        Notification result = notificationService.getNotificationById(999L);

        assertNull(result);
    }

    @Test
    void getNotificationsByUserId() {
        List<Notification> mockList = Arrays.asList(new Notification(), new Notification());
        when(notificationRepository.findByUserId(eq(1L), any(org.springframework.data.domain.Pageable.class))).thenReturn(new PageImpl<>(mockList));

        Page<Notification> result = notificationService.getNotificationsByUserId(1L, PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    void markAsRead_success() {
        Notification notification = new Notification();
        notification.setId(1L);
        notification.setIsRead(false);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.markAsRead(1L);

        assertNotNull(result);
        assertTrue(result.getIsRead());
    }

    @Test
    void markAsRead_notFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        Notification result = notificationService.markAsRead(999L);

        assertNull(result);
    }

    @Test
    void getUnreadCount() {
        when(notificationRepository.countByUserIdAndIsRead(1L, false)).thenReturn(5L);

        long count = notificationService.getUnreadCount(1L);

        assertEquals(5L, count);
    }

    @Test
    void deleteNotifications() {
        doNothing().when(notificationRepository).deleteAllById(anyList());

        int count = notificationService.deleteNotifications(Arrays.asList(1L, 2L, 3L));

        assertEquals(3, count);
        verify(notificationRepository).deleteAllById(Arrays.asList(1L, 2L, 3L));
    }
}
