package com.spark.volunteer.service.impl;

import com.spark.volunteer.entity.Message;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.repository.MessageRepository;
import com.spark.volunteer.repository.UserRepository;
import com.spark.volunteer.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * 消息服务实现类
 * 实现消息相关的服务方法
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Message createMessage(Message message) {
        message.setCreateTime(new Date());
        message.setUpdateTime(new Date());
        message.setStatus("UNREAD");
        return messageRepository.save(message);
    }

    @Override
    public Message getMessageById(Long id) {
        return messageRepository.findById(id).orElse(null);
    }

    @Override
    public List<Map<String, Object>> getConversations(Long userId) {
        List<Map<String, Object>> conversations = new ArrayList<>();
        
        // 获取会话ID列表
        List<Long> conversationIds = messageRepository.findConversationIds(userId);
        
        for (Long otherUserId : conversationIds) {
            // 获取对方用户信息
            User otherUser = userRepository.findById(otherUserId).orElse(null);
            if (otherUser == null) {
                continue;
            }
            
            // 获取两个用户之间的最新消息
            List<Message> messages = messageRepository.findBySenderIdAndReceiverId(userId, otherUserId);
            if (messages.isEmpty()) {
                messages = messageRepository.findBySenderIdAndReceiverId(otherUserId, userId);
            }
            
            Message lastMessage = null;
            if (!messages.isEmpty()) {
                lastMessage = messages.get(messages.size() - 1);
            }
            
            // 构建会话信息
            Map<String, Object> conversation = new HashMap<>();
            conversation.put("userId", otherUser.getId());
            conversation.put("username", otherUser.getUsername());
            conversation.put("name", otherUser.getName());
            conversation.put("avatar", otherUser.getAvatar());
            conversation.put("lastMessage", lastMessage);
            conversation.put("lastMessageTime", lastMessage != null ? lastMessage.getCreateTime() : null);
            
            conversations.add(conversation);
        }
        
        return conversations;
    }

    @Override
    public List<Message> getMessagesBetweenUsers(Long userId1, Long userId2) {
        List<Message> messages1 = messageRepository.findBySenderIdAndReceiverId(userId1, userId2);
        List<Message> messages2 = messageRepository.findBySenderIdAndReceiverId(userId2, userId1);
        
        List<Message> allMessages = new ArrayList<>();
        allMessages.addAll(messages1);
        allMessages.addAll(messages2);
        
        // 按时间排序
        allMessages.sort((m1, m2) -> m1.getCreateTime().compareTo(m2.getCreateTime()));
        
        return allMessages;
    }

    @Override
    public Page<Message> getMessagesBetweenUsers(Long userId1, Long userId2, Pageable pageable) {
        return messageRepository.findMessagesBetweenUsers(userId1, userId2, pageable);
    }

    @Override
    public Message sendMessage(Message message) {
        message.setCreateTime(new Date());
        message.setUpdateTime(new Date());
        message.setStatus("UNREAD");
        return messageRepository.save(message);
    }

    @Override
    public Message updateMessageStatus(Long id, String status) {
        Message message = messageRepository.findById(id).orElse(null);
        if (message != null) {
            message.setStatus(status);
            message.setUpdateTime(new Date());
            return messageRepository.save(message);
        }
        return null;
    }

    @Override
    public int markAllMessagesAsRead(Long userId) {
        return messageRepository.batchMarkAsRead(userId);
    }

    @Override
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    @Override
    public int getUnreadMessageCount(Long userId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndStatus(userId, "UNREAD");
        return unreadMessages.size();
    }
}
