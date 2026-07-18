/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import com.spark.volunteer.dto.TaskResponseDTO;
import com.spark.volunteer.dto.request.AcceptTaskRequest;
import com.spark.volunteer.dto.request.CreateTaskRequest;
import com.spark.volunteer.dto.request.UpdateTaskRequest;
import com.spark.volunteer.entity.Task;
import com.spark.volunteer.entity.TaskParticipant;
import com.spark.volunteer.service.TaskParticipantService;
import com.spark.volunteer.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RESTful风格的任务控制器 V2
 * 遵循RESTful API设计规范，路径统一使用 /api/v2/tasks
 */
@RestController
@RequestMapping("/api/v2/tasks")
public class TaskControllerV2 {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskParticipantService taskParticipantService;

    /**
     * 创建任务
     * POST /api/v2/tasks
     */
    @PostMapping
    public Result<TaskResponseDTO> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = request.toTask();
        Task createdTask = taskService.createTask(task);

        // 创建任务参与者记录（发起者）
        TaskParticipant participant = new TaskParticipant();
        participant.setTaskId(createdTask.getId());
        participant.setUserId(createdTask.getUserId());
        participant.setRole("REQUESTER");
        participant.setStatus(1);
        taskParticipantService.createParticipant(participant);

        TaskResponseDTO dto = TaskResponseDTO.fromTask(createdTask);
        return Result.success(dto);
    }

    /**
     * 获取任务列表（分页 + 筛选）
     * GET /api/v2/tasks?status=0&type=sos&page=0&size=10
     */
    @GetMapping
    public Result<Page<TaskResponseDTO>> getTasks(
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
     * GET /api/v2/tasks/{id}
     */
    @GetMapping("/{id}")
    public Result<TaskResponseDTO> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        TaskResponseDTO dto = TaskResponseDTO.fromTask(task);
        return Result.success(dto);
    }

    /**
     * 更新任务
     * PUT /api/v2/tasks/{id}
     */
    @PutMapping("/{id}")
    public Result<TaskResponseDTO> updateTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        Task task = request.toTask();
        task.setId(id);
        Task updatedTask = taskService.updateTask(id, task);
        TaskResponseDTO dto = TaskResponseDTO.fromTask(updatedTask);
        return Result.success(dto);
    }

    /**
     * 删除任务
     * DELETE /api/v2/tasks/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return Result.success();
    }

    /**
     * 志愿者接受任务
     * POST /api/v2/tasks/{id}/accept
     */
    @PostMapping("/{id}/accept")
    public Result<TaskResponseDTO> acceptTask(@PathVariable Long id, @RequestBody(required = false) AcceptTaskRequest request) {
        Long volunteerId = request != null ? request.getVolunteerId() : null;

        Task task = taskService.acceptTask(id, volunteerId);

        // 创建任务参与者记录（接收者）
        TaskParticipant participant = new TaskParticipant();
        participant.setTaskId(id);
        participant.setUserId(volunteerId);
        participant.setRole("RECEIVER");
        participant.setStatus(1);
        taskParticipantService.createParticipant(participant);

        TaskResponseDTO dto = TaskResponseDTO.fromTask(task);
        return Result.success(dto);
    }

    /**
     * 完成任务
     * POST /api/v2/tasks/{id}/complete
     */
    @PostMapping("/{id}/complete")
    public Result<TaskResponseDTO> completeTask(@PathVariable Long id) {
        Task task = taskService.completeTask(id);
        TaskResponseDTO dto = TaskResponseDTO.fromTask(task);
        return Result.success(dto);
    }

    /**
     * 取消任务
     * POST /api/v2/tasks/{id}/cancel
     */
    @PostMapping("/{id}/cancel")
    public Result<TaskResponseDTO> cancelTask(@PathVariable Long id) {
        Task task = taskService.cancelTask(id);
        TaskResponseDTO dto = TaskResponseDTO.fromTask(task);
        return Result.success(dto);
    }

    /**
     * 获取任务参与者
     * GET /api/v2/tasks/{id}/participants
     */
    @GetMapping("/{id}/participants")
    public Result<List<com.spark.volunteer.dto.TaskParticipantResponseDTO>> getTaskParticipants(@PathVariable Long id) {
        List<TaskParticipant> participants = taskParticipantService.getParticipantsByTaskId(id);
        List<com.spark.volunteer.dto.TaskParticipantResponseDTO> dtoList = participants.stream()
                .map(com.spark.volunteer.dto.TaskParticipantResponseDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
        return Result.success(dtoList);
    }

    /**
     * 获取用户的任务列表
     * GET /api/v2/tasks/user/{userId}?role=REQUESTER
     */
    @GetMapping("/user/{userId}")
    public Result<List<TaskResponseDTO>> getUserTasks(@PathVariable Long userId,
                                                    @RequestParam(required = false) String role) {
        List<TaskParticipant> participants;
        if (role != null) {
            participants = taskParticipantService.getParticipantsByUserIdAndRole(userId, role);
        } else {
            participants = taskParticipantService.getParticipantsByUserId(userId);
        }

        List<Long> taskIds = participants.stream()
                .map(TaskParticipant::getTaskId)
                .collect(Collectors.toList());

        List<Task> tasks = taskService.getTasksByIds(taskIds);
        List<TaskResponseDTO> dtoList = tasks.stream()
                .map(TaskResponseDTO::fromTask)
                .collect(Collectors.toList());

        return Result.success(dtoList);
    }
}
