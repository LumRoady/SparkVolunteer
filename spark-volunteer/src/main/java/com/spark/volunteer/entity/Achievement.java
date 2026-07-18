package com.spark.volunteer.entity;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.util.Date;

/**
 * 成就实体类
 * 记录志愿者解锁的成就徽章
 */
@Entity
@Table(name = "achievement")
@Getter
@Setter
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 成就类型编码 */
    @Column(name = "achievement_type", nullable = false, length = 50)
    private String achievementType;

    /** 成就名称 */
    @Column(nullable = false, length = 100)
    private String title;

    /** 成就描述 */
    @Column(length = 255)
    private String description;

    /** 解锁时间 */
    @Column(name = "unlocked_at")
    private Date unlockedAt;

    /** 图标 emoji */
    @Column(length = 20)
    private String icon;

    @PrePersist
    protected void onCreate() {
        unlockedAt = new Date();
    }
}
