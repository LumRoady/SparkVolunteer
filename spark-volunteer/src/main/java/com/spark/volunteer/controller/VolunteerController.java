/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import com.spark.volunteer.exception.NotFoundException;
import com.spark.volunteer.service.VolunteerGrowthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 志愿者成长与证书控制器
 */
@RestController
@RequestMapping("/api/volunteer")
public class VolunteerController {

    @Autowired
    private VolunteerGrowthService growthService;

    /**
     * 获取志愿者成长数据
     * GET /api/volunteer/{userId}/growth
     */
    @GetMapping("/{userId}/growth")
    public Result<Map<String, Object>> getGrowth(@PathVariable Long userId) {
        return Result.success(growthService.getGrowthData(userId));
    }

    /**
     * 获取电子证书数据
     * GET /api/certificate/{userId}
     */
    @GetMapping("/certificate/{userId}")
    public Result<Map<String, Object>> getCertificate(@PathVariable Long userId) {
        Map<String, Object> cert = growthService.getCertificateData(userId);
        if (cert == null) throw new NotFoundException("用户", userId);
        return Result.success(cert);
    }
}
