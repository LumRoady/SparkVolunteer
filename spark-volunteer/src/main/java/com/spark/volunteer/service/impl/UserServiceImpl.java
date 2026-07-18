package com.spark.volunteer.service.impl;

import com.spark.volunteer.entity.User;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.exception.ForbiddenException;
import com.spark.volunteer.exception.NotFoundException;
import com.spark.volunteer.exception.UnauthorizedException;
import com.spark.volunteer.repository.UserRepository;
import com.spark.volunteer.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public User register(User user) {
        // 1. 检查用户名是否存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 检查手机号是否存在
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new BusinessException("手机号已存在");
        }

        // 3. 安全：自注册强制 ELDERLY 角色，忽略客户端传入的任何值
        user.setRole("ELDERLY");
        if (user.getIsDeleted() == null) {
            user.setIsDeleted(0);
        }

        // 4. 密码加密存储
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 5. 保存到数据库
        return userRepository.save(user);
    }

    @Override
    public User registerWithRole(User user, String role) {
        // 1. 检查用户名是否存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 检查手机号是否存在
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new BusinessException("手机号已存在");
        }

        // 3. 角色白名单校验，非法角色回退为 ELDERLY
        Set<String> validRoles = new java.util.HashSet<>(java.util.Arrays.asList("ADMIN", "VOLUNTEER", "ELDERLY"));
        user.setRole(validRoles.contains(role) ? role : "ELDERLY");
        if (user.getIsDeleted() == null) {
            user.setIsDeleted(0);
        }

        // 4. 密码加密存储
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // 5. 保存到数据库
        return userRepository.save(user);
    }

    @Override
    public User login(String account, String password) {
        // 1. 先按用户名查找，找不到再按手机号查找
        User user = userRepository.findByUsername(account)
                .orElseGet(() -> userRepository.findByPhone(account)
                        .orElseThrow(() -> new NotFoundException("用户", account)));

        // 2. 使用 BCrypt 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("密码错误");
        }

        // 3. 检查用户是否被禁用
        if (user.getIsDeleted() != 0) {
            throw new ForbiddenException("用户已被禁用");
        }

        // 4. 更新最后登录时间
        user.setUpdateTime(new Date());
        userRepository.save(user);

        return user;
    }

    @Value("${wechat.appid:}")
    private String wechatAppId;

    @Value("${wechat.secret:}")
    private String wechatSecret;

    @Override
    public User loginByWechat(String code) {
        String openid = resolveWechatOpenid(code);

        // 根据openid查找用户
        User user = userRepository.findByOpenid(openid);

        if (user == null) {
            // 首次登录，自动注册
            user = new User();
            user.setOpenid(openid);
            user.setNickname("微信用户");
            user.setRole("ELDERLY"); // 默认角色为老人
            user.setIsDeleted(0);
            user = userRepository.save(user);
        }

        return user;
    }

    /**
     * 通过微信 code 换取 openid
     * 调用微信官方 jscode2session 接口
     */
    private String resolveWechatOpenid(String code) {
        if (wechatAppId == null || wechatAppId.isEmpty() || wechatSecret == null || wechatSecret.isEmpty()) {
            logger.warn("微信 AppID/Secret 未配置，使用 code 哈希作为临时 openid");
            return "wx_" + Integer.toHexString(code.hashCode());
        }
        try {
            String url = String.format(
                "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
                wechatAppId, wechatSecret, code);
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> resp = restTemplate.getForObject(url, java.util.Map.class);
            if (resp != null && resp.containsKey("openid")) {
                return (String) resp.get("openid");
            }
            logger.error("微信登录失败: {}", resp);
        } catch (Exception e) {
            logger.error("调用微信API异常", e);
        }
        // 微信 API 调用失败时回退
        return "wx_" + Integer.toHexString(code.hashCode());
    }

    @Override
    @Cacheable(value = "user", key = "#id", unless = "#result == null")
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("用户", id));
    }

    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("用户", username));
    }

    @Override
    @CacheEvict(value = "user", key = "#user.id")
    public User updateUser(User user) {
        // 1. 先获取现有用户
        User existingUser = getUserById(user.getId());

        // 2. 更新允许修改的字段
        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }
        if (user.getNickname() != null) {
            existingUser.setNickname(user.getNickname());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        if (user.getAddress() != null) {
            existingUser.setAddress(user.getAddress());
        }
        if (user.getProvince() != null) {
            existingUser.setProvince(user.getProvince());
        }
        if (user.getCity() != null) {
            existingUser.setCity(user.getCity());
        }
        if (user.getCommunity() != null) {
            existingUser.setCommunity(user.getCommunity());
        }
        // 密码字段：仅当显式设置时才更新（加密后的密码）
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(user.getPassword());
        }

        // 3. 保存更新
        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setIsDeleted(1);
        userRepository.save(user);
    }

    @Override
    public List<User> getAllElderlyUsers() {
        return userRepository.findByRole("ELDERLY");
    }

    @Override
    public List<User> getAllVolunteers() {
        return userRepository.findByRole("VOLUNTEER");
    }

    @Override
    public boolean isElderlyUser(Long userId) {
        User user = getUserById(userId);
        return "ELDERLY".equals(user.getRole());
    }

    @Override
    public boolean isVolunteer(Long userId) {
        User user = getUserById(userId);
        return "VOLUNTEER".equals(user.getRole());
    }

    @Override
    public boolean isAdmin(Long userId) {
        User user = getUserById(userId);
        return "ADMIN".equals(user.getRole());
    }

    @Override
    @CacheEvict(value = "user", key = "#userId")
    public User updatePoints(Long userId, int points) {
        User user = getUserById(userId);
        user.setPoints(user.getPoints() != null ? user.getPoints() + points : points);
        return userRepository.save(user);
    }

    @Override
    public Integer getPoints(Long userId) {
        User user = getUserById(userId);
        return user.getPoints() != null ? user.getPoints() : 0;
    }

    @Override
    @CacheEvict(value = "user", key = "#userId")
    public User updateCompletedTasks(Long userId, int increment) {
        User user = getUserById(userId);
        user.setCompletedTasks(user.getCompletedTasks() != null ? user.getCompletedTasks() + increment : increment);
        return userRepository.save(user);
    }

    @Override
    public Integer getCompletedTasks(Long userId) {
        User user = getUserById(userId);
        return user.getCompletedTasks() != null ? user.getCompletedTasks() : 0;
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public List<User> getAllUsers() {
        return (List<User>) userRepository.findAll();
    }
}
