/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.config;

import com.spark.volunteer.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .httpBasic().disable()
            .formLogin().disable()
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
            // CORS 预检请求放行
            .antMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
            // 公开接口：认证、健康检查、WebSocket握手
            .antMatchers("/api/auth/**", "/api/hello", "/api/location", "/api/health").permitAll()
            // WebSocket（小程序实时通信）
            .antMatchers("/ws", "/ws/**").permitAll()
            // Swagger / OpenAPI 文档
            .antMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
            // Actuator 健康检查（Docker / K8s 探针）
            .antMatchers("/actuator/health", "/actuator/health/**").permitAll()
            // 任务列表只读（小程序首页）
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/task/**", "/api/tasks/**").permitAll()
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/mini-program/tasks/**").permitAll()
            // 排行榜 / 评价列表 / 签到记录 / 挑战 / 通知 公开只读
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/ranking", "/api/feedback/list").permitAll()
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/checkins/**").permitAll()
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/challenges/**").permitAll()
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/notifications/**").permitAll()

            // ===== ADMIN 专属接口 =====
            // 仪表盘（仅 Vue 管理后台使用）
            .antMatchers("/api/dashboard/**").hasAuthority("ADMIN")
            // 用户管理：列表、删除
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/users").hasAuthority("ADMIN")
            .antMatchers(org.springframework.http.HttpMethod.DELETE, "/api/users/**").hasAuthority("ADMIN")

            // ===== 用户自服务（认证即可，运行时校验只能访问自己的数据） =====
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/users/*/profile").authenticated()
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/users/*/stats").authenticated()
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/users/*/history").authenticated()
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/users/*/participated").authenticated()
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/users/*/achievements").authenticated()
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/users/*/certificates").authenticated()
            .antMatchers(org.springframework.http.HttpMethod.GET, "/api/users/*/devices").authenticated()
            .antMatchers(org.springframework.http.HttpMethod.PUT, "/api/users/**").authenticated()

            // ===== /api/users/** 兜底 → ADMIN =====
            .antMatchers("/api/users/**").hasAuthority("ADMIN")

            // ===== 以下全部需要认证 =====
            // 紧急求助
            .antMatchers("/api/emergency/**").authenticated()
            // 签到操作
            .antMatchers(org.springframework.http.HttpMethod.POST, "/api/checkins").authenticated()
            // 通知操作
            .antMatchers(org.springframework.http.HttpMethod.PUT, "/api/notifications/**").authenticated()
            .antMatchers(org.springframework.http.HttpMethod.POST, "/api/notifications").authenticated()
            // 任务操作
            .antMatchers(org.springframework.http.HttpMethod.POST, "/api/task/accept/**").authenticated()
            .antMatchers(org.springframework.http.HttpMethod.POST, "/api/task/complete/**").authenticated()
            .antMatchers(org.springframework.http.HttpMethod.POST, "/api/task/cancel/**").authenticated()
            .antMatchers(org.springframework.http.HttpMethod.PUT, "/api/tasks/**").authenticated()
            // 设备管理
            .antMatchers("/api/device/**").authenticated()
            // 亲属绑定
            .antMatchers("/api/family/**").authenticated()
            // 社区 / 志愿者 / 地图 / 消息
            .antMatchers("/api/community/**").authenticated()
            .antMatchers("/api/volunteer/**").authenticated()
            .antMatchers("/api/map/**").authenticated()
            .antMatchers("/api/messages/**").authenticated()
            // 评价操作
            .antMatchers("/api/feedback/**").authenticated()
            // 小程序专用
            .antMatchers("/api/mini-program/elderly/**").authenticated()
            .antMatchers("/api/mini-program/volunteer/**").authenticated()
            .antMatchers("/api/mini-program/tasks/*/accept-by-volunteer").hasAuthority("VOLUNTEER")
            // V2 API
            .antMatchers("/api/v2/**").authenticated()
            .anyRequest().authenticated();

        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // CORS 配置：开发环境允许本地和小程序调试，生产环境通过环境变量指定
        // 使用 allowedOriginPatterns 而非 allowedOrigins，支持模式匹配
        String allowedOrigins = System.getenv().getOrDefault("CORS_ALLOWED_ORIGINS",
            "http://localhost:*");
        config.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
