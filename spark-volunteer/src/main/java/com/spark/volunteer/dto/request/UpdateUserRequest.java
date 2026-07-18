/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto.request;

import javax.validation.constraints.NotNull;

/**
 * 更新用户信息请求 DTO
 * 用于用户信息更新接口的请求参数
 */
public class UpdateUserRequest {

    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String nickname;

    private String avatar;

    private String phone;

    private String community;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCommunity() { return community; }
    public void setCommunity(String community) { this.community = community; }
}
