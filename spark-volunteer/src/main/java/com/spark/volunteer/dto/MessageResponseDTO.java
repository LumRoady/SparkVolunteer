/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto;

import com.spark.volunteer.entity.Message;
import lombok.Data;

import java.util.Date;

/**
 * 消息响应DTO
 */
@Data
public class MessageResponseDTO {

    private Long id;
    private Long senderId;
    private Long receiverId;
    private String messageType;
    private String content;
    private String status;
    private Long taskId;
    private Date createTime;

    public static MessageResponseDTO fromEntity(Message entity) {
        if (entity == null) return null;
        MessageResponseDTO dto = new MessageResponseDTO();
        dto.setId(entity.getId());
        dto.setSenderId(entity.getSenderId());
        dto.setReceiverId(entity.getReceiverId());
        dto.setMessageType(entity.getMessageType());
        dto.setContent(entity.getContent());
        dto.setStatus(entity.getStatus());
        dto.setTaskId(entity.getTaskId());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }
}
