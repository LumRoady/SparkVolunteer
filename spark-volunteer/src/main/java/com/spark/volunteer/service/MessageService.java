package com.spark.volunteer.service;

import com.spark.volunteer.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 消息服务接口
 * 定义消息相关的服务方法
 */
public interface MessageService {

    /**
     * 创建消息
     * @param message 消息对象
     * @return 创建的消息
     */
    Message createMessage(Message message);

    /**
     * 获取消息详情
     * @param id 消息ID
     * @return 消息对象
     */
    Message getMessageById(Long id);

    /**
     * 获取用户的会话列表
     * @param userId 用户ID
     * @return 会话列表，每个会话包含对方用户信息和最后一条消息
     */
    List<Map<String, Object>> getConversations(Long userId);

    /**
     * 分页获取两个用户之间的消息列表
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @param pageable 分页参数
     * @return 分页消息列表
     */
    Page<Message> getMessagesBetweenUsers(Long userId1, Long userId2, Pageable pageable);

    /**
     * 获取两个用户之间的消息列表（不分页）
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @return 消息列表
     */
    List<Message> getMessagesBetweenUsers(Long userId1, Long userId2);

    /**
     * 发送消息
     * @param message 消息对象
     * @return 发送的消息
     */
    Message sendMessage(Message message);

    /**
     * 更新消息状态
     * @param id 消息ID
     * @param status 新状态
     * @return 更新后的消息
     */
    Message updateMessageStatus(Long id, String status);

    /**
     * 标记所有消息为已读
     * @param userId 用户ID
     * @return 更新的消息数量
     */
    int markAllMessagesAsRead(Long userId);

    /**
     * 删除消息
     * @param id 消息ID
     */
    void deleteMessage(Long id);

    /**
     * 获取用户的未读消息数量
     * @param userId 用户ID
     * @return 未读消息数量
     */
    int getUnreadMessageCount(Long userId);
}
