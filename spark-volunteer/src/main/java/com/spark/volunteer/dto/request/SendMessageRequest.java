/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto.request;

import com.spark.volunteer.entity.Message;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 发送消息请求 DTO
 * 用于消息发送接口的请求参数
 */
public class SendMessageRequest {

    @NotNull(message = "发送者ID不能为空")
    private Long senderId;

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private String messageType;

    private Long taskId;

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    /**
     * 将 DTO 转换为 Message 实体
     */
    public Message toMessage() {
        Message message = new Message();
        message.setSenderId(this.senderId);
        message.setReceiverId(this.receiverId);
        message.setContent(this.content);
        message.setMessageType(this.messageType);
        message.setTaskId(this.taskId);
        return message;
    }
}
