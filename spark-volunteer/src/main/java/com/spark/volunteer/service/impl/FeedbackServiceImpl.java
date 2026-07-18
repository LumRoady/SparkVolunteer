package com.spark.volunteer.service.impl;

import com.spark.volunteer.entity.Feedback;
import com.spark.volunteer.repository.FeedbackRepository;
import com.spark.volunteer.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 评价服务实现类
 * 实现评价相关的服务方法
 */
@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Override
    public Feedback createFeedback(Feedback feedback) {
        return feedbackRepository.save(feedback);
    }

    @Override
    public Feedback getFeedbackById(Long id) {
        return feedbackRepository.findById(id).orElse(null);
    }

    @Override
    public Feedback getFeedbackByTaskId(Long taskId) {
        return feedbackRepository.findByTaskId(taskId);
    }

    @Override
    public List<Feedback> getFeedbacksByTaskIds(List<Long> taskIds) {
        return feedbackRepository.findByTaskIdIn(taskIds);
    }

    @Override
    public Page<Feedback> getAllFeedbacks(Pageable pageable) {
        return feedbackRepository.findAll(pageable);
    }

    @Override
    public List<Feedback> getAllFeedbacks() {
        return feedbackRepository.findAll();
    }

    @Override
    public void deleteFeedback(Long id) {
        feedbackRepository.deleteById(id);
    }
}