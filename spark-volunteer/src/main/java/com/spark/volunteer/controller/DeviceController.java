/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.PageResponse;
import com.spark.volunteer.common.Result;
import com.spark.volunteer.dto.DeviceResponseDTO;
import com.spark.volunteer.dto.request.CreateDeviceRequest;
import com.spark.volunteer.dto.request.UpdateDeviceRequest;
import com.spark.volunteer.entity.Device;
import com.spark.volunteer.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 设备控制器
 * 用于管理ESP32嵌入式遥控器设备
 */
@RestController
@RequestMapping("/api/device")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    /**
     * 创建设备
     * POST /api/device
     */
    @PostMapping
    public Result<DeviceResponseDTO> createDevice(@Valid @RequestBody CreateDeviceRequest request) {
        Device device = request.toDevice();
        Device createdDevice = deviceService.createDevice(device);
        return Result.success(DeviceResponseDTO.fromEntity(createdDevice));
    }

    /**
     * 根据ID获取设备
     * GET /api/device/{id}
     */
    @GetMapping("/{id}")
    public Result<DeviceResponseDTO> getDeviceById(@PathVariable Long id) {
        Device device = deviceService.getDeviceById(id);
        return Result.success(DeviceResponseDTO.fromEntity(device));
    }

    /**
     * 根据设备ID获取设备
     * GET /api/device/by-device-id/{deviceId}
     */
    @GetMapping("/by-device-id/{deviceId}")
    public Result<DeviceResponseDTO> getDeviceByDeviceId(@PathVariable String deviceId) {
        Device device = deviceService.getDeviceByDeviceId(deviceId);
        return Result.success(DeviceResponseDTO.fromEntity(device));
    }

    /**
     * 获取所有设备（支持分页）
     * GET /api/device?page=0&size=10
     */
    @GetMapping
    public Result<PageResponse<DeviceResponseDTO>> getAllDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Device> devicePage = deviceService.getAllDevices(pageable);
        PageResponse<DeviceResponseDTO> pageResponse = PageResponse.of(
                devicePage, DeviceResponseDTO::fromEntity);
        return Result.success(pageResponse);
    }

    /**
     * 根据用户ID获取设备（支持分页）
     * GET /api/device/by-user/{userId}?page=0&size=10
     */
    @GetMapping("/by-user/{userId}")
    public Result<PageResponse<DeviceResponseDTO>> getDevicesByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Device> devicePage = deviceService.getDevicesByUserId(userId, pageable);
        PageResponse<DeviceResponseDTO> pageResponse = PageResponse.of(
                devicePage, DeviceResponseDTO::fromEntity);
        return Result.success(pageResponse);
    }

    /**
     * 更新设备
     * PUT /api/device/{id}
     */
    @PutMapping("/{id}")
    public Result<DeviceResponseDTO> updateDevice(@PathVariable Long id, @Valid @RequestBody UpdateDeviceRequest request) {
        Device device = deviceService.getDeviceById(id);
        if (device == null) {
            return Result.error("设备不存在");
        }
        device.setName(request.getName());
        device.setDeviceType(request.getDeviceType());
        device.setUserId(request.getUserId());
        device.setStatus(request.getStatus());
        Device updatedDevice = deviceService.updateDevice(id, device);
        return Result.success(DeviceResponseDTO.fromEntity(updatedDevice));
    }

    /**
     * 删除设备
     * DELETE /api/device/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return Result.success();
    }

    /**
     * 绑定设备到用户
     * POST /api/device/bind
     */
    @PostMapping("/bind")
    public Result<DeviceResponseDTO> bindDeviceToUser(@RequestParam String deviceId, @RequestParam Long userId) {
        Device device = deviceService.bindDeviceToUser(deviceId, userId);
        return Result.success(DeviceResponseDTO.fromEntity(device));
    }

    /**
     * 解除设备绑定
     * POST /api/device/unbind
     */
    @PostMapping("/unbind")
    public Result<DeviceResponseDTO> unbindDeviceFromUser(@RequestParam String deviceId) {
        Device device = deviceService.unbindDeviceFromUser(deviceId);
        return Result.success(DeviceResponseDTO.fromEntity(device));
    }

    /**
     * 激活设备
     * POST /api/device/activate
     */
    @PostMapping("/activate")
    public Result<DeviceResponseDTO> activateDevice(@RequestParam String deviceId) {
        Device device = deviceService.activateDevice(deviceId);
        return Result.success(DeviceResponseDTO.fromEntity(device));
    }

    /**
     * 停用设备
     * POST /api/device/deactivate
     */
    @PostMapping("/deactivate")
    public Result<DeviceResponseDTO> deactivateDevice(@RequestParam String deviceId) {
        Device device = deviceService.deactivateDevice(deviceId);
        return Result.success(DeviceResponseDTO.fromEntity(device));
    }

    /**
     * 更新设备在线状态
     * POST /api/device/update-online-status
     */
    @PostMapping("/update-online-status")
    public Result<DeviceResponseDTO> updateDeviceOnlineStatus(@RequestParam String deviceId) {
        Device device = deviceService.updateDeviceOnlineStatus(deviceId);
        return Result.success(DeviceResponseDTO.fromEntity(device));
    }
}
