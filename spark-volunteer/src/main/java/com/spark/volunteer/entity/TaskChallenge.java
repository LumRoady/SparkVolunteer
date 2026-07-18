package com.spark.volunteer.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * 任务挑战实体类
 * 对应数据库的 task_challenge 表
 */
@Entity
@Table(name = "task_challenge")
@Getter
@Setter
public class TaskChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 挑战标题
    private String title;

    // 挑战描述
    @Column(columnDefinition = "TEXT")
    private String description;

    // 挑战奖励积分
    private Integer reward;

    // 标记挑战是否完成
    private Boolean completed = false;

    // 联系人信息
    private String contact;

    // 挑战地点
    private String address;

    // 关联发布挑战的用户
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 创建时间
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    // 更新时间
    @Column(name = "updated_at")
    private Date updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new Date();
        updatedAt = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskChallenge)) return false;
        TaskChallenge entity = (TaskChallenge) o;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}