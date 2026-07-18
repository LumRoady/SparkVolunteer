package com.spark.volunteer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * 用户实体类
 * 对应数据库的 user 表
 */
@Entity
@Table(name = "`user`", indexes = {
    @Index(name = "idx_user_openid", columnList = "openid"),
    @Index(name = "idx_user_role", columnList = "role"),
    @Index(name = "idx_user_phone", columnList = "phone"),
    @Index(name = "idx_user_community", columnList = "community"),
    @Index(name = "idx_user_username", columnList = "username"),
    @Index(name = "idx_user_is_deleted", columnList = "is_deleted"),
    @Index(name = "idx_user_last_checkin", columnList = "last_checkin"),
    @Index(name = "idx_user_create_time", columnList = "create_time"),
    @Index(name = "idx_user_update_time", columnList = "update_time")
})
@Getter
@Setter
public class User implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    // 主键，自动增长
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 微信用户唯一标识
    @Column(unique = true)
    private String openid;

    // 用户昵称
    private String nickname;

    // 头像URL
    private String avatar;

    // 用户角色（老人/志愿者）
    @Column(length = 20, nullable = false)
    private String role = "ELDERLY";

    // 手机号码（脱敏存储）
    @Column
    private String phone;

    // 所在社区
    @Column
    private String community;

    // 用户名
    @Column(unique = true)
    private String username;

    // 密码
    @JsonIgnore  // 防止序列化时返回密码
    private String password;

    // 姓名
    private String name;

    // 地址
    private String address;

    // 省份
    private String province;

    // 城市
    private String city;

    // 微信openid（用于发模板消息）
    @Column(name = "wechat_openid")
    private String wechatOpenid;

    // 关联的亲属老人ID（子女记录指向老人，用于紧急通知）
    @Column(name = "parent_id")
    private Long parentId;

    // 与亲属的关系（子女/配偶/其他）
    @Column(length = 20)
    private String relation;

    // 是否删除：0-未删除，1-已删除
    @Column(name = "is_deleted")
    private Integer isDeleted = 0;

    // 积分
    private Integer points = 0;

    // 完成的任务数量
    @Column(name = "completed_tasks")
    private Integer completedTasks = 0;

    // 最后签到时间
    @Column(name = "last_checkin")
    private Date lastCheckin;

    // 连续签到天数
    @Column(name = "checkin_streak")
    private Integer checkinStreak = 0;

    // 创建时间，插入时自动设置
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    // 更新时间，更新时自动设置
    @Column(name = "update_time")
    private Date updateTime;

    // 在保存到数据库之前执行
    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        updateTime = new Date();
    }

    // 在更新到数据库之前执行
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
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}