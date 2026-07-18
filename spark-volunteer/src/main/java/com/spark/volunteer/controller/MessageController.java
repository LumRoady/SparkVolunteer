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
import com.spark.volunteer.dto.MessageResponseDTO;
import com.spark.volunteer.dto.request.SendMessageRequest;
import com.spark.volunteer.entity.Message;
import com.spark.volunteer.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息控制器
 * 处理消息相关的HTTP请求
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    /**
     * 获取用户的会话列表
     * GET /api/messages/conversations?userId=1
     */
    @GetMapping("/conversations")
    public Result<List<Map<String, Object>>> getConversations(@RequestParam Long userId) {
        List<Map<String, Object>> conversations = messageService.getConversations(userId);
        return Result.success(conversations);
    }

    /**
     * 获取用户的消息列表（支持分页）
     * GET /api/messages/{conversationId}?userId=1&page=0&size=10
     */
    @GetMapping("/{conversationId}")
    public Result<PageResponse<MessageResponseDTO>> getMessages(
            @PathVariable Long conversationId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Message> messagePage = messageService.getMessagesBetweenUsers(userId, conversationId, pageable);
        PageResponse<MessageResponseDTO> pageResponse = PageResponse.of(
                messagePage, MessageResponseDTO::fromEntity);
        return Result.success(pageResponse);
    }

    /**
     * 发送消息
     * POST /api/messages
     */
    @PostMapping
    public Result<MessageResponseDTO> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        Message message = request.toMessage();
        Message sentMessage = messageService.sendMessage(message);
        return Result.success(MessageResponseDTO.fromEntity(sentMessage));
    }

    /**
     * 更新消息状态
     * PUT /api/messages/{id}/status
     */
    @PutMapping("/{id}/status")
    public Result<MessageResponseDTO> updateMessageStatus(@PathVariable Long id, @RequestParam String status) {
        Message updatedMessage = messageService.updateMessageStatus(id, status);
        return Result.success(MessageResponseDTO.fromEntity(updatedMessage));
    }

    /**
     * 标记所有消息为已读
     * PUT /api/messages/read-all?userId=1
     */
    @PutMapping("/read-all")
    public Result<Integer> markAllAsRead(@RequestParam Long userId) {
        int count = messageService.markAllMessagesAsRead(userId);
        return Result.success(count);
    }

    /**
     * 获取用户的未读消息数量
     * GET /api/messages/unread-count?userId=1
     */
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount(@RequestParam Long userId) {
        int count = messageService.getUnreadMessageCount(userId);
        return Result.success(count);
    }
}
