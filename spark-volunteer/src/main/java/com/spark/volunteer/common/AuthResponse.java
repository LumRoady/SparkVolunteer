/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.common;

import com.spark.volunteer.dto.UserResponseDTO;
import lombok.Data;

@Data
public class AuthResponse {

    private String token;
    private UserResponseDTO user;
    private String refreshToken;  // 刷新令牌（7天有效，用于换取新 JWT）

    public AuthResponse(String token, UserResponseDTO user, String refreshToken) {
        this.token = token;
        this.user = user;
        this.refreshToken = refreshToken;
    }

    /** 向后兼容的双参构造函数（不返回 refreshToken） */
    public AuthResponse(String token, UserResponseDTO user) {
        this(token, user, null);
    }
}
