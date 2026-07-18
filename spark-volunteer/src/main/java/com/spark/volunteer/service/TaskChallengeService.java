package com.spark.volunteer.service;

import com.spark.volunteer.entity.TaskChallenge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * 任务挑战服务接口
 * 定义任务挑战相关的服务方法
 */
public interface TaskChallengeService {

    // 创建任务挑战
    TaskChallenge createTaskChallenge(TaskChallenge taskChallenge);

    // 根据ID获取任务挑战
    TaskChallenge getTaskChallengeById(Long id);

    // 分页获取所有任务挑战
    Page<TaskChallenge> getAllTaskChallenges(Pageable pageable);

    // 获取所有任务挑战
    List<TaskChallenge> getAllTaskChallenges();

    // 分页获取用户的任务挑战
    Page<TaskChallenge> getTaskChallengesByUserId(Long userId, Pageable pageable);

    // 获取用户的任务挑战
    List<TaskChallenge> getTaskChallengesByUserId(Long userId);

    // 获取未完成的任务挑战
    List<TaskChallenge> getUncompletedTaskChallenges();

    // 获取用户未完成的任务挑战
    List<TaskChallenge> getUncompletedTaskChallengesByUserId(Long userId);

    // 更新任务挑战状态
    TaskChallenge updateTaskChallengeStatus(Long id, Boolean completed);

    // 更新任务挑战
    TaskChallenge updateTaskChallenge(Long id, TaskChallenge taskChallenge);

    // 删除任务挑战
    void deleteTaskChallenge(Long id);
}