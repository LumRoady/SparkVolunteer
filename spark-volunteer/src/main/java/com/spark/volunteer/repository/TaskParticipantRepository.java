package com.spark.volunteer.repository;

import com.spark.volunteer.entity.TaskParticipant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 任务参与者数据访问接口
 */
public interface TaskParticipantRepository extends JpaRepository<TaskParticipant, Long> {

    /**
     * 根据任务ID查询所有参与者
     */
    List<TaskParticipant> findByTaskId(Long taskId);

    /**
     * 根据用户ID查询所有参与的任务
     */
    List<TaskParticipant> findByUserId(Long userId);

    /**
     * 根据任务ID和用户ID查询参与记录
     */
    Optional<TaskParticipant> findByTaskIdAndUserId(Long taskId, Long userId);

    /**
     * 根据任务ID和角色查询参与者
     */
    List<TaskParticipant> findByTaskIdAndRole(Long taskId, String role);

    /**
     * 根据用户ID和角色查询参与的任务
     */
    List<TaskParticipant> findByUserIdAndRole(Long userId, String role);

    /**
     * 根据任务ID和状态查询参与者
     */
    List<TaskParticipant> findByTaskIdAndStatus(Long taskId, Integer status);

    /**
     * 根据用户ID和状态查询参与的任务
     */
    List<TaskParticipant> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 分页查询任务参与者
     */
    Page<TaskParticipant> findByTaskId(Long taskId, Pageable pageable);

    /**
     * 分页查询用户参与的任务
     */
    Page<TaskParticipant> findByUserId(Long userId, Pageable pageable);

    /**
     * 查询任务的接收者
     */
    Optional<TaskParticipant> findByTaskIdAndRoleAndStatus(Long taskId, String role, Integer status);

    /**
     * 统计任务的参与人数
     */
    @Query("SELECT COUNT(tp) FROM TaskParticipant tp WHERE tp.taskId = :taskId")
    long countByTaskId(@Param("taskId") Long taskId);

    /**
     * 统计用户参与的任务数
     */
    @Query("SELECT COUNT(tp) FROM TaskParticipant tp WHERE tp.userId = :userId")
    long countByUserId(@Param("userId") Long userId);
}