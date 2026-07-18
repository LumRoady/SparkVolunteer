/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.dto.request;

import javax.validation.constraints.NotBlank;

/**
 * 微信登录请求 DTO
 * 用于微信小程序登录接口的请求参数
 */
public class LoginByCodeRequest {

    @NotBlank(message = "微信登录凭证不能为空")
    private String code;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
