/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.controller;

import com.spark.volunteer.common.AuthResponse;
import com.spark.volunteer.common.Result;
import com.spark.volunteer.dto.UserResponseDTO;
import com.spark.volunteer.dto.request.LoginRequest;
import com.spark.volunteer.dto.request.RefreshTokenRequest;
import com.spark.volunteer.dto.request.RegisterRequest;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.exception.UnauthorizedException;
import com.spark.volunteer.service.RedisCacheService;
import com.spark.volunteer.service.RefreshTokenService;
import com.spark.volunteer.service.UserService;
import com.spark.volunteer.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.Random;

/**
 * 认证控制器
 * 处理用户登录、注册、令牌刷新等认证相关的HTTP请求
 *
 * 登录限流基于 Redis 实现，支持分布式部署
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Random RANDOM = new Random();

    /**
     * 用户登录（基于Redis的暴力破解防护：5次失败后锁定15分钟）
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String username = request.getUsername();

        // Redis检查是否被锁定（Redis不可用时跳过锁定检查）
        try {
            if (redisCacheService.isLoginLocked(username)) {
                long remainingSeconds = redisCacheService.getLoginLockRemainingSeconds(username);
                logger.warn("账号 {} 已被锁定，剩余 {} 秒", username, remainingSeconds);
                throw new BusinessException("登录失败次数过多，请" + remainingSeconds + "秒后重试");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Redis 不可用，跳过登录锁定检查: {}", e.getMessage());
        }

        try {
            User user = userService.login(username, request.getPassword());
            // 登录成功，清除 Redis 中的失败记录
            try { redisCacheService.clearLoginFailure(username); } catch (Exception ignored) {}
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());
            String refreshToken = null;
            try { refreshToken = refreshTokenService.createRefreshToken(user.getId()); } catch (Exception ignored) {}
            logger.info("用户 {} 登录成功", username);
            return Result.success(new AuthResponse(token, UserResponseDTO.fromEntity(user), refreshToken));
        } catch (Exception e) {
            // 登录失败，Redis 记录失败次数
            try { redisCacheService.recordLoginFailure(username); } catch (Exception ignored) {}
            logger.warn("账号 {} 登录失败", username);
            throw e;
        }
    }

    /**
     * 用户注册
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public Result<UserResponseDTO> register(@Valid @RequestBody RegisterRequest request) {
        User registeredUser = userService.register(request.toUser());
        return Result.success(UserResponseDTO.fromEntity(registeredUser));
    }

    /**
     * 刷新访问令牌
     * POST /api/auth/refresh
     * 使用 refresh token 换取新的 JWT 和新的 refresh token（令牌轮换）
     */
    @PostMapping("/refresh")
    public Result<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        Long userId = refreshTokenService.validateRefreshToken(request.getRefreshToken());
        if (userId == null) {
            throw new UnauthorizedException("刷新令牌无效或已过期");
        }

        // 查找用户
        User user = userService.getUserById(userId);
        if (user.getIsDeleted() != 0) {
            throw new UnauthorizedException("用户已被禁用");
        }

        // 删除旧 refresh token（轮换，防止重放攻击）
        refreshTokenService.deleteRefreshToken(request.getRefreshToken());

        // 生成新的 JWT 和 refresh token
        String newToken = jwtUtil.generateToken(user.getUsername(), user.getRole(), user.getId());
        String newRefreshToken = refreshTokenService.createRefreshToken(user.getId());

        logger.info("用户 {} 刷新令牌成功", user.getUsername());
        return Result.success(new AuthResponse(newToken, UserResponseDTO.fromEntity(user), newRefreshToken));
    }

    // ==================== 忘记密码 / 修改密码 / 绑定手机 ====================

    /**
     * 发送密码重置验证码
     * POST /api/auth/send-reset-code
     */
    @PostMapping("/send-reset-code")
    public Result<String> sendResetCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("请输入正确的手机号码");
        }
        // 检查手机号是否存在
        if (!userService.getAllUsers().stream().anyMatch(u -> phone.equals(u.getPhone()))) {
            throw new BusinessException("该手机号未注册");
        }
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        try {
            redisCacheService.storeVerifyCode("reset:" + phone, code, 5);
        } catch (Exception e) {
            logger.warn("Redis 不可用，验证码仅打印日志: {}", e.getMessage());
        }
        logger.info("密码重置验证码已发送: phone={}, code={}", phone, code);
        // 开发阶段返回验证码（生产环境应通过短信网关发送）
        return Result.success("验证码已发送");
    }

    /**
     * 重置密码
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");
        String newPassword = body.get("newPassword");

        if (phone == null || code == null || newPassword == null) {
            throw new BusinessException("参数不完整");
        }
        if (newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }

        // 验证码校验
        boolean valid;
        try {
            valid = redisCacheService.verifyCode("reset:" + phone, code);
        } catch (Exception e) {
            logger.warn("Redis 不可用，跳过验证码校验");
            valid = true; // Redis 不可用时降级放行
        }
        if (!valid) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 查找用户并更新密码
        User user = userService.getAllUsers().stream()
                .filter(u -> phone.equals(u.getPhone()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("该手机号未注册"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateUser(user);
        logger.info("用户密码已重置: phone={}", phone);
        return Result.success("密码重置成功");
    }

    /**
     * 修改密码（需登录）
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public Result<String> changePassword(@RequestBody Map<String, String> body,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            throw new BusinessException("参数不完整");
        }
        if (newPassword.length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }

        // 从 JWT 解析用户
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录");
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateUser(user);
        logger.info("用户 {} 修改密码成功", user.getUsername());
        return Result.success("密码修改成功");
    }

    /**
     * 发送绑定手机验证码
     * POST /api/auth/send-bind-code
     */
    @PostMapping("/send-bind-code")
    public Result<String> sendBindCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("请输入正确的手机号码");
        }
        String code = String.format("%06d", RANDOM.nextInt(1000000));
        try {
            redisCacheService.storeVerifyCode("bind:" + phone, code, 5);
        } catch (Exception e) {
            logger.warn("Redis 不可用，验证码仅打印日志: {}", e.getMessage());
        }
        logger.info("绑定手机验证码已发送: phone={}, code={}", phone, code);
        return Result.success("验证码已发送");
    }

    /**
     * 绑定手机（需登录）
     * POST /api/auth/bind-phone
     */
    @PostMapping("/bind-phone")
    public Result<String> bindPhone(@RequestBody Map<String, String> body,
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String phone = body.get("phone");
        String code = body.get("code");
        if (phone == null || code == null) {
            throw new BusinessException("参数不完整");
        }

        // 验证码校验
        boolean valid;
        try {
            valid = redisCacheService.verifyCode("bind:" + phone, code);
        } catch (Exception e) {
            logger.warn("Redis 不可用，跳过验证码校验");
            valid = true;
        }
        if (!valid) {
            throw new BusinessException("验证码错误或已过期");
        }

        // 从 JWT 解析用户
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录");
        }
        String token = authHeader.substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getUserById(userId);
        user.setPhone(phone);
        userService.updateUser(user);
        logger.info("用户 {} 绑定手机成功: phone={}", user.getUsername(), phone);
        return Result.success("绑定成功");
    }
}
