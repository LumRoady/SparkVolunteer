package com.spark.volunteer.repository;

import com.spark.volunteer.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // 根据状态查找任务
    List<Task> findByStatus(Integer status);

    // 根据状态查找任务（分页）
    Page<Task> findByStatus(Integer status, Pageable pageable);

    // 根据类型查找任务
    List<Task> findByType(String type);

    // 根据类型查找任务（分页）
    Page<Task> findByType(String type, Pageable pageable);

    // 根据状态和类型查找任务
    List<Task> findByStatusAndType(Integer status, String type);

    // 根据状态和类型查找任务（分页）
    Page<Task> findByStatusAndType(Integer status, String type, Pageable pageable);

    // 根据用户ID查找任务
    List<Task> findByUserId(Long userId);
    Page<Task> findByUserId(Long userId, Pageable pageable);

    // 根据接单人ID查找任务
    List<Task> findByReceiverId(Long receiverId);
    Page<Task> findByReceiverId(Long receiverId, Pageable pageable);

    // 根据task_no查找任务
    Task findByTaskNo(Integer taskNo);

    // ===== 数据大屏 & 成长体系 =====

    // 时间范围查询
    @Query("SELECT t FROM Task t WHERE t.createTime BETWEEN :start AND :end")
    List<Task> findByCreateTimeBetween(@Param("start") Date start, @Param("end") Date end);

    // 今日创建数
    @Query("SELECT COUNT(t) FROM Task t WHERE t.createTime >= :todayStart")
    long countTodayCreated(@Param("todayStart") Date todayStart);

    // 今日已完成数
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 2 AND t.finishTime >= :todayStart")
    long countTodayCompleted(@Param("todayStart") Date todayStart);

    // 今日已接单数
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status IN (1,2) AND t.acceptTime >= :todayStart")
    long countTodayAccepted(@Param("todayStart") Date todayStart);

    // 今日紧急求助待处理数
    @Query("SELECT COUNT(t) FROM Task t WHERE t.type = 'sos' AND t.status = 0")
    long countEmergencyPending();

    // 志愿者完成服务次数
    long countByReceiverId(Long receiverId);

    // 志愿者平均评分
    @Query("SELECT AVG(t.rating) FROM Task t WHERE t.receiverId = :volunteerId AND t.rating IS NOT NULL")
    Double avgRatingByReceiverId(@Param("volunteerId") Long volunteerId);

    // 今日平均响应时间（分钟）—— 使用 nativeQuery 兼容 MySQL 和 H2
    @Query(value = "SELECT AVG(TIMESTAMPDIFF(MINUTE, t.create_time, t.accept_time)) FROM task t " +
           "WHERE t.accept_time IS NOT NULL AND t.accept_time >= :todayStart", nativeQuery = true)
    Double avgResponseMinutesToday(@Param("todayStart") Date todayStart);

    // 近7天每日求助数 —— 使用 nativeQuery 兼容 MySQL 和 H2
    @Query(value = "SELECT CAST(t.create_time AS DATE) as dt, COUNT(*) as cnt FROM task t " +
           "WHERE t.create_time >= :sevenDaysAgo GROUP BY CAST(t.create_time AS DATE) ORDER BY dt", nativeQuery = true)
    List<Object[]> countByDaySince(@Param("sevenDaysAgo") Date sevenDaysAgo);

    // ===== 原子操作（防竞态） =====

    /** 原子接单：仅当 status=0(PENDING) 时更新，返回影响行数（0=已被他人接单） */
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = 1, t.receiverId = :volunteerId, t.acceptTime = CURRENT_TIMESTAMP " +
           "WHERE t.id = :taskId AND t.status = 0")
    int acceptTaskAtomically(@Param("taskId") Long taskId, @Param("volunteerId") Long volunteerId);

    /** 原子完成任务：仅当 status=1(ACCEPTED) 且 receiverId 匹配时更新 */
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = 2, t.finishTime = CURRENT_TIMESTAMP " +
           "WHERE t.id = :taskId AND t.status = 1 AND t.receiverId = :volunteerId")
    int completeTaskAtomically(@Param("taskId") Long taskId, @Param("volunteerId") Long volunteerId);

    /** 原子取消任务：仅当 status IN (0,1) 时更新 */
    @Modifying
    @Transactional
    @Query("UPDATE Task t SET t.status = 3 WHERE t.id = :taskId AND t.status IN (0, 1)")
    int cancelTaskAtomically(@Param("taskId") Long taskId);
}