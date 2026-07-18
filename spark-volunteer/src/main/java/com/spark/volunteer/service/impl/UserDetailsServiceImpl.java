package com.spark.volunteer.service.impl;

import com.spark.volunteer.entity.User;
import com.spark.volunteer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security UserDetailsService 实现
 *
 * 从数据库加载用户信息，供 Spring Security 认证框架使用。<br>
 * 注意：本项目使用 JWT 令牌认证（{@code JwtAuthenticationFilter}），
 * 此 Service 主要用于满足 Spring Security 自动配置要求，
 * 消除 "Using generated security password" 警告。
 *
 * @see com.spark.volunteer.config.JwtAuthenticationFilter
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        logger.debug("加载用户详情: username={}, role={}", user.getUsername(), user.getRole());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority(user.getRole())))
                .accountExpired(false)
                .accountLocked(user.getIsDeleted() != null && user.getIsDeleted() == 1)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
