package com.spark.volunteer.service;

import com.spark.volunteer.entity.User;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.exception.ForbiddenException;
import com.spark.volunteer.exception.UnauthorizedException;
import com.spark.volunteer.repository.UserRepository;
import com.spark.volunteer.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setPhone("13800138000");
        testUser.setRole("ELDERLY");
        testUser.setIsDeleted(0);
    }

    @Test
    void login_success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.login("testuser", "password123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_wrongPassword_throwsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "$2a$10$encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.login("testuser", "wrongPassword"));
    }

    @Test
    void login_disabledUser_throwsException() {
        testUser.setIsDeleted(1);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "$2a$10$encodedPassword")).thenReturn(true);

        assertThrows(ForbiddenException.class, () -> userService.login("testuser", "password123"));
    }

    @Test
    void register_success() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("plainPassword");
        newUser.setPhone("13900139000");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByPhone("13900139000")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$10$newEncoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        User result = userService.register(newUser);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("$2a$10$newEncoded", result.getPassword());
        assertEquals("ELDERLY", result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateUsername_throwsException() {
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setPassword("plainPassword");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.register(newUser));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_withAdminRole_forcedToElderly() {
        // 安全测试：自注册传入 role=ADMIN 应被强制覆盖为 ELDERLY
        User newUser = new User();
        newUser.setUsername("hacker");
        newUser.setPassword("plainPassword");
        newUser.setRole("ADMIN"); // 恶意设置

        when(userRepository.existsByUsername("hacker")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register(newUser);

        assertEquals("ELDERLY", result.getRole(), "自注册必须强制为 ELDERLY 角色");
    }

    @Test
    void registerWithRole_validAdminRole() {
        User newUser = new User();
        newUser.setUsername("admin2");
        newUser.setPassword("plainPassword");

        when(userRepository.existsByUsername("admin2")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.registerWithRole(newUser, "ADMIN");

        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void registerWithRole_invalidRole_fallsBackToElderly() {
        User newUser = new User();
        newUser.setUsername("user3");
        newUser.setPassword("plainPassword");

        when(userRepository.existsByUsername("user3")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.registerWithRole(newUser, "SUPERUSER");

        assertEquals("ELDERLY", result.getRole(), "非法角色应回退为 ELDERLY");
    }
}
