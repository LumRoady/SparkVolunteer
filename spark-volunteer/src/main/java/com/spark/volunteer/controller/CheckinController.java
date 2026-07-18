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
import com.spark.volunteer.dto.CheckinResponseDTO;
import com.spark.volunteer.entity.Checkin;
import com.spark.volunteer.service.CheckinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 签到控制器
 * 处理签到相关的HTTP请求
 */
@RestController
@RequestMapping("/api/checkins")
public class CheckinController {

    @Autowired
    private CheckinService checkinService;

    /**
     * 用户签到
     * POST /api/checkins?userId=1
     */
    @PostMapping
    public Result<CheckinResponseDTO> checkin(@RequestParam Long userId) {
        Checkin checkin = checkinService.checkin(userId);
        return Result.success(CheckinResponseDTO.fromEntity(checkin));
    }

    /**
     * 获取签到记录详情
     * GET /api/checkins/{id}
     */
    @GetMapping("/{id}")
    public Result<CheckinResponseDTO> getCheckinById(@PathVariable Long id) {
        Checkin checkin = checkinService.getCheckinById(id);
        if (checkin == null) {
            return Result.error("签到记录不存在");
        }
        return Result.success(CheckinResponseDTO.fromEntity(checkin));
    }

    /**
     * 获取用户的签到记录（支持分页）
     * GET /api/checkins?userId=1&page=0&size=10
     */
    @GetMapping
    public Result<PageResponse<CheckinResponseDTO>> getCheckins(
            @RequestParam Long userId,
            @RequestParam(required = false) Date startDate,
            @RequestParam(required = false) Date endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("checkinDate").descending());
        Page<Checkin> checkinPage;
        if (startDate != null && endDate != null) {
            // 日期范围查询暂保留全量模式
            List<Checkin> checkins = checkinService.getCheckinsByUserIdAndDateRange(userId, startDate, endDate);
            PageResponse<CheckinResponseDTO> pageResponse = PageResponse.fromList(
                    checkins.stream().map(CheckinResponseDTO::fromEntity).collect(Collectors.toList()), page, size);
            return Result.success(pageResponse);
        } else {
            checkinPage = checkinService.getCheckinsByUserId(userId, pageable);
        }
        PageResponse<CheckinResponseDTO> pageResponse = PageResponse.of(
                checkinPage, CheckinResponseDTO::fromEntity);
        return Result.success(pageResponse);
    }

    /**
     * 检查用户今天是否已签到
     * GET /api/checkins/today?userId=1
     */
    @GetMapping("/today")
    public Result<Boolean> hasCheckedInToday(@RequestParam Long userId) {
        boolean hasCheckedIn = checkinService.hasCheckedInToday(userId);
        return Result.success(hasCheckedIn);
    }

    /**
     * 获取用户的连续签到天数
     * GET /api/checkins/streak?userId=1
     */
    @GetMapping("/streak")
    public Result<Integer> getCheckinStreak(@RequestParam Long userId) {
        int streak = checkinService.getCheckinStreak(userId);
        return Result.success(streak);
    }

    /**
     * 获取用户的总签到次数
     * GET /api/checkins/total?userId=1
     */
    @GetMapping("/total")
    public Result<Long> getTotalCheckins(@RequestParam Long userId) {
        long total = checkinService.getTotalCheckins(userId);
        return Result.success(total);
    }
}
