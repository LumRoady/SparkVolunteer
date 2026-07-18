package com.spark.volunteer.service;

import com.spark.volunteer.entity.User;
import java.util.List;

public interface UserService {

    // 注册用户（自注册，强制 ELDERLY 角色）
    User register(User user);

    // 注册用户（管理员创建，允许指定角色，白名单校验）
    User registerWithRole(User user, String role);

    // 登录
    User login(String username, String password);

    // 微信小程序登录
    User loginByWechat(String code);

    // 根据ID获取用户
    User getUserById(Long id);

    // 根据用户名获取用户
    User getUserByUsername(String username);

    // 更新用户信息
    User updateUser(User user);

    // 删除用户（软删除，修改状态）
    void deleteUser(Long id);

    // 获取所有老年人用户
    List<User> getAllElderlyUsers();

    // 获取所有志愿者用户
    List<User> getAllVolunteers();

    // 检查用户是否为老年人
    boolean isElderlyUser(Long userId);

    // 检查用户是否为志愿者
    boolean isVolunteer(Long userId);

    // 检查用户是否为管理员
    boolean isAdmin(Long userId);

    // 更新用户积分
    User updatePoints(Long userId, int points);

    // 获取用户积分
    Integer getPoints(Long userId);

    // 更新用户完成的任务数量
    User updateCompletedTasks(Long userId, int increment);

    // 获取用户完成的任务数量
    Integer getCompletedTasks(Long userId);

    // 根据角色获取用户列表
    List<User> getUsersByRole(String role);

    // 获取所有用户列表
    List<User> getAllUsers();
}
