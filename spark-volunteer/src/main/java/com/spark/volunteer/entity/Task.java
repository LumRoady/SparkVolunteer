package com.spark.volunteer.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.util.Date;
import java.util.Objects;

/**
 * 任务实体类
 */
@Entity
@Table(name = "task")
@Getter
@Setter
public class Task implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 发布任务的老人ID（外键）
    @Column(name = "user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    // 发布者ID（外键）
    @Column(name = "requester_id")
    private Long requesterId;
    
    // 发布任务的设备ID（外键）
    @Column(name = "device_id")
    private String deviceId;

    // 任务类型（sos/life_service/consultation）
    @Column(name = "type")
    @NotBlank(message = "任务类型不能为空")
    private String type;

    // 任务标题
    @NotBlank(message = "任务标题不能为空")
    @Size(max = 100, message = "任务标题长度不能超过100个字符")
    private String title;

    // 任务描述
    @Column(name = "content", columnDefinition = "TEXT")
    @NotBlank(message = "任务描述不能为空")
    private String content;

    // 任务状态（0待接单/1已接单/2已完成/3已取消）
    @Column
    private Integer status = 0;

    // 接单人ID（志愿者）
    @Column(name = "receiver_id")
    private Long receiverId;

    // 任务地点
    private String location;

    // 纬度
    private Double latitude;

    // 经度
    private Double longitude;

    // 创建时间
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    // 接单时间
    @Column(name = "accept_time")
    private Date acceptTime;

    // 完成时间
    @Column(name = "finish_time")
    private Date finishTime;

    // 更新时间
    @Column(name = "update_time")
    private Date updateTime;

    // 紧急程度（0-一般/1-紧急/2-非常紧急）
    private Integer urgency = 0;

    // 优先级（0-普通/1-重要/2-高优先级）
    private Integer priority = 0;

    // 预计完成时间（分钟）
    @Column(name = "estimated_time")
    private Integer estimatedTime;

    // 评分（1-5 分）
    private Integer rating;

    // 对外展示序号（1,2,3...）
    @Column(name = "task_no")
    private Integer taskNo;

    // 评价内容
    @Column(columnDefinition = "TEXT")
    private String review;

    // 是否需要上门服务
    @Column(name = "need_home_service")
    private Boolean needHomeService = false;

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = new Date();
    }

    /**
     * 基于ID的相等判断，避免JPA代理问题
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return id != null && Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}