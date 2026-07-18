/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto;

import com.spark.volunteer.entity.Checkin;
import java.util.Date;

/**
 * 签到记录响应 DTO
 */
public class CheckinResponseDTO {

    private Long id;
    private Long userId;
    private Date checkinDate;
    private Date checkinTime;
    private String status;
    private Date createTime;

    public static CheckinResponseDTO fromEntity(Checkin entity) {
        CheckinResponseDTO dto = new CheckinResponseDTO();
        dto.id = entity.getId();
        dto.userId = entity.getUserId();
        dto.checkinDate = entity.getCheckinDate();
        dto.checkinTime = entity.getCheckinTime();
        dto.status = entity.getStatus();
        dto.createTime = entity.getCreateTime();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Date getCheckinDate() { return checkinDate; }
    public void setCheckinDate(Date checkinDate) { this.checkinDate = checkinDate; }

    public Date getCheckinTime() { return checkinTime; }
    public void setCheckinTime(Date checkinTime) { this.checkinTime = checkinTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
