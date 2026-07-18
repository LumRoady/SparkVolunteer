package com.spark.volunteer.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * 任务分配关系表
 * 记录任务与志愿者的关联关系
 *
 * @deprecated 当前业务流程已不使用该实体，任务接收关系通过 Task.receiverId 字段管理。
 *             保留以避免数据库迁移风险，后续版本可安全删除。
 */
@Deprecated
@Entity
@Table(name = "task_assignment", indexes = {
    @Index(name = "idx_task_assignment_task_id", columnList = "task_id"),
    @Index(name = "idx_task_assignment_volunteer_id", columnList = "volunteer_id"),
    @Index(name = "idx_task_assignment_status", columnList = "status")
})
@Getter
@Setter
public class TaskAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 任务ID（外键）
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    // 志愿者ID（外键）
    @Column(name = "volunteer_id", nullable = false)
    private Long volunteerId;

    // 分配状态（0-待确认/1-已接受/2-已拒绝）
    @Column
    private Integer status = 0;

    // 分配时间
    @Column(name = "assign_time")
    private Date assignTime;

    // 确认时间
    @Column(name = "confirm_time")
    private Date confirmTime;

    // 创建时间
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    // 更新时间
    @Column(name = "update_time")
    private Date updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
        assignTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskAssignment)) return false;
        TaskAssignment entity = (TaskAssignment) o;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}