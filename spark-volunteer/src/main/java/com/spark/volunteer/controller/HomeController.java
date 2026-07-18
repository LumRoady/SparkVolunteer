/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "星火志愿服务平台 API 已启动";
    }

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello from backend! 后端服务运行正常!");
    }

    @GetMapping("/location")
    public Result<Map<String, String>> location() {
        Map<String, String> result = new LinkedHashMap<>();
        // ip-api.com 有频率限制，设为空由前端兜底
        result.put("regionName", "");
        result.put("city", "");
        return Result.success(result);
    }
}
