/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto.request;

import javax.validation.constraints.NotNull;

/**
 * 更新任务挑战请求 DTO
 * 用于任务挑战更新接口的请求参数
 */
public class UpdateTaskChallengeRequest {

    @NotNull(message = "任务挑战ID不能为空")
    private Long id;

    private String title;

    private String description;

    private Integer reward;

    private Boolean completed;

    private String contact;

    private String address;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getReward() { return reward; }
    public void setReward(Integer reward) { this.reward = reward; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
