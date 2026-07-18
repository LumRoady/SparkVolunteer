package com.spark.volunteer.repository;

import com.spark.volunteer.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知数据访问接口
 * 继承JpaRepository，自动拥有CRUD功能
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 根据用户ID查找通知
    List<Notification> findByUserId(Long userId);
    org.springframework.data.domain.Page<Notification> findByUserId(Long userId, org.springframework.data.domain.Pageable pageable);

    // 根据用户ID和类型查找通知
    List<Notification> findByUserIdAndType(Long userId, String type);
    org.springframework.data.domain.Page<Notification> findByUserIdAndType(Long userId, String type, org.springframework.data.domain.Pageable pageable);

    // 根据用户ID和已读状态查找通知
    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);

    // 根据类型查找通知
    List<Notification> findByType(String type);

    // 根据用户ID和类型以及已读状态查找通知
    List<Notification> findByUserIdAndTypeAndIsRead(Long userId, String type, Boolean isRead);

    // 根据任务ID查找通知
    List<Notification> findByTaskId(Long taskId);

    // 统计用户未读通知数量
    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    /**
     * 批量标记通知为已读（避免 N+1）
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int batchMarkAllAsRead(@Param("userId") Long userId);

    /**
     * 按类型批量标记已读
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.type = :type AND n.isRead = false")
    int batchMarkAsReadByType(@Param("userId") Long userId, @Param("type") String type);
}