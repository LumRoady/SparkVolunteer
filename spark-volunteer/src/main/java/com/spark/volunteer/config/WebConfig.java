/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 注意：CORS 配置已统一在 SecurityConfig 中管理，这里不再重复配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS 配置由 SecurityConfig.corsConfigurationSource() 统一管理
}
