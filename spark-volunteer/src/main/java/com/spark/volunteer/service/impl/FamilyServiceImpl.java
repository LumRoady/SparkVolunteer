package com.spark.volunteer.service.impl;

import com.spark.volunteer.entity.User;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.repository.UserRepository;
import com.spark.volunteer.service.FamilyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FamilyServiceImpl implements FamilyService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Map<String, Object> bindFamily(Long familyUserId, Long elderlyUserId, String relation) {
        User familyUser = userRepository.findById(familyUserId).orElse(null);
        User elderlyUser = userRepository.findById(elderlyUserId).orElse(null);

        if (familyUser == null || elderlyUser == null) {
            throw new BusinessException("用户不存在");
        }

        familyUser.setParentId(elderlyUserId);
        familyUser.setRelation(relation);
        userRepository.save(familyUser);

        Map<String, Object> result = new HashMap<>();
        result.put("familyUser", familyUser.getName());
        result.put("elderlyUser", elderlyUser.getName());
        result.put("relation", relation);
        result.put("status", "绑定成功");
        return result;
    }

    @Override
    public void unbindFamily(Long familyUserId) {
        User familyUser = userRepository.findById(familyUserId).orElse(null);
        if (familyUser == null) {
            throw new BusinessException("用户不存在");
        }

        familyUser.setParentId(null);
        familyUser.setRelation(null);
        userRepository.save(familyUser);
    }

    @Override
    public List<Map<String, Object>> getFamilyMembers(Long elderlyId) {
        // 使用 findByParentId 索引查询，替代全表扫描
        List<User> familyUsers = userRepository.findByParentId(elderlyId);
        return familyUsers.stream().map(u -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("name", u.getName());
            m.put("phone", u.getPhone());
            m.put("relation", u.getRelation());
            m.put("wechatOpenid", u.getWechatOpenid() != null);
            return m;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateWechatOpenid(Long userId, String openid) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setWechatOpenid(openid);
        userRepository.save(user);
    }
}
