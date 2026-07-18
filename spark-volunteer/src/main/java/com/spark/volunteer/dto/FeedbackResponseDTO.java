/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto;

import com.spark.volunteer.entity.Feedback;
import lombok.Data;

import java.util.Date;

/**
 * 评价响应DTO
 */
@Data
public class FeedbackResponseDTO {

    private Long id;
    private Long taskId;
    private Integer score;
    private String comment;
    private Date createTime;

    public static FeedbackResponseDTO fromEntity(Feedback entity) {
        if (entity == null) return null;
        FeedbackResponseDTO dto = new FeedbackResponseDTO();
        dto.setId(entity.getId());
        dto.setTaskId(entity.getTaskId());
        dto.setScore(entity.getScore());
        dto.setComment(entity.getComment());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }
}
