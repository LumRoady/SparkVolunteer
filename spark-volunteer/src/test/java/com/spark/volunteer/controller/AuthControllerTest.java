package com.spark.volunteer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spark.volunteer.config.TestRedisConfig;
import com.spark.volunteer.config.WebSocketHandler;
import com.spark.volunteer.dto.request.LoginRequest;
import com.spark.volunteer.dto.request.RegisterRequest;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.service.RateLimitService;
import com.spark.volunteer.service.RedisCacheService;
import com.spark.volunteer.service.RefreshTokenService;
import com.spark.volunteer.service.UserService;
import com.spark.volunteer.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 单元测试（@WebMvcTest）
 * 公开接口，禁用 filter 链避免 JWT/CSRF 干扰
 */
@WebMvcTest(com.spark.volunteer.controller.AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RedisCacheService redisCacheService;

    @MockBean
    private RateLimitService rateLimitService;

    @MockBean
    private WebSocketHandler webSocketHandler;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Test
    void register_withoutRole_returnsElderly() throws Exception {
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser");
        savedUser.setRole("ELDERLY");

        when(userService.register(any(User.class))).thenReturn(savedUser);
        when(redisCacheService.isLoginLocked(anyString())).thenReturn(false);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.role").value("ELDERLY"));
    }

    @Test
    void login_success_returnsTokenAndUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole("ADMIN");

        when(userService.login("testuser", "password123")).thenReturn(user);
        when(redisCacheService.isLoginLocked("testuser")).thenReturn(false);
        when(jwtUtil.generateToken(eq("testuser"), eq("ADMIN"), anyLong())).thenReturn("mock-jwt-token");

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"))
                .andExpect(jsonPath("$.data.user.role").value("ADMIN"));
    }

    @Test
    void login_lockedAccount_returnsError() throws Exception {
        when(redisCacheService.isLoginLocked("locked")).thenReturn(true);
        when(redisCacheService.getLoginLockRemainingSeconds("locked")).thenReturn(300L);

        LoginRequest request = new LoginRequest();
        request.setUsername("locked");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
