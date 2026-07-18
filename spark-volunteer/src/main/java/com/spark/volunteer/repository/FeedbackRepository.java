package com.spark.volunteer.repository;

import com.spark.volunteer.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 反馈数据访问接口
 * 继承JpaRepository，自动拥有CRUD功能
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // 根据任务ID查找评价
    Feedback findByTaskId(Long taskId);
    
    // 根据任务ID列表查找评价
    List<Feedback> findByTaskIdIn(List<Long> taskIds);
}