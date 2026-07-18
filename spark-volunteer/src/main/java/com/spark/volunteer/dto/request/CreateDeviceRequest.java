/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto.request;

import com.spark.volunteer.entity.Device;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建设备请求 DTO
 * 用于设备创建接口的请求参数
 */
public class CreateDeviceRequest {

    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @NotBlank(message = "设备名称不能为空")
    private String name;

    private String deviceType;

    private Long userId;

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    /**
     * 将 DTO 转换为 Device 实体
     */
    public Device toDevice() {
        Device device = new Device();
        device.setDeviceId(this.deviceId);
        device.setName(this.name);
        device.setDeviceType(this.deviceType);
        device.setUserId(this.userId);
        return device;
    }
}
