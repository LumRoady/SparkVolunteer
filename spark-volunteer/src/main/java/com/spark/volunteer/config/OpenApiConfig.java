/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc OpenAPI 3 配置
 *
 * 访问地址: http://localhost:8084/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sparkVolunteerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("星火智慧养老志愿服务平台 API")
                        .description("企业级志愿服务管理系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("星火志愿团队")));
    }
}
