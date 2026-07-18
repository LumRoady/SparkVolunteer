/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import com.spark.volunteer.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 数据大屏控制器
 * 提供实时统计数据，用于大屏展示
 * Controller 仅负责参数接收和响应包装，业务逻辑委托给 DashboardService
 */
@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "数据大屏", description = "实时统计数据接口")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * 数据大屏统计接口
     * GET /api/dashboard/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "获取数据大屏统计数据", description = "返回今日求助数、响应率、在线志愿者等核心指标")
    public Result<Map<String, Object>> getStats() {
        return Result.success(dashboardService.getStats());
    }
}
