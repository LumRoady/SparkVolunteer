package com.spark.volunteer.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * 签到记录实体类
 * 对应数据库的 checkin 表
 */
@Entity
@Table(name = "checkin")
@Getter
@Setter
public class Checkin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 关联签到的用户
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 签到日期
    @Column(name = "checkin_date", nullable = false)
    private Date checkinDate;

    // 签到时间
    @Column(name = "checkin_time", nullable = false)
    private Date checkinTime;

    // 签到状态
    private String status = "SUCCESS";

    // 创建时间
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Checkin)) return false;
        Checkin entity = (Checkin) o;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}