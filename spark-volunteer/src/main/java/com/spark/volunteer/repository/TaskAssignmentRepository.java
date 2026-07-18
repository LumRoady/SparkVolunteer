package com.spark.volunteer.repository;

import com.spark.volunteer.entity.TaskAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, Long> {

    // 根据任务ID查询分配记录
    List<TaskAssignment> findByTaskId(Long taskId);

    // 根据志愿者ID查询分配记录
    List<TaskAssignment> findByVolunteerId(Long volunteerId);

    // 根据任务ID和志愿者ID查询分配记录
    TaskAssignment findByTaskIdAndVolunteerId(Long taskId, Long volunteerId);

    // 根据状态查询分配记录
    List<TaskAssignment> findByStatus(Integer status);
}