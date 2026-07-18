package com.spark.volunteer.integration;

import com.spark.volunteer.config.TestRedisConfig;
import com.spark.volunteer.dto.request.LoginRequest;
import com.spark.volunteer.dto.request.RegisterRequest;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 认证流程集成测试：注册 → 登录 → 角色安全
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestRedisConfig.class)
class AuthFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_withoutRole_defaultsToElderly() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser1");
        request.setPassword("password123");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/register", request, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(200, body.get("code"));

        // 验证 data 中 role 为 ELDERLY
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertEquals("ELDERLY", data.get("role"));
    }

    @Test
    void register_withAdminRole_forcedToElderly() {
        // 安全测试：即使传入 role=ADMIN 也应被强制为 ELDERLY
        Map<String, Object> request = Map.of(
                "username", "hacker",
                "password", "password123",
                "role", "ADMIN"
        );

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/register", request, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertEquals("ELDERLY", data.get("role"), "自注册必须强制为 ELDERLY 角色");
    }

    @Test
    void register_thenLogin_success() {
        // 先注册
        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setUsername("logintest");
        regRequest.setPassword("password123");
        restTemplate.postForEntity("/api/auth/register", regRequest, Map.class);

        // 再登录
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("logintest");
        loginRequest.setPassword("password123");

        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertEquals(200, body.get("code"));

        Map<String, Object> data = (Map<String, Object>) body.get("data");
        assertNotNull(data.get("token"), "登录应返回 token");
        Map<String, Object> user = (Map<String, Object>) data.get("user");
        assertNotNull(user);
        assertEquals("logintest", user.get("username"));
        assertEquals("ELDERLY", user.get("role"));

        // 验证不暴露敏感字段
        assertFalse(user.containsKey("password"), "不应暴露 password");
        assertFalse(user.containsKey("openid"), "不应暴露 openid");
        assertFalse(user.containsKey("wechatOpenid"), "不应暴露 wechatOpenid");
        assertFalse(user.containsKey("parentId"), "不应暴露 parentId");
        assertFalse(user.containsKey("isDeleted"), "不应暴露 isDeleted");
    }

    @Test
    void login_wrongPassword_returnsError() {
        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setUsername("wrongpw");
        regRequest.setPassword("password123");
        restTemplate.postForEntity("/api/auth/register", regRequest, Map.class);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wrongpw");
        loginRequest.setPassword("wrongpassword");

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "/api/auth/login", loginRequest, Map.class);
            // 密码错误应返回非 200
            assertNotEquals(200,
                    response.getBody() != null ? response.getBody().get("code") : null);
        } catch (Exception e) {
            // TestRestTemplate 遇到 401 挑战时抛 ResourceAccessException，这也是预期行为
            assertTrue(e.getMessage() != null, "密码错误应导致服务器返回错误");
        }
    }

    @Test
    void dashboard_requiresAdminRole() {
        // 注册普通用户
        RegisterRequest regRequest = new RegisterRequest();
        regRequest.setUsername("elderly1");
        regRequest.setPassword("password123");
        restTemplate.postForEntity("/api/auth/register", regRequest, Map.class);

        // 登录获取 token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("elderly1");
        loginRequest.setPassword("password123");
        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                "/api/auth/login", loginRequest, Map.class);
        String token = (String) ((Map<String, Object>) loginResponse.getBody().get("data")).get("token");

        // 用普通用户 token 访问 dashboard → 应 403
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        org.springframework.http.HttpEntity<Void> entity = new org.springframework.http.HttpEntity<>(headers);

        ResponseEntity<Map> dashboardResponse = restTemplate.exchange(
                "/api/dashboard/stats", org.springframework.http.HttpMethod.GET, entity, Map.class);

        assertEquals(HttpStatus.FORBIDDEN, dashboardResponse.getStatusCode());
    }
}
