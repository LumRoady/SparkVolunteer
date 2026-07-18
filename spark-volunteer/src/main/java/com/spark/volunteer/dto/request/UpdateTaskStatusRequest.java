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
 * 任务状态更新请求 DTO
 */
public class UpdateTaskStatusRequest {

    @NotNull(message = "任务ID不能为空")
    private Long taskId;

    private String newStatus;
    private String status;

    private Long volunteerId;

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public String getNewStatus() {
        return newStatus != null ? newStatus : status;
    }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getVolunteerId() { return volunteerId; }
    public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }
}
