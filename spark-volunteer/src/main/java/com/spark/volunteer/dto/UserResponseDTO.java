package com.spark.volunteer.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.spark.volunteer.entity.User;
import lombok.Data;

import java.util.Date;

/**
 * 用户响应DTO — 隔离敏感字段（密码/openid/isDeleted 等不暴露给前端）
 */
@Data
public class UserResponseDTO {

    private Long id;
    private String username;       // 用户名
    private String nickname;       // 昵称
    private String name;           // 真实姓名
    private String phone;          // 手机号（脱敏输出）
    private String avatar;         // 头像URL
    private String role;           // 角色: ADMIN/VOLUNTEER/ELDERLY
    private String community;      // 所属社区
    private String address;        // 地址
    private String province;       // 省份
    private String city;           // 城市
    private Integer points;        // 积分
    private Integer completedTasks;// 完成任务数
    private Date lastCheckin;      // 最后签到时间
    private Integer checkinStreak; // 连续签到天数
    private Date createTime;       // 注册时间
    private Date updateTime;       // 更新时间

    /**
     * 手机号脱敏：中间4位替换为****
     * 例如: 138****0001
     */
    @JsonGetter("phone")
    public String getMaskedPhone() {
        return maskPhone(phone);
    }

    public static UserResponseDTO fromEntity(User entity) {
        if (entity == null) return null;
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(entity.getId());
        dto.setUsername(entity.getUsername());
        dto.setNickname(entity.getNickname());
        dto.setName(entity.getName());
        dto.setPhone(entity.getPhone());
        dto.setAvatar(entity.getAvatar());
        dto.setRole(entity.getRole());
        dto.setCommunity(entity.getCommunity());
        dto.setAddress(entity.getAddress());
        dto.setProvince(entity.getProvince());
        dto.setCity(entity.getCity());
        dto.setPoints(entity.getPoints());
        dto.setCompletedTasks(entity.getCompletedTasks());
        dto.setLastCheckin(entity.getLastCheckin());
        dto.setCheckinStreak(entity.getCheckinStreak());
        dto.setCreateTime(entity.getCreateTime());
        dto.setUpdateTime(entity.getUpdateTime());
        return dto;
    }

    /**
     * 手机号脱敏工具方法
     * 11位手机号中间4位替换为 ****
     */
    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
