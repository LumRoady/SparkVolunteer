/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import com.spark.volunteer.config.WebSocketMessageBridge;
import com.spark.volunteer.dto.TaskResponseDTO;
import com.spark.volunteer.dto.request.CreateTaskRequest;
import com.spark.volunteer.entity.Task;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.service.TaskService;
import com.spark.volunteer.service.WechatNotifyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 紧急求助控制器
 * 专为空巢老人小程序和嵌入式遥控器（ESP32）设计
 *
 * 支持物理按钮触发：
 * - 红色按钮：紧急求助（sos）
 * - 黄色按钮：生活求助（life_service）
 * - 绿色按钮：日常咨询（consultation）
 */
@RestController
@RequestMapping("/api/emergency")
public class EmergencyController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private WechatNotifyService wechatNotifyService;

    @Autowired
    private WebSocketMessageBridge messageBridge;

    /**
     * 紧急求助任务创建
     * POST /api/emergency/task
     */
    @PostMapping("/task")
    public Result<TaskResponseDTO> createEmergencyTask(@RequestBody CreateTaskRequest request) {
        Task emergencyTask = taskService.createTask(request.toTask());

        // 紧急求助(sos)异步通知亲属
        if ("sos".equals(emergencyTask.getType())) {
            wechatNotifyService.sendEmergencyNotify(emergencyTask);
        }

        TaskResponseDTO dto = TaskResponseDTO.fromTask(emergencyTask);
        broadcastNewTask(dto);
        return Result.success(dto);
    }

    /**
     * 统一按钮接口 - 支持多种按钮类型
     * GET/POST /api/emergency/button/{buttonType}?requesterId=1&deviceId=xxx
     *
     * 支持的按钮类型：red/yellow/green/emergency/life_service/consultation
     *
     * 参数校验：
     * - requesterId：求助者ID，必填且必须大于0；若为空且 deviceId 也未提供则返回400
     * - deviceId：设备ID，与 requesterId 至少提供一个
     *
     * ESP32 调用示例：
     * POST http://<server-address>:8084/api/emergency/button/red?requesterId=1
     */
    @Valid
    @RequestMapping(value = "/button/{buttonType}", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<TaskResponseDTO> buttonHelp(@PathVariable String buttonType,
                                 @RequestParam(required = false) Long requesterId,
                                 @RequestParam(required = false) String deviceId) {
        // 参数校验：requesterId 为空或无效时拒绝请求，防止匿名创建任务
        if (requesterId == null || requesterId <= 0) {
            if (deviceId == null || deviceId.trim().isEmpty()) {
                throw new BusinessException(400, "设备ID和求助者ID至少需要提供一个");
            }
            throw new BusinessException(400, "求助者ID不能为空");
        }

        Task task = new Task();
        task.setUserId(requesterId);
        task.setRequesterId(requesterId);
        task.setDeviceId(deviceId);

        // 根据按钮类型设置任务信息
        switch (buttonType.toLowerCase()) {
            case "red":
            case "emergency":
                task.setTitle("紧急求助");
                task.setContent("老人按下红色紧急求助按钮");
                task.setType("sos");
                break;
            case "yellow":
            case "life_service":
                task.setTitle("生活求助");
                task.setContent("老人按下黄色生活求助按钮");
                task.setType("life_service");
                break;
            case "green":
            case "consultation":
                task.setTitle("日常咨询");
                task.setContent("老人按下绿色咨询求助按钮");
                task.setType("consultation");
                break;
            default:
                throw new BusinessException("不支持的按钮类型");
        }

        Task createdTask = taskService.createTask(task);

        TaskResponseDTO dto = TaskResponseDTO.fromTask(createdTask);
        broadcastNewTask(dto);

        return Result.success(dto);
    }

    /**
     * 广播新任务消息到 WebSocket
     */
    private void broadcastNewTask(TaskResponseDTO dto) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "new_task");
        message.put("data", dto);
        message.put("timestamp", System.currentTimeMillis());
        messageBridge.broadcast(message);
    }
}
