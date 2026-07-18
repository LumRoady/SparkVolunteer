/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import com.spark.volunteer.dto.UserResponseDTO;
import com.spark.volunteer.dto.request.CreateUserRequest;
import com.spark.volunteer.dto.request.LoginByCodeRequest;
import com.spark.volunteer.dto.request.UpdateUserRequest;
import com.spark.volunteer.entity.Task;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.exception.ForbiddenException;
import com.spark.volunteer.service.TaskService;
import com.spark.volunteer.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private com.spark.volunteer.service.DeviceService deviceService;

    @Autowired
    private com.spark.volunteer.repository.AchievementRepository achievementRepository;

    /**
     * 更新用户信息
     * PUT /api/users/update
     */
    @PutMapping("/update")
    public Result<UserResponseDTO> updateUser(@Valid @RequestBody UpdateUserRequest request) {
        verifyOwnDataOrAdmin(request.getId());
        User user = userService.getUserById(request.getId());
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setPhone(request.getPhone());
        user.setCommunity(request.getCommunity());
        User updatedUser = userService.updateUser(user);
        return Result.success(UserResponseDTO.fromEntity(updatedUser));
    }

    /**
     * 获取用户列表（支持按角色筛选）
     * GET /api/users?role=VOLUNTEER
     */
    @GetMapping
    public Result<java.util.List<UserResponseDTO>> getUsers(@RequestParam(required = false) String role) {
        java.util.List<User> users;
        if (role != null && !role.isEmpty()) {
            users = userService.getUsersByRole(role);
        } else {
            users = userService.getAllUsers();
        }
        java.util.List<UserResponseDTO> dtoList = users.stream()
                .map(UserResponseDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
        return Result.success(dtoList);
    }

    /**
     * 获取用户信息
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    public Result<UserResponseDTO> getUserById(@PathVariable Long id) {
        verifyOwnDataOrAdmin(id);
        User user = userService.getUserById(id);
        return Result.success(UserResponseDTO.fromEntity(user));
    }

    /**
     * 获取用户历史求助记录
     * GET /api/users/{id}/history
     */
    @GetMapping("/{id}/history")
    public Result<Page<com.spark.volunteer.dto.TaskResponseDTO>> getUserHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        verifyOwnDataOrAdmin(id);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Task> tasks = taskService.getTasksPageByUserId(id, pageable);
        Page<com.spark.volunteer.dto.TaskResponseDTO> dtoPage = tasks.map(com.spark.volunteer.dto.TaskResponseDTO::fromTask);
        return Result.success(dtoPage);
    }

    /**
     * 获取用户统计数据
     * GET /api/users/{id}/stats
     */
    @GetMapping("/{id}/stats")
    public Result<java.util.Map<String, Object>> getUserStats(@PathVariable Long id) {
        verifyOwnDataOrAdmin(id);
        User user = userService.getUserById(id);
        java.util.Map<String, Object> stats = new java.util.LinkedHashMap<>();
        stats.put("points", user.getPoints() != null ? user.getPoints() : 0);
        stats.put("completedTasks", user.getCompletedTasks() != null ? user.getCompletedTasks() : 0);
        stats.put("checkinStreak", user.getCheckinStreak() != null ? user.getCheckinStreak() : 0);
        stats.put("level", getLevel(user.getPoints() != null ? user.getPoints() : 0));
        return Result.success(stats);
    }

    /**
     * 获取用户参与的任务
     * GET /api/users/{id}/participated
     */
    @GetMapping("/{id}/participated")
    public Result<Page<com.spark.volunteer.dto.TaskResponseDTO>> getUserParticipated(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        verifyOwnDataOrAdmin(id);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Task> tasks = taskService.getTasksPageByUserId(id, pageable);
        Page<com.spark.volunteer.dto.TaskResponseDTO> dtoPage = tasks.map(com.spark.volunteer.dto.TaskResponseDTO::fromTask);
        return Result.success(dtoPage);
    }

    /**
     * 获取用户成就
     * GET /api/users/{id}/achievements
     */
    @GetMapping("/{id}/achievements")
    public Result<java.util.List<java.util.Map<String, Object>>> getUserAchievements(@PathVariable Long id) {
        verifyOwnDataOrAdmin(id);

        // 查询用户已解锁的成就
        java.util.List<com.spark.volunteer.entity.Achievement> unlocked = achievementRepository.findByUserId(id);
        java.util.Set<String> unlockedTypes = unlocked.stream()
                .map(com.spark.volunteer.entity.Achievement::getAchievementType)
                .collect(java.util.stream.Collectors.toSet());

        // 全部成就定义
        String[] types = {"first_task", "hundred", "emergency_10", "night_guard", "fast_response", "streak_7"};
        String[] titles = {"首次接单", "累计百单", "紧急响应", "夜间守护", "极速响应", "连续签到7天"};
        String[] icons = {"🌟", "📋", "🚨", "🌙", "⚡", "🔥"};

        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        for (int i = 0; i < types.length; i++) {
            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
            item.put("type", types[i]);
            item.put("title", titles[i]);
            item.put("icon", icons[i]);
            item.put("unlocked", unlockedTypes.contains(types[i]));
            list.add(item);
        }
        return Result.success(list);
    }

    /**
     * 获取用户档案（profile 别名）
     * GET /api/users/{id}/profile
     */
    @GetMapping("/{id}/profile")
    public Result<UserResponseDTO> getUserProfile(@PathVariable Long id) {
        return getUserById(id);  // getUserById 内部已调用 verifyOwnDataOrAdmin
    }

    /**
     * 更新用户（RESTful 风格）
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    public Result<UserResponseDTO> updateUserById(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        verifyOwnDataOrAdmin(id);
        request.setId(id);
        return updateUser(request);
    }

    /**
     * 获取用户证书
     * GET /api/users/{id}/certificates
     */
    @GetMapping("/{id}/certificates")
    public Result<java.util.Map<String, Object>> getUserCertificates(@PathVariable Long id) {
        verifyOwnDataOrAdmin(id);
        User user = userService.getUserById(id);
        java.util.Map<String, Object> cert = new java.util.LinkedHashMap<>();
        cert.put("volunteerName", user.getName() != null ? user.getName() : user.getNickname());
        cert.put("totalServices", user.getCompletedTasks() != null ? user.getCompletedTasks() : 0);
        cert.put("points", user.getPoints() != null ? user.getPoints() : 0);
        cert.put("level", getLevel(user.getPoints() != null ? user.getPoints() : 0));
        cert.put("joinDate", user.getCreateTime() != null
                ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(user.getCreateTime())
                : "-");
        cert.put("certNumber", "XHZC-2026-" + String.format("%05d", id));
        return Result.success(cert);
    }

    /**
     * 获取用户绑定设备列表
     * GET /api/users/{id}/devices
     */
    @GetMapping("/{id}/devices")
    public Result<java.util.List<com.spark.volunteer.dto.DeviceResponseDTO>> getUserDevices(@PathVariable Long id) {
        verifyOwnDataOrAdmin(id);
        java.util.List<com.spark.volunteer.entity.Device> devices = deviceService.getDevicesByUserId(id);
        java.util.List<com.spark.volunteer.dto.DeviceResponseDTO> dtoList;
        if (devices != null) {
            dtoList = devices.stream()
                    .map(com.spark.volunteer.dto.DeviceResponseDTO::fromEntity)
                    .collect(java.util.stream.Collectors.toList());
        } else {
            dtoList = java.util.Collections.emptyList();
        }
        return Result.success(dtoList);
    }

    /**
     * 校验当前用户只能访问自己的数据，管理员可访问所有
     */
    private void verifyOwnDataOrAdmin(Long pathUserId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return;
        String currentUsername = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));
        if (!isAdmin) {
            User currentUser = userService.getUserByUsername(currentUsername);
            if (!currentUser.getId().equals(pathUserId)) {
                throw new ForbiddenException("只能访问自己的数据");
            }
        }
    }

    private int getLevel(int points) {
        if (points >= 1000) return 4;
        if (points >= 300) return 3;
        if (points >= 100) return 2;
        return 1;
    }
}
