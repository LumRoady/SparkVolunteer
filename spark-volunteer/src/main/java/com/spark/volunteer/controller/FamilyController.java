/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.Result;
import com.spark.volunteer.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 亲属绑定控制器
 * 管理老人与子女的绑定关系，用于紧急求助通知
 */
@RestController
@RequestMapping("/api/family")
public class FamilyController {

    @Autowired
    private FamilyService familyService;

    /**
     * 绑定亲属关系（子女绑定老人）
     * POST /api/family/bind
     * body: { familyUserId, elderlyUserId, relation }
     */
    @PostMapping("/bind")
    public Result<Map<String, Object>> bindFamily(@RequestBody Map<String, Object> body) {
        Long familyUserId = Long.valueOf(body.get("familyUserId").toString());
        Long elderlyUserId = Long.valueOf(body.get("elderlyUserId").toString());
        String relation = (String) body.getOrDefault("relation", "子女");
        return Result.success(familyService.bindFamily(familyUserId, elderlyUserId, relation));
    }

    /**
     * 解绑亲属关系
     * POST /api/family/unbind
     */
    @PostMapping("/unbind")
    public Result<String> unbindFamily(@RequestBody Map<String, Object> body) {
        Long familyUserId = Long.valueOf(body.get("familyUserId").toString());
        familyService.unbindFamily(familyUserId);
        return Result.success("解绑成功");
    }

    /**
     * 获取老人的所有亲属
     * GET /api/family/elderly/{elderlyId}/members
     */
    @GetMapping("/elderly/{elderlyId}/members")
    public Result<List<Map<String, Object>>> getFamilyMembers(@PathVariable Long elderlyId) {
        return Result.success(familyService.getFamilyMembers(elderlyId));
    }

    /**
     * 更新微信openid（用于接收模板消息）
     * PUT /api/family/{userId}/wechat-openid
     */
    @PutMapping("/{userId}/wechat-openid")
    public Result<String> updateWechatOpenid(@PathVariable Long userId,
                                             @RequestBody Map<String, String> body) {
        familyService.updateWechatOpenid(userId, body.get("openid"));
        return Result.success("更新成功");
    }
}
