/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto;

import com.spark.volunteer.entity.Notification;
import lombok.Data;

import java.util.Date;

/**
 * 通知响应DTO
 */
@Data
public class NotificationResponseDTO {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private Boolean isRead;
    private Date createTime;

    public static NotificationResponseDTO fromEntity(Notification entity) {
        if (entity == null) return null;
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setType(entity.getType());
        dto.setIsRead(entity.getIsRead());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }
}
