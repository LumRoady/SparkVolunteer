package com.spark.volunteer.service.impl;

import com.spark.volunteer.entity.TaskChallenge;
import com.spark.volunteer.repository.TaskChallengeRepository;
import com.spark.volunteer.service.TaskChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 任务挑战服务实现类
 * 实现任务挑战相关的服务方法
 */
@Service
public class TaskChallengeServiceImpl implements TaskChallengeService {

    @Autowired
    private TaskChallengeRepository taskChallengeRepository;

    @Override
    public TaskChallenge createTaskChallenge(TaskChallenge taskChallenge) {
        return taskChallengeRepository.save(taskChallenge);
    }

    @Override
    public TaskChallenge getTaskChallengeById(Long id) {
        return taskChallengeRepository.findById(id).orElse(null);
    }

    @Override
    public Page<TaskChallenge> getAllTaskChallenges(Pageable pageable) {
        return taskChallengeRepository.findAll(pageable);
    }

    @Override
    public List<TaskChallenge> getAllTaskChallenges() {
        return taskChallengeRepository.findAll();
    }

    @Override
    public Page<TaskChallenge> getTaskChallengesByUserId(Long userId, Pageable pageable) {
        return taskChallengeRepository.findByUserId(userId, pageable);
    }

    @Override
    public List<TaskChallenge> getTaskChallengesByUserId(Long userId) {
        return taskChallengeRepository.findByUserId(userId);
    }

    @Override
    public List<TaskChallenge> getUncompletedTaskChallenges() {
        return taskChallengeRepository.findByCompleted(false);
    }

    @Override
    public List<TaskChallenge> getUncompletedTaskChallengesByUserId(Long userId) {
        return taskChallengeRepository.findByUserIdAndCompleted(userId, false);
    }

    @Override
    public TaskChallenge updateTaskChallengeStatus(Long id, Boolean completed) {
        TaskChallenge taskChallenge = taskChallengeRepository.findById(id).orElse(null);
        if (taskChallenge != null) {
            taskChallenge.setCompleted(completed);
            return taskChallengeRepository.save(taskChallenge);
        }
        return null;
    }

    @Override
    public TaskChallenge updateTaskChallenge(Long id, TaskChallenge taskChallenge) {
        TaskChallenge existingTaskChallenge = taskChallengeRepository.findById(id).orElse(null);
        if (existingTaskChallenge != null) {
            if (taskChallenge.getTitle() != null) {
                existingTaskChallenge.setTitle(taskChallenge.getTitle());
            }
            if (taskChallenge.getDescription() != null) {
                existingTaskChallenge.setDescription(taskChallenge.getDescription());
            }
            if (taskChallenge.getReward() != null) {
                existingTaskChallenge.setReward(taskChallenge.getReward());
            }
            if (taskChallenge.getContact() != null) {
                existingTaskChallenge.setContact(taskChallenge.getContact());
            }
            if (taskChallenge.getAddress() != null) {
                existingTaskChallenge.setAddress(taskChallenge.getAddress());
            }
            return taskChallengeRepository.save(existingTaskChallenge);
        }
        return null;
    }

    @Override
    public void deleteTaskChallenge(Long id) {
        taskChallengeRepository.deleteById(id);
    }
}