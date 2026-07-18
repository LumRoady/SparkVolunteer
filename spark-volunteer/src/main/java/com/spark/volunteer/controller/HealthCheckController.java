/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一健康检查入口
 * 
 * GET /api/health → 返回服务健康状态（供 Docker / K8s / 监控系统使用）
 * 
 * 同时暴露 Actuator 原生: GET /actuator/health
 */
@RestController
public class HealthCheckController {

    @Autowired(required = false)
    private HealthEndpoint healthEndpoint;

    /**
     * 应用级健康检查
     */
    @GetMapping("/api/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("service", "Spark Volunteer Platform");
        result.put("version", "1.0.0");
        
        // 如果有 Actuator HealthEndpoint，纳入其状态
        if (healthEndpoint != null) {
            try {
                Status actuatorStatus = healthEndpoint.health().getStatus();
                result.put("actuator", actuatorStatus.getCode());
            } catch (Exception e) {
                result.put("actuator", "UNAVAILABLE");
            }
        }
        
        result.put("timestamp", System.currentTimeMillis());
        return Result.success(result);
    }
}
