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
import com.spark.volunteer.dto.FeedbackResponseDTO;
import com.spark.volunteer.dto.request.CreateFeedbackRequest;
import com.spark.volunteer.entity.Feedback;
import com.spark.volunteer.exception.NotFoundException;
import com.spark.volunteer.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评价控制器
 * 处理评价相关的HTTP请求
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    /**
     * 提交评价
     * POST /api/feedback/create
     */
    @PostMapping("/create")
    public Result<FeedbackResponseDTO> createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        Feedback feedback = request.toFeedback();
        Feedback createdFeedback = feedbackService.createFeedback(feedback);
        return Result.success(FeedbackResponseDTO.fromEntity(createdFeedback));
    }

    /**
     * 获取评价详情
     * GET /api/feedback/{id}
     */
    @GetMapping("/{id}")
    public Result<FeedbackResponseDTO> getFeedbackById(@PathVariable Long id) {
        Feedback feedback = feedbackService.getFeedbackById(id);
        if (feedback == null) {
            throw new NotFoundException("评价", id);
        }
        return Result.success(FeedbackResponseDTO.fromEntity(feedback));
    }

    /**
     * 根据任务ID获取评价
     * GET /api/feedback/task/{taskId}
     */
    @GetMapping("/task/{taskId}")
    public Result<FeedbackResponseDTO> getFeedbackByTaskId(@PathVariable Long taskId) {
        Feedback feedback = feedbackService.getFeedbackByTaskId(taskId);
        if (feedback == null) {
            throw new NotFoundException("评价", taskId);
        }
        return Result.success(FeedbackResponseDTO.fromEntity(feedback));
    }

    /**
     * 获取评价列表（支持分页）
     * GET /api/feedback/list?page=0&size=10
     */
    @GetMapping("/list")
    public Result<PageResponse<FeedbackResponseDTO>> getAllFeedbacks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createTime").descending());
        Page<Feedback> feedbackPage = feedbackService.getAllFeedbacks(pageable);
        PageResponse<FeedbackResponseDTO> pageResponse = PageResponse.of(
                feedbackPage, FeedbackResponseDTO::fromEntity);
        return Result.success(pageResponse);
    }

    /**
     * 删除评价
     * DELETE /api/feedback/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        return Result.success();
    }
}
