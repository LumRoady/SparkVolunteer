package com.spark.volunteer.repository;

import com.spark.volunteer.entity.TaskChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务挑战数据访问接口
 * 继承JpaRepository，自动拥有CRUD功能
 */
@Repository
public interface TaskChallengeRepository extends JpaRepository<TaskChallenge, Long> {

    // 根据用户ID查找任务挑战
    List<TaskChallenge> findByUserId(Long userId);
    org.springframework.data.domain.Page<TaskChallenge> findByUserId(Long userId, org.springframework.data.domain.Pageable pageable);

    // 根据完成状态查找任务挑战
    List<TaskChallenge> findByCompleted(Boolean completed);

    // 根据用户ID和完成状态查找任务挑战
    List<TaskChallenge> findByUserIdAndCompleted(Long userId, Boolean completed);
}