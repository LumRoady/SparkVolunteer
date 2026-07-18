package com.spark.volunteer.service;

import com.spark.volunteer.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 评价服务接口
 * 定义评价相关的服务方法
 */
public interface FeedbackService {

    // 创建评价
    Feedback createFeedback(Feedback feedback);

    // 根据ID获取评价
    Feedback getFeedbackById(Long id);
    
    // 根据任务ID获取评价
    Feedback getFeedbackByTaskId(Long taskId);
    
    // 根据任务ID列表获取评价
    List<Feedback> getFeedbacksByTaskIds(List<Long> taskIds);

    // 分页获取所有评价
    Page<Feedback> getAllFeedbacks(Pageable pageable);

    // 获取所有评价
    List<Feedback> getAllFeedbacks();

    // 删除评价
    void deleteFeedback(Long id);
}