/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto.request;

import com.spark.volunteer.entity.Notification;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建通知请求 DTO
 * 用于通知创建接口的请求参数
 */
public class CreateNotificationRequest {

    // 系统通知无需指定用户ID
    private Long userId;

    @NotBlank(message = "通知类型不能为空")
    private String type;

    @NotBlank(message = "通知标题不能为空")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    private String content;

    private Long taskId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    /**
     * 将 DTO 转换为 Notification 实体
     */
    public Notification toNotification() {
        Notification notification = new Notification();
        notification.setUserId(this.userId);
        notification.setType(this.type);
        notification.setTitle(this.title);
        notification.setContent(this.content);
        notification.setTaskId(this.taskId);
        return notification;
    }
}
