package com.spark.volunteer.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.PreUpdate;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * 消息实体类
 * 对应数据库的 message 表
 */
@Getter
@Setter
@Entity
@Table(name = "message")
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 发送者ID（关联user表）
     */
    @Column(name = "sender_id")
    private Long senderId;

    /**
     * 接收者ID（关联user表）
     */
    @Column(name = "receiver_id")
    private Long receiverId;

    /**
     * 消息类型：TEXT, NOTIFICATION, SYSTEM
     */
    @Column(name = "message_type")
    private String messageType;

    /**
     * 消息内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 消息状态：UNREAD, READ
     */
    @Column(name = "status")
    private String status;

    /**
     * 关联的任务ID（可选）
     */
    @Column(name = "task_id")
    private Long taskId;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    /**
     * 最后更新时间
     */
    @Column(name = "update_time")
    private Date updateTime;

    /**
     * 消息已删除标志
     */
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    /**
     * 自动更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Message)) return false;
        Message entity = (Message) o;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
