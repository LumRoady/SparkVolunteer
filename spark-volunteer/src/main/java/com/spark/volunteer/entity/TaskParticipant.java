package com.spark.volunteer.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * 任务参与者实体类
 * 记录任务与参与者的关联关系，支持多个志愿者参与同一个任务
 */
@Entity
@Table(name = "task_participant")
@Getter
@Setter
public class TaskParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 任务ID（外键）
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    // 用户ID（外键）
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 参与角色（REQUESTER-发起者/RECEIVER-接收者/VOLUNTEER-志愿者）
    @Column(length = 20, nullable = false)
    private String role;

    // 参与状态（0-待确认/1-已接受/2-已拒绝/3-已完成）
    @Column
    private Integer status = 0;

    // 参与时间
    @Column(name = "participate_time")
    private Date participateTime;

    // 确认时间
    @Column(name = "confirm_time")
    private Date confirmTime;

    // 完成时间
    @Column(name = "complete_time")
    private Date completeTime;

    // 创建时间
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    // 更新时间
    @Column(name = "update_time")
    private Date updateTime;

    // 备注信息
    @Column(length = 500)
    private String remarks;

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
        participateTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskParticipant)) return false;
        TaskParticipant entity = (TaskParticipant) o;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}