/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto.request;

import com.spark.volunteer.entity.Feedback;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 创建评价请求 DTO
 * 用于评价创建接口的请求参数
 */
public class CreateFeedbackRequest {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1分")
    @Max(value = 5, message = "评分最高为5分")
    private Integer score;

    private String comment;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    /**
     * 将 DTO 转换为 Feedback 实体
     */
    public Feedback toFeedback() {
        Feedback feedback = new Feedback();
        feedback.setTaskId(this.taskId);
        feedback.setScore(this.score);
        feedback.setComment(this.comment);
        return feedback;
    }
}
