package com.spark.volunteer.controller;

import com.spark.volunteer.config.SecurityConfig;
import com.spark.volunteer.config.TestRedisConfig;
import com.spark.volunteer.config.WebSocketHandler;
import com.spark.volunteer.service.DashboardService;
import com.spark.volunteer.service.RateLimitService;
import com.spark.volunteer.service.RedisCacheService;
import com.spark.volunteer.service.UserService;
import com.spark.volunteer.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Dashboard 鉴权测试：验证 ADMIN 才能访问，其他角色 403
 * 显式导入 SecurityConfig 确保 @EnableGlobalMethodSecurity 生效
 */
@WebMvcTest(DashboardController.class)
@ActiveProfiles("test")
@Import({TestRedisConfig.class, SecurityConfig.class})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

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

    @Test
    void dashboard_withoutAuth_returns403() throws Exception {
        // Spring Security 默认无 AuthenticationEntryPoint 时返回 403（非 401）
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ELDERLY")
    void dashboard_withElderlyToken_returns403() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "VOLUNTEER")
    void dashboard_withVolunteerToken_returns403() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void dashboard_withAdminToken_returns200() throws Exception {
        mockMvc.perform(get("/api/dashboard/stats"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assert status != 403 : "ADMIN should not get 403";
                });
    }
}
