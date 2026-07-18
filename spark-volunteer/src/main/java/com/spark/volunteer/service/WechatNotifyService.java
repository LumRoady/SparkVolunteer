package com.spark.volunteer.service;

import com.spark.volunteer.entity.Task;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 微信模板消息通知服务
 * 当老人触发紧急求助时，向绑定亲属发送微信模板消息
 *
 * 当前为框架代码，access_token 缓存逻辑完整，
 * 实际发送需配置 wechat.appid / wechat.secret / wechat.template_id
 */
@Service
public class WechatNotifyService {

    private static final Logger logger = LoggerFactory.getLogger(WechatNotifyService.class);

    // access_token 缓存
    private String cachedAccessToken;
    private long tokenExpireTime;

    @Value("${wechat.appid:}")
    private String appid;

    @Value("${wechat.secret:}")
    private String secret;

    @Value("${wechat.template_id:}")
    private String templateId;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 紧急求助后异步向亲属发送通知
     */
    @Async
    public void sendEmergencyNotify(Task task) {
        if (appid.isEmpty() || secret.isEmpty()) {
            logger.info("微信通知未配置，跳过发送。taskId={}", task.getId());
            return;
        }

        User elderly = userRepository.findById(task.getUserId()).orElse(null);
        if (elderly == null) return;

        // 查找绑定的亲属
        List<User> familyMembers = findFamilyMembers(elderly.getId());
        if (familyMembers.isEmpty()) {
            logger.info("老人 {} 没有绑定的亲属，跳过通知", elderly.getId());
            return;
        }

        String accessToken = getAccessToken();
        if (accessToken == null) {
            logger.error("获取微信 access_token 失败，跳过通知");
            return;
        }

        for (User family : familyMembers) {
            if (family.getWechatOpenid() == null || family.getWechatOpenid().isEmpty()) continue;

            String timeStr = new SimpleDateFormat("MM-dd HH:mm").format(task.getCreateTime());
            String content = "【紧急提醒】您的家人" +
                    (elderly.getNickname() != null ? elderly.getNickname() : "老人") +
                    "于" + timeStr + "发出了紧急求助，" +
                    "志愿者正在赶往" +
                    (task.getLocation() != null ? task.getLocation() : "求助地址") +
                    "，请关注。";

            boolean success = sendTemplateMessage(accessToken, family.getWechatOpenid(), templateId, content);
            if (success) {
                logger.info("微信通知已发送: 亲属={}, 老人={}", family.getPhone(), elderly.getNickname());
            } else {
                logger.warn("微信通知发送失败: 亲属={}", family.getPhone());
            }
        }
    }

    /**
     * 查找老人的亲属（通过 parent_id 字段关联）
     */
    private List<User> findFamilyMembers(Long elderlyId) {
        return userRepository.findByParentId(elderlyId);
    }

    /**
     * 获取微信 access_token（带2小时缓存）
     */
    private synchronized String getAccessToken() {
        if (cachedAccessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedAccessToken;
        }

        try {
            String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="
                    + appid + "&secret=" + secret;
            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.getForObject(url, Map.class);

            if (resp != null && resp.containsKey("access_token")) {
                cachedAccessToken = (String) resp.get("access_token");
                int expiresIn = (Integer) resp.getOrDefault("expires_in", 7200);
                tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L; // 提前5分钟刷新
                logger.info("微信 access_token 已刷新，有效期: {} 秒", expiresIn);
                return cachedAccessToken;
            } else {
                logger.error("获取 access_token 失败: {}", resp);
                return null;
            }
        } catch (Exception e) {
            logger.error("请求微信 access_token 异常", e);
            return null;
        }
    }

    /**
     * 发送模板消息
     */
    @SuppressWarnings("unchecked")
    private boolean sendTemplateMessage(String accessToken, String openid, String templateId, String content) {
        try {
            String url = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=" + accessToken;

            Map<String, Object> body = new HashMap<>();
            body.put("touser", openid);
            body.put("template_id", templateId);
            body.put("data", buildTemplateData(content));

            Map<String, Object> resp = restTemplate.postForObject(url, body, Map.class);
            if (resp != null && "0".equals(String.valueOf(resp.get("errcode")))) {
                return true;
            } else {
                logger.error("模板消息发送失败: {}", resp);
                return false;
            }
        } catch (Exception e) {
            logger.error("发送模板消息异常", e);
            return false;
        }
    }

    private Map<String, Object> buildTemplateData(String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("first",    kv("紧急求助通知", "#FF0000"));
        data.put("keyword1", kv("紧急求助", "#173177"));
        data.put("keyword2", kv(content, "#173177"));
        data.put("remark",   kv("请尽快与家人联系确认情况", "#888888"));
        return data;
    }

    private Map<String, String> kv(String value, String color) {
        Map<String, String> m = new HashMap<>();
        m.put("value", value);
        m.put("color", color);
        return m;
    }
}
