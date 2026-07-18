/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto;

import com.spark.volunteer.entity.Device;
import lombok.Data;

import java.util.Date;

/**
 * 设备响应DTO
 */
@Data
public class DeviceResponseDTO {

    private Long id;
    private String deviceId;       // 设备唯一标识
    private String name;           // 设备名称
    private String deviceType;     // 设备类型: ESP32_C3等
    private Long userId;           // 绑定用户ID
    private String status;         // 状态: ACTIVE/INACTIVE
    private Date lastOnlineTime;   // 最后在线时间
    private Date createTime;
    public static DeviceResponseDTO fromEntity(Device entity) {
        if (entity == null) return null;
        DeviceResponseDTO dto = new DeviceResponseDTO();
        dto.setId(entity.getId());
        dto.setDeviceId(entity.getDeviceId());
        dto.setName(entity.getName());
        dto.setDeviceType(entity.getDeviceType());
        dto.setUserId(entity.getUserId());
        dto.setStatus(entity.getStatus());
        dto.setLastOnlineTime(entity.getLastOnlineTime());
        dto.setCreateTime(entity.getCreateTime());
        return dto;
    }
}
