package com.spark.volunteer.service;

import java.util.List;
import java.util.Map;

/**
 * 亲属关系服务接口
 */
public interface FamilyService {

    /**
     * 绑定亲属关系
     */
    Map<String, Object> bindFamily(Long familyUserId, Long elderlyUserId, String relation);

    /**
     * 解绑亲属关系
     */
    void unbindFamily(Long familyUserId);

    /**
     * 获取老人的所有亲属成员
     */
    List<Map<String, Object>> getFamilyMembers(Long elderlyId);

    /**
     * 更新微信openid
     */
    void updateWechatOpenid(Long userId, String openid);
}
