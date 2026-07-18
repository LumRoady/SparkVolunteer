package com.spark.volunteer.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * 设备实体类
 * 用于管理ESP32嵌入式遥控器
 */
@Entity
@Table(name = "device")
@Getter
@Setter
public class Device implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 设备ID（唯一标识）
    @Column(name = "device_id", unique = true, nullable = false)
    private String deviceId;

    // 设备名称
    private String name;

    // 设备类型：ESP32_REMOTE（ESP32遥控器）
    @Column(name = "device_type")
    private String deviceType = "ESP32_REMOTE";

    // 绑定的用户ID
    @Column(name = "user_id")
    private Long userId;

    // 设备状态：ACTIVE（活跃），INACTIVE（未激活）
    private String status = "INACTIVE";

    // 最后在线时间
    @Column(name = "last_online_time")
    private Date lastOnlineTime;

    // 创建时间
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @PrePersist
    protected void onCreate() {
        createTime = new Date();
        lastOnlineTime = new Date();
    }

    @PreUpdate
    protected void onUpdate() {
        lastOnlineTime = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Device)) return false;
        Device entity = (Device) o;
        return id != null && Objects.equals(id, entity.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
