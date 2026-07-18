package com.spark.volunteer.repository;

import com.spark.volunteer.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 消息数据访问接口
 * 用于消息数据的CRUD操作
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * 根据接收者ID获取消息列表
     * @param receiverId 接收者ID
     * @return 消息列表
     */
    List<Message> findByReceiverId(Long receiverId);

    /**
     * 根据发送者ID和接收者ID获取消息列表（会话）
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @return 消息列表
     */
    List<Message> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    /**
     * 获取用户的会话列表（去重）
     * @param userId 用户ID
     * @return 会话列表
     */
    @Query(value = "SELECT DISTINCT CASE WHEN sender_id = :userId THEN receiver_id ELSE sender_id END as otherUserId FROM message WHERE sender_id = :userId OR receiver_id = :userId ORDER BY create_time DESC", nativeQuery = true)
    List<Long> findConversationIds(@Param("userId") Long userId);

    /**
     * 根据接收者ID和状态获取消息列表
     * @param receiverId 接收者ID
     * @param status 消息状态
     * @return 消息列表
     */
    List<Message> findByReceiverIdAndStatus(Long receiverId, String status);

    /**
     * 根据任务ID获取消息列表
     * @param taskId 任务ID
     * @return 消息列表
     */
    List<Message> findByTaskId(Long taskId);

    /**
     * 分页获取两个用户之间的消息（按时间倒序）
     */
    @Query(value = "SELECT * FROM message WHERE (sender_id = :userId1 AND receiver_id = :userId2) OR (sender_id = :userId2 AND receiver_id = :userId1) ORDER BY create_time DESC",
           countQuery = "SELECT count(*) FROM message WHERE (sender_id = :userId1 AND receiver_id = :userId2) OR (sender_id = :userId2 AND receiver_id = :userId1)",
           nativeQuery = true)
    Page<Message> findMessagesBetweenUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2, Pageable pageable);

    /**
     * 批量标记消息为已读（避免 N+1）
     */
    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.status = 'READ', m.updateTime = CURRENT_TIMESTAMP WHERE m.receiverId = :userId AND m.status = 'UNREAD'")
    int batchMarkAsRead(@Param("userId") Long userId);
}
