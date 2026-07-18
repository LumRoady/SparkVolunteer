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
import com.spark.volunteer.dto.TaskChallengeResponseDTO;
import com.spark.volunteer.dto.request.CreateTaskChallengeRequest;
import com.spark.volunteer.dto.request.UpdateTaskChallengeRequest;
import com.spark.volunteer.entity.TaskChallenge;
import com.spark.volunteer.exception.NotFoundException;
import com.spark.volunteer.service.TaskChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 任务挑战控制器
 * 处理任务挑战相关的HTTP请求
 */
@RestController
@RequestMapping("/api/challenges")
public class TaskChallengeController {

    @Autowired
    private TaskChallengeService taskChallengeService;

    /**
     * 发布任务挑战
     * POST /api/challenges
     */
    @PostMapping
    public Result<TaskChallengeResponseDTO> createTaskChallenge(@Valid @RequestBody CreateTaskChallengeRequest request) {
        TaskChallenge taskChallenge = request.toTaskChallenge();
        TaskChallenge createdTaskChallenge = taskChallengeService.createTaskChallenge(taskChallenge);
        return Result.success(TaskChallengeResponseDTO.fromEntity(createdTaskChallenge));
    }

    /**
     * 获取任务挑战详情
     * GET /api/challenges/{id}
     */
    @GetMapping("/{id}")
    public Result<TaskChallengeResponseDTO> getTaskChallengeById(@PathVariable Long id) {
        TaskChallenge taskChallenge = taskChallengeService.getTaskChallengeById(id);
        if (taskChallenge == null) {
            throw new NotFoundException("任务挑战", id);
        }
        return Result.success(TaskChallengeResponseDTO.fromEntity(taskChallenge));
    }

    /**
     * 获取任务挑战列表（支持分页）
     * GET /api/challenges?userId=1&completed=false&page=0&size=10
     */
    @GetMapping
    public Result<PageResponse<TaskChallengeResponseDTO>> getTaskChallenges(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TaskChallenge> challengePage;
        if (userId != null && completed != null) {
            if (completed) {
                challengePage = taskChallengeService.getTaskChallengesByUserId(userId, pageable);
            } else {
                // 未完成的挑战量通常很小，用全量查询
                List<TaskChallenge> challenges = taskChallengeService.getUncompletedTaskChallengesByUserId(userId);
                PageResponse<TaskChallengeResponseDTO> pageResponse = PageResponse.fromList(
                        challenges.stream().map(TaskChallengeResponseDTO::fromEntity)
                                .collect(java.util.stream.Collectors.toList()), page, size);
                return Result.success(pageResponse);
            }
        } else if (userId != null) {
            challengePage = taskChallengeService.getTaskChallengesByUserId(userId, pageable);
        } else if (completed != null && !completed) {
            List<TaskChallenge> challenges = taskChallengeService.getUncompletedTaskChallenges();
            PageResponse<TaskChallengeResponseDTO> pageResponse = PageResponse.fromList(
                    challenges.stream().map(TaskChallengeResponseDTO::fromEntity)
                            .collect(java.util.stream.Collectors.toList()), page, size);
            return Result.success(pageResponse);
        } else {
            challengePage = taskChallengeService.getAllTaskChallenges(pageable);
        }
        PageResponse<TaskChallengeResponseDTO> pageResponse = PageResponse.of(
                challengePage, TaskChallengeResponseDTO::fromEntity);
        return Result.success(pageResponse);
    }

    /**
     * 更新任务挑战状态
     * PUT /api/challenges/{id}/status?completed=true
     */
    @PutMapping("/{id}/status")
    public Result<TaskChallengeResponseDTO> updateTaskChallengeStatus(
            @PathVariable Long id,
            @RequestParam Boolean completed) {
        TaskChallenge taskChallenge = taskChallengeService.updateTaskChallengeStatus(id, completed);
        if (taskChallenge == null) {
            throw new NotFoundException("任务挑战", id);
        }
        return Result.success(TaskChallengeResponseDTO.fromEntity(taskChallenge));
    }

    /**
     * 更新任务挑战
     * PUT /api/challenges/{id}
     */
    @PutMapping("/{id}")
    public Result<TaskChallengeResponseDTO> updateTaskChallenge(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskChallengeRequest request) {
        TaskChallenge taskChallenge = taskChallengeService.getTaskChallengeById(id);
        if (taskChallenge == null) {
            throw new NotFoundException("任务挑战", id);
        }
        taskChallenge.setTitle(request.getTitle());
        taskChallenge.setDescription(request.getDescription());
        taskChallenge.setReward(request.getReward());
        taskChallenge.setCompleted(request.getCompleted());
        taskChallenge.setContact(request.getContact());
        taskChallenge.setAddress(request.getAddress());
        TaskChallenge updatedTaskChallenge = taskChallengeService.updateTaskChallenge(id, taskChallenge);
        return Result.success(TaskChallengeResponseDTO.fromEntity(updatedTaskChallenge));
    }

    /**
     * 删除任务挑战
     * DELETE /api/challenges/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTaskChallenge(@PathVariable Long id) {
        taskChallengeService.deleteTaskChallenge(id);
        return Result.success();
    }
}
