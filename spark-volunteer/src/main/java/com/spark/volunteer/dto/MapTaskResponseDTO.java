/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto;

import com.spark.volunteer.entity.Task;
import java.util.Date;

/**
 * 地图附近任务响应 DTO
 * 仅暴露地图场景需要的字段
 */
public class MapTaskResponseDTO {

    private Long id;
    private String title;
    private String type;
    private String location;
    private Double latitude;
    private Double longitude;
    private Date createTime;

    public static MapTaskResponseDTO fromTask(Task task) {
        MapTaskResponseDTO dto = new MapTaskResponseDTO();
        dto.id = task.getId();
        dto.title = task.getTitle();
        dto.type = task.getType();
        dto.location = task.getLocation();
        dto.latitude = task.getLatitude();
        dto.longitude = task.getLongitude();
        dto.createTime = task.getCreateTime();
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}
