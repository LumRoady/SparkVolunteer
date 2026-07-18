package com.spark.volunteer.service.impl;

import com.spark.volunteer.entity.TaskParticipant;
import com.spark.volunteer.exception.NotFoundException;
import com.spark.volunteer.repository.TaskParticipantRepository;
import com.spark.volunteer.service.TaskParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 任务参与者服务实现类
 */
@Service
public class TaskParticipantServiceImpl implements TaskParticipantService {

    @Autowired
    private TaskParticipantRepository taskParticipantRepository;

    @Override
    public TaskParticipant createParticipant(TaskParticipant participant) {
        return taskParticipantRepository.save(participant);
    }

    @Override
    public TaskParticipant updateParticipantStatus(Long id, Integer status) {
        Optional<TaskParticipant> participantOpt = taskParticipantRepository.findById(id);
        if (participantOpt.isPresent()) {
            TaskParticipant participant = participantOpt.get();
            participant.setStatus(status);
            
            // 根据状态更新相应的时间
            if (status == 1) { // 已接受
                participant.setConfirmTime(new Date());
            } else if (status == 3) { // 已完成
                participant.setCompleteTime(new Date());
            }
            
            return taskParticipantRepository.save(participant);
        }
        throw new NotFoundException("参与者记录", id);
    }

    @Override
    public Optional<TaskParticipant> getParticipantById(Long id) {
        return taskParticipantRepository.findById(id);
    }

    @Override
    public List<TaskParticipant> getParticipantsByTaskId(Long taskId) {
        return taskParticipantRepository.findByTaskId(taskId);
    }

    @Override
    public List<TaskParticipant> getParticipantsByUserId(Long userId) {
        return taskParticipantRepository.findByUserId(userId);
    }

    @Override
    public Optional<TaskParticipant> getParticipantByTaskIdAndUserId(Long taskId, Long userId) {
        return taskParticipantRepository.findByTaskIdAndUserId(taskId, userId);
    }

    @Override
    public List<TaskParticipant> getParticipantsByTaskIdAndRole(Long taskId, String role) {
        return taskParticipantRepository.findByTaskIdAndRole(taskId, role);
    }

    @Override
    public List<TaskParticipant> getParticipantsByUserIdAndRole(Long userId, String role) {
        return taskParticipantRepository.findByUserIdAndRole(userId, role);
    }

    @Override
    public Page<TaskParticipant> getParticipantsByTaskId(Long taskId, Pageable pageable) {
        return taskParticipantRepository.findByTaskId(taskId, pageable);
    }

    @Override
    public Page<TaskParticipant> getParticipantsByUserId(Long userId, Pageable pageable) {
        return taskParticipantRepository.findByUserId(userId, pageable);
    }

    @Override
    public Optional<TaskParticipant> getTaskRequester(Long taskId) {
        List<TaskParticipant> participants = taskParticipantRepository.findByTaskIdAndRole(taskId, "REQUESTER");
        return participants.isEmpty() ? Optional.empty() : Optional.of(participants.get(0));
    }

    @Override
    public Optional<TaskParticipant> getTaskReceiver(Long taskId) {
        return taskParticipantRepository.findByTaskIdAndRoleAndStatus(taskId, "RECEIVER", 1);
    }

    @Override
    public long countParticipantsByTaskId(Long taskId) {
        return taskParticipantRepository.countByTaskId(taskId);
    }

    @Override
    public long countParticipantsByUserId(Long userId) {
        return taskParticipantRepository.countByUserId(userId);
    }

    @Override
    public void deleteParticipant(Long id) {
        taskParticipantRepository.deleteById(id);
    }
}