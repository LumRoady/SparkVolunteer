/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto.request;

import com.spark.volunteer.entity.User;

import javax.validation.constraints.NotBlank;

/**
 * 创建用户（注册）请求 DTO
 * 用于用户注册接口的请求参数
 */
public class CreateUserRequest {

    private String nickname;

    private String avatar;

    private String phone;

    private String community;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    /**
     * 将 DTO 转换为 User 实体（不含角色，由 Service 层控制）
     */
    public User toUser() {
        User user = new User();
        user.setNickname(this.nickname);
        user.setAvatar(this.avatar);
        user.setPhone(this.phone);
        user.setCommunity(this.community);
        user.setUsername(this.username);
        user.setPassword(this.password);
        return user;
    }
}
