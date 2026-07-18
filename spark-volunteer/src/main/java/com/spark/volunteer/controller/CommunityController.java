/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 社区管理控制器
 * GET /api/community/stats → 各社区统计数据
 */
@RestController
@RequestMapping("/api/community")
public class CommunityController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> getCommunityStats() {
        List<User> all = userRepository.findAll();
        Map<String, List<User>> grouped = new LinkedHashMap<>();

        for (User u : all) {
            String c = u.getCommunity() != null ? u.getCommunity() : "未分配";
            grouped.computeIfAbsent(c, k -> new ArrayList<>()).add(u);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<User>> entry : grouped.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            int elderly = 0, volunteers = 0, admins = 0;
            for (User u : entry.getValue()) {
                switch (u.getRole()) {
                    case "ELDERLY":    elderly++;    break;
                    case "VOLUNTEER":  volunteers++;  break;
                    case "ADMIN":      admins++;      break;
                }
            }
            item.put("name", entry.getKey());
            item.put("elderly", elderly);
            item.put("volunteers", volunteers);
            item.put("admins", admins);
            item.put("total", elderly + volunteers + admins);
            result.add(item);
        }
        return Result.success(result);
    }
}
