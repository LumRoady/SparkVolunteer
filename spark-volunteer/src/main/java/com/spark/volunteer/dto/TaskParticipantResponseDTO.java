package com.spark.volunteer.dto;

import com.spark.volunteer.entity.TaskParticipant;
import lombok.Data;

import java.util.Date;

/**
 * 任务参与者响应 DTO — 隔离内部字段（remarks 等）
 */
@Data
public class TaskParticipantResponseDTO {

    private Long id;
    private Long taskId;
    private Long userId;
    private String role;          // REQUESTER/RECEIVER/VOLUNTEER
    private Integer status;       // 0-待确认/1-已接受/2-已拒绝/3-已完成
    private Date participateTime;
    private Date confirmTime;
    private Date completeTime;

    public static TaskParticipantResponseDTO fromEntity(TaskParticipant entity) {
        if (entity == null) return null;
        TaskParticipantResponseDTO dto = new TaskParticipantResponseDTO();
        dto.setId(entity.getId());
        dto.setTaskId(entity.getTaskId());
        dto.setUserId(entity.getUserId());
        dto.setRole(entity.getRole());
        dto.setStatus(entity.getStatus());
        dto.setParticipateTime(entity.getParticipateTime());
        dto.setConfirmTime(entity.getConfirmTime());
        dto.setCompleteTime(entity.getCompleteTime());
        return dto;
    }
}
