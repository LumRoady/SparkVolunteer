/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import com.spark.volunteer.service.RankingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 排行榜控制器
 * GET /api/ranking?type=accept     → 接单榜
 * GET /api/ranking?type=rating     → 好评榜
 * GET /api/ranking?type=points     → 志愿累计榜
 */
@RestController
@RequestMapping("/api")
public class RankingController {

    @Autowired
    private RankingService rankingService;

    @GetMapping("/ranking")
    public Result<List<Map<String, Object>>> getRanking(@RequestParam(defaultValue = "accept") String type) {
        return Result.success(rankingService.getRanking(type));
    }
}
