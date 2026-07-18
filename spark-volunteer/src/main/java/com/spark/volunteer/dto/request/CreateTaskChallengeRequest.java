/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto.request;

import com.spark.volunteer.entity.TaskChallenge;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建任务挑战请求 DTO
 * 用于任务挑战创建接口的请求参数
 */
public class CreateTaskChallengeRequest {

    @NotBlank(message = "挑战标题不能为空")
    private String title;

    @NotBlank(message = "挑战描述不能为空")
    private String description;

    @NotNull(message = "奖励积分不能为空")
    private Integer reward;

    private String contact;

    private String address;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getReward() { return reward; }
    public void setReward(Integer reward) { this.reward = reward; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    /**
     * 将 DTO 转换为 TaskChallenge 实体
     */
    public TaskChallenge toTaskChallenge() {
        TaskChallenge taskChallenge = new TaskChallenge();
        taskChallenge.setTitle(this.title);
        taskChallenge.setDescription(this.description);
        taskChallenge.setReward(this.reward);
        taskChallenge.setContact(this.contact);
        taskChallenge.setAddress(this.address);
        taskChallenge.setUserId(this.userId);
        return taskChallenge;
    }
}
