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
import com.spark.volunteer.dto.TaskResponseDTO;
import com.spark.volunteer.dto.request.AcceptTaskRequest;
import com.spark.volunteer.dto.request.CreateTaskRequest;
import com.spark.volunteer.dto.request.UpdateTaskStatusRequest;
import com.spark.volunteer.entity.Task;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.service.TaskService;
import com.spark.volunteer.service.RedisCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务控制器（V1 — 计划废弃）
 *
 * 处理任务相关的 HTTP 请求。
 * 本 Controller 为 V1 版本，大部分接口已有 V2 替代：
 *   - 任务 CRUD → TaskControllerV2 (/api/v2/tasks)
 *   - 任务参与者 → TaskControllerV2
 *   - 用户任务查询 → TaskControllerV2
 *
 * V1 保留接口（V2 暂未覆盖）：
 *   - POST /api/task/report    (ESP32 设备上报)
 *   - GET  /api/task/no/{no}   (前端按编号查询)
 *   - GET  /api/task/list      (小程序任务列表)
 *
 * 迁移计划：下一大版本将 V1 全部迁移到 V2，届时删除本 Controller。
 *
 * 注意：所有异常由 GlobalExceptionHandler 统一处理，
 * Controller 层不再使用 try-catch 包裹
 */
@RestController
@RequestMapping("/api")
@Tag(name = "任务管理 V1", description = "任务创建、接单、完成、状态更新等接口（计划迁移至 V2）")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private RedisCacheService redisCacheService;

    /**
     * 获取任务状态
     * GET /api/task/{taskId}/status
     */
    @GetMapping("/task/{taskId}/status")
    public Result<Integer> getTaskStatus(@PathVariable("taskId") Long taskId) {
        Task task = taskService.getTaskById(taskId);
        return Result.success(task.getStatus());
    }

    /**
     * 硬件任务上报接口（ESP32 设备专用）
     * POST /api/task/report
     * 参数：deviceId, button, timestamp
     */
    @PostMapping("/task/report")
    public Result<TaskResponseDTO> reportTask(@RequestBody Map<String, Object> params) {
        String deviceId = (String) params.get("deviceId");
        Integer button = (Integer) params.get("button");
        Task task = taskService.createTaskByDevice(deviceId, button);
        TaskResponseDTO dto = TaskResponseDTO.fromTask(task);
        return Result.success(dto);
    }

    /**
     * 老人手动发布任务（幂等接口）
     * POST /api/task/create
     * 支持请求头 Idempotency-Key 去重，无 Key 时按 userId+title+type 组合去重
     */
    @PostMapping("/task/create")
    @Operation(summary = "创建任务（幂等）", description = "支持 Idempotency-Key 请求头去重，60秒内相同请求不会重复创建")
    public Result<TaskResponseDTO> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        // 幂等校验：防止网络重试导致重复提交
        String key = (idempotencyKey != null && !idempotencyKey.isEmpty())
                ? idempotencyKey
                : request.getUserId() + ":" + request.getTitle() + ":" + request.getType();
        if (!redisCacheService.tryAcquireIdempotency(key, 60)) {
            throw new BusinessException(409, "请勿重复提交，任务正在创建中");
        }
        try {
            Task task = request.toTask();
            Task createdTask = taskService.createTask(task);
            return Result.success(TaskResponseDTO.fromTask(createdTask));
        } catch (Exception e) {
            redisCacheService.releaseIdempotency(key);
            throw e;
        }
    }

    /**
     * 志愿者接单
     * POST /api/task/accept/{taskId}
     */
    @PostMapping("/task/accept/{taskId}")
    public Result<TaskResponseDTO> acceptTask(@PathVariable Long taskId,
                                            @RequestBody(required = false) AcceptTaskRequest request) {
        Long volunteerId = request != null ? request.getVolunteerId() : null;
        Task task = taskService.acceptTask(taskId, volunteerId);
        return Result.success(TaskResponseDTO.fromTask(task));
    }

    /**
     * 志愿者完成任务
     * POST /api/task/complete/{taskId}
     */
    @PostMapping("/task/complete/{taskId}")
    public Result<TaskResponseDTO> completeTask(@PathVariable Long taskId) {
        Task task = taskService.completeTask(taskId);
        return Result.success(TaskResponseDTO.fromTask(task));
    }

    /**
     * 取消任务
     * POST /api/task/cancel/{taskId}
     */
    @PostMapping("/task/cancel/{taskId}")
    public Result<TaskResponseDTO> cancelTask(@PathVariable Long taskId) {
        Task task = taskService.cancelTask(taskId);
        return Result.success(TaskResponseDTO.fromTask(task));
    }

    /**
     * 获取任务列表（支持按状态、类型筛选，分页）
     * GET /api/task/list?status=0&type=sos&page=0&size=10
     */
    @GetMapping("/task/list")
    public Result<PageResponse<TaskResponseDTO>> getTasks(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Task> taskPage = taskService.getTasksPage(status, type, pageable);
        PageResponse<TaskResponseDTO> pageResponse = PageResponse.of(
                taskPage, TaskResponseDTO::fromTask);
        return Result.success(pageResponse);
    }

    /**
     * 分页获取任务列表（支持筛选）
     * GET /api/task/tasks-page?page=0&size=10&status=0&type=sos
     */
    @GetMapping("/task/tasks-page")
    public Result<Page<TaskResponseDTO>> getTasksPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String type) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Task> tasks = taskService.getTasksPage(status, type, pageable);
        Page<TaskResponseDTO> dtoPage = tasks.map(TaskResponseDTO::fromTask);
        return Result.success(dtoPage);
    }

    /**
     * 获取任务详情
     * GET /api/task/detail/{taskId}
     */
    @GetMapping("/task/detail/{taskId}")
    public Result<TaskResponseDTO> getTaskById(@PathVariable Long taskId) {
        Task task = taskService.getTaskById(taskId);
        TaskResponseDTO dto = TaskResponseDTO.fromTask(task);
        return Result.success(dto);
    }

    /**
     * 获取任务详情（通过taskNo——用于前端展示序号）
     * GET /api/task/no/{taskNo}
     */
    @GetMapping("/task/no/{taskNo}")
    public Result<TaskResponseDTO> getTaskByTaskNo(@PathVariable Integer taskNo) {
        Task task = taskService.getTaskByTaskNo(taskNo);
        return Result.success(TaskResponseDTO.fromTask(task));
    }

    /**
     * 获取任务详情（便捷路径，等同于 /api/task/detail/{id}）
     * GET /api/tasks/{id}
     */
    @GetMapping("/tasks/{id}")
    public Result<TaskResponseDTO> getTaskDetailByShortPath(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return Result.success(TaskResponseDTO.fromTask(task));
    }

    /**
     * 更新任务状态（推荐使用此接口）
     * POST /api/task/updateStatus
     */
    @PostMapping("/task/updateStatus")
    @Operation(summary = "更新任务状态", description = "支持 PENDING/ACCEPTED/COMPLETED/CANCELLED 状态转换")
    public Result<TaskResponseDTO> updateTaskStatus(@RequestBody UpdateTaskStatusRequest request) {
        if (request.getTaskId() == null) {
            throw new BusinessException(400, "任务ID不能为空");
        }
        if (request.getNewStatus() == null || request.getNewStatus().trim().isEmpty()) {
            throw new BusinessException(400, "新状态不能为空");
        }

        Integer status = mapStatusStringToInt(request.getNewStatus());
        if (status == null) {
            throw new BusinessException(400, "无效的任务状态");
        }

        Task task = taskService.updateTaskStatus(request.getTaskId(), status, request.getVolunteerId());
        TaskResponseDTO dto = TaskResponseDTO.fromTask(task);
        return Result.success(dto);
    }

    /**
     * 获取我的任务（支持分页）
     * GET /api/task/my?role=elderly&userId=1&page=0&size=10
     */
    @GetMapping("/task/my")
    public Result<PageResponse<TaskResponseDTO>> getMyTasks(
            @RequestParam String role,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Task> tasks = taskService.getMyTasks(role, userId);
        PageResponse<TaskResponseDTO> pageResponse = PageResponse.fromList(
                tasks.stream().map(TaskResponseDTO::fromTask).collect(Collectors.toList()), page, size);
        return Result.success(pageResponse);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 将字符串状态转换为整数
     */
    private Integer mapStatusStringToInt(String statusStr) {
        if (statusStr == null) return null;
        switch (statusStr.toUpperCase()) {
            case "PENDING":   return 0;
            case "ACCEPTED":  return 1;
            case "COMPLETED": return 2;
            case "CANCELLED": return 3;
            default:          return null;
        }
    }
}
