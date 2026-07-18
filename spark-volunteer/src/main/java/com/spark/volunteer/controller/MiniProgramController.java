/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import com.spark.volunteer.dto.DeviceResponseDTO;
import com.spark.volunteer.dto.TaskResponseDTO;
import com.spark.volunteer.entity.Device;
import com.spark.volunteer.entity.Task;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.service.TaskService;
import com.spark.volunteer.service.UserService;
import com.spark.volunteer.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小程序专用控制器
 * 为面向空巢老人的小程序提供定制化接口
 */
@RestController
@RequestMapping("/api/mini-program")
@Tag(name = "小程序专用", description = "为微信小程序提供的定制化接口")
public class MiniProgramController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceService deviceService;

    /**
     * 获取所有待处理的紧急求助任务
     * GET /api/mini-program/tasks/emergency/pending
     */
    @GetMapping("/tasks/emergency/pending")
    public Result<List<TaskResponseDTO>> getPendingEmergencyTasks() {
        List<Task> emergencyTasks = taskService.getTasks(0, "sos");
        List<TaskResponseDTO> dtoList = emergencyTasks.stream()
                .map(TaskResponseDTO::fromTask)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }

    /**
     * 志愿者抢单（小程序优化版）
     * POST /api/mini-program/tasks/{taskId}/accept-by-volunteer
     */
    @PostMapping("/tasks/{taskId}/accept-by-volunteer")
    @PreAuthorize("hasAuthority('VOLUNTEER') or hasAuthority('ADMIN')")
    public Result<TaskResponseDTO> acceptTaskByVolunteer(@PathVariable Long taskId,
                                              @RequestBody VolunteerAcceptRequest request) {
        Long volunteerId = request.getVolunteerId();
        if (volunteerId == null) {
            return Result.error("志愿者ID不能为空");
        }

        Task task = taskService.acceptTask(taskId, volunteerId);
        return Result.success(TaskResponseDTO.fromTask(task));
    }

    /**
     * 获取老人发布的所有任务
     * GET /api/mini-program/elderly/{elderlyId}/tasks
     */
    @GetMapping("/elderly/{elderlyId}/tasks")
    public Result<List<TaskResponseDTO>> getElderlyTasks(@PathVariable Long elderlyId) {
        if (!userService.isElderlyUser(elderlyId)) {
            return Result.error("该用户不是老人用户");
        }
        List<Task> tasks = taskService.getMyTasks("elderly", elderlyId);
        List<TaskResponseDTO> dtoList = tasks.stream()
                .map(TaskResponseDTO::fromTask)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }

    /**
     * 获取志愿者接取的所有任务
     * GET /api/mini-program/volunteer/{volunteerId}/tasks
     */
    @GetMapping("/volunteer/{volunteerId}/tasks")
    public Result<List<TaskResponseDTO>> getVolunteerTasks(@PathVariable Long volunteerId) {
        if (!userService.isVolunteer(volunteerId) && !userService.isAdmin(volunteerId)) {
            return Result.error("该用户不是志愿者或管理员");
        }
        List<Task> tasks = taskService.getMyTasks("volunteer", volunteerId);
        List<TaskResponseDTO> dtoList = tasks.stream()
                .map(TaskResponseDTO::fromTask)
                .collect(Collectors.toList());
        return Result.success(dtoList);
    }

    /**
     * 获取附近可接取的任务（模拟地理位置）
     * GET /api/mini-program/tasks/nearby
     */
    @GetMapping("/tasks/nearby")
    public Result<List<TaskResponseDTO>> getNearbyTasks(
            @RequestParam(required = false, defaultValue = "0") double latitude,
            @RequestParam(required = false, defaultValue = "0") double longitude,
            @RequestParam(required = false, defaultValue = "10") int radius) {
        List<Task> pendingTasks = taskService.getTasks(0, null);

        // 坐标为 0 表示未提供位置，返回全部待处理任务
        if (latitude == 0 && longitude == 0) {
            List<TaskResponseDTO> dtoList = pendingTasks.stream()
                    .map(TaskResponseDTO::fromTask)
                    .collect(Collectors.toList());
            return Result.success(dtoList);
        }

        // 根据地理位置筛选附近任务
        double radiusMeters = radius * 1000.0; // km → m
        List<TaskResponseDTO> nearbyTasks = pendingTasks.stream()
                .filter(t -> t.getLatitude() != null && t.getLongitude() != null
                        && t.getLatitude() != 0 && t.getLongitude() != 0)
                .filter(t -> MapController.calculateDistanceMeters(latitude, longitude, t.getLatitude(), t.getLongitude()) <= radiusMeters)
                .map(TaskResponseDTO::fromTask)
                .collect(Collectors.toList());

        // 附近无任务时回退返回全部
        if (nearbyTasks.isEmpty()) {
            nearbyTasks = pendingTasks.stream()
                    .map(TaskResponseDTO::fromTask)
                    .collect(Collectors.toList());
        }
        return Result.success(nearbyTasks);
    }

    /**
     * 老人一键发布求助任务
     * POST /api/mini-program/elderly/{elderlyId}/quick-task
     */
    @PostMapping("/elderly/{elderlyId}/quick-task")
    public Result<TaskResponseDTO> createQuickTask(
            @PathVariable Long elderlyId,
            @RequestBody Map<String, String> requestBody) {
        if (!userService.isElderlyUser(elderlyId)) {
            return Result.error("只有老人才能发布快速求助任务");
        }

        String taskType = requestBody.get("taskType");
        String description = requestBody.get("description");

        Task task = new Task();
        task.setUserId(elderlyId);
        task.setType(taskType);
        task.setContent(description);
        task.setTitle(getTitleByTaskType(taskType));

        Task createdTask = taskService.createTask(task);
        TaskResponseDTO dto = TaskResponseDTO.fromTask(createdTask);
        return Result.success(dto);
    }

    /**
     * 老人简化版发布任务（适合老年人使用）
     * POST /api/mini-program/elderly/{elderlyId}/simple-task
     */
    @PostMapping("/elderly/{elderlyId}/simple-task")
    public Result<TaskResponseDTO> createSimpleTask(
            @PathVariable Long elderlyId,
            @RequestBody Map<String, Object> requestBody) {
        if (!userService.isElderlyUser(elderlyId)) {
            return Result.error("只有老人才能发布任务");
        }

        Integer taskType = Integer.valueOf(requestBody.get("taskType").toString());
        Task task = new Task();
        task.setUserId(elderlyId);

        // 根据数字选择任务类型，简化老人操作
        switch (taskType) {
            case 1:
                task.setType("sos");
                task.setTitle("紧急求助");
                task.setContent("老人紧急求助");
                break;
            case 2:
                task.setType("sos");
                task.setTitle("医疗求助");
                task.setContent("老人医疗求助");
                break;
            case 3:
                task.setType("life_service");
                task.setTitle("生活求助");
                task.setContent("老人生活求助");
                break;
            default:
                return Result.error("任务类型选择错误");
        }

        return Result.success(TaskResponseDTO.fromTask(taskService.createTask(task)));
    }

    /**
     * 获取老人绑定的设备
     * GET /api/mini-program/elderly/{elderlyId}/devices
     */
    @GetMapping("/elderly/{elderlyId}/devices")
    public Result<List<DeviceResponseDTO>> getElderlyDevices(@PathVariable Long elderlyId) {
        if (!userService.isElderlyUser(elderlyId)) {
            return Result.error("该用户不是老人用户");
        }
        List<Device> devices = deviceService.getDevicesByUserId(elderlyId);
        List<DeviceResponseDTO> dtoList = devices.stream()
                .map(DeviceResponseDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
        return Result.success(dtoList);
    }

    /**
     * 老人一键绑定设备
     * POST /api/mini-program/elderly/{elderlyId}/bind-device
     */
    @PostMapping("/elderly/{elderlyId}/bind-device")
    public Result<DeviceResponseDTO> bindDevice(@PathVariable Long elderlyId, @RequestParam String deviceId) {
        if (!userService.isElderlyUser(elderlyId)) {
            return Result.error("只有老人才能绑定设备");
        }
        Device device = deviceService.bindDeviceToUser(deviceId, elderlyId);
        return Result.success(DeviceResponseDTO.fromEntity(device));
    }

    // ==================== 内部类 ====================

    private static class VolunteerAcceptRequest {
        private Long volunteerId;

        public Long getVolunteerId() { return volunteerId; }
        public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据任务类型生成标题
     */
    private String getTitleByTaskType(String taskType) {
        if (taskType == null) return "求助任务";
        switch (taskType.toLowerCase()) {
            case "sos":          return "紧急求助";
            case "life_service": return "生活服务";
            case "consultation": return "日常咨询";
            default:             return "求助任务";
        }
    }
}
