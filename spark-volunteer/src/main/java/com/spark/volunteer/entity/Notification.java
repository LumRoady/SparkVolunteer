package com.spark.volunteer.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * 通知实体类
 * 对应数据库的 notification 表
 */
@Entity
@Table(name = "notification")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 通知类型（task、system、activity）
    private String type;

    // 通知标题
    private String title;

    // 通知内容
    @Column(columnDefinition = "TEXT")
    private String content;

    // 关联的任务ID（可选）
    @Column(name = "task_id")
    private Long taskId;

    // 关联的用户ID（系统通知可为空）
    @Column(name = "user_id")
    private Long userId;

    // 标记通知是否已读
    @Column(name = "is_read")
    private Boolean isRead = false;

    // 创建时间
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @PrePersist
    protected void onCreate() {
        if (createTime == null) createTime = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification)) return false;
        Notification entity = (Notification) o;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}