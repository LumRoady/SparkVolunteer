/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建任务请求 DTO
 * 用于任务创建接口的请求参数，仅暴露允许填写的字段，防止 Mass Assignment
 */
public class CreateTaskRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private Long requesterId;
    private String deviceId;

    @NotBlank(message = "任务类型不能为空")
    private String type;

    @NotBlank(message = "任务标题不能为空")
    private String title;

    @NotBlank(message = "任务描述不能为空")
    private String content;

    private String location;
    private Double latitude;
    private Double longitude;
    private Integer urgency;
    private Integer priority;
    private Integer estimatedTime;
    private Boolean needHomeService;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRequesterId() { return requesterId; }
    public void setRequesterId(Long requesterId) { this.requesterId = requesterId; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Integer getUrgency() { return urgency; }
    public void setUrgency(Integer urgency) { this.urgency = urgency; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Integer getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(Integer estimatedTime) { this.estimatedTime = estimatedTime; }

    public Boolean getNeedHomeService() { return needHomeService; }
    public void setNeedHomeService(Boolean needHomeService) { this.needHomeService = needHomeService; }

    /**
     * 将 DTO 转换为 Task 实体
     */
    public com.spark.volunteer.entity.Task toTask() {
        com.spark.volunteer.entity.Task task = new com.spark.volunteer.entity.Task();
        task.setUserId(this.userId);
        task.setRequesterId(this.requesterId);
        task.setDeviceId(this.deviceId);
        task.setType(this.type);
        task.setTitle(this.title);
        task.setContent(this.content);
        task.setLocation(this.location);
        task.setLatitude(this.latitude);
        task.setLongitude(this.longitude);
        task.setUrgency(this.urgency);
        task.setPriority(this.priority);
        task.setEstimatedTime(this.estimatedTime);
        task.setNeedHomeService(this.needHomeService);
        return task;
    }
}
