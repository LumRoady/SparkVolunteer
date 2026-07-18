/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto;

import com.spark.volunteer.entity.TaskChallenge;
import lombok.Data;

import java.util.Date;

/**
 * 任务挑战响应DTO
 */
@Data
public class TaskChallengeResponseDTO {

    private Long id;
    private String title;
    private String description;
    private Integer reward;
    private Boolean completed;
    private String contact;
    private String address;
    private Long userId;
    private Date createdAt;
    private Date updatedAt;

    public static TaskChallengeResponseDTO fromEntity(TaskChallenge entity) {
        if (entity == null) return null;
        TaskChallengeResponseDTO dto = new TaskChallengeResponseDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setReward(entity.getReward());
        dto.setCompleted(entity.getCompleted());
        dto.setContact(entity.getContact());
        dto.setAddress(entity.getAddress());
        dto.setUserId(entity.getUserId());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
