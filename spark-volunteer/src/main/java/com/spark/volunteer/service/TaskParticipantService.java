package com.spark.volunteer.service;

import com.spark.volunteer.entity.TaskParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 任务参与者服务接口
 */
public interface TaskParticipantService {

    /**
     * 创建任务参与者记录
     */
    TaskParticipant createParticipant(TaskParticipant participant);

    /**
     * 更新参与者状态
     */
    TaskParticipant updateParticipantStatus(Long id, Integer status);

    /**
     * 根据ID获取参与者
     */
    Optional<TaskParticipant> getParticipantById(Long id);

    /**
     * 根据任务ID获取所有参与者
     */
    List<TaskParticipant> getParticipantsByTaskId(Long taskId);

    /**
     * 根据用户ID获取所有参与的任务
     */
    List<TaskParticipant> getParticipantsByUserId(Long userId);

    /**
     * 根据任务ID和用户ID获取参与记录
     */
    Optional<TaskParticipant> getParticipantByTaskIdAndUserId(Long taskId, Long userId);

    /**
     * 根据任务ID和角色获取参与者
     */
    List<TaskParticipant> getParticipantsByTaskIdAndRole(Long taskId, String role);

    /**
     * 根据用户ID和角色获取参与的任务
     */
    List<TaskParticipant> getParticipantsByUserIdAndRole(Long userId, String role);

    /**
     * 分页查询任务参与者
     */
    Page<TaskParticipant> getParticipantsByTaskId(Long taskId, Pageable pageable);

    /**
     * 分页查询用户参与的任务
     */
    Page<TaskParticipant> getParticipantsByUserId(Long userId, Pageable pageable);

    /**
     * 获取任务的发起者
     */
    Optional<TaskParticipant> getTaskRequester(Long taskId);

    /**
     * 获取任务的接收者
     */
    Optional<TaskParticipant> getTaskReceiver(Long taskId);

    /**
     * 统计任务的参与人数
     */
    long countParticipantsByTaskId(Long taskId);

    /**
     * 统计用户参与的任务数
     */
    long countParticipantsByUserId(Long userId);

    /**
     * 删除参与者记录
     */
    void deleteParticipant(Long id);
}