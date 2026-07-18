package com.spark.volunteer.service;

import com.spark.volunteer.entity.Achievement;
import com.spark.volunteer.entity.Task;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.repository.AchievementRepository;
import com.spark.volunteer.repository.TaskRepository;
import com.spark.volunteer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 志愿者成长体系服务
 * 管理等级计算、成就解锁判断
 */
@Service
public class VolunteerGrowthService {

    private static final Logger logger = LoggerFactory.getLogger(VolunteerGrowthService.class);

    // 成就定义
    private static final Map<String, String[]> ACHIEVEMENTS = new LinkedHashMap<>();
    static {
        ACHIEVEMENTS.put("first_task",   new String[]{"⭐", "首次接单", "完成第1次志愿服务"});
        ACHIEVEMENTS.put("fast_response", new String[]{"⚡", "极速响应", "接单时间<1分钟，累计10次"});
        ACHIEVEMENTS.put("night_guard",  new String[]{"🌙", "深夜守护", "22:00-06:00完成服务"});
        ACHIEVEMENTS.put("streak_7",     new String[]{"🔥", "连续服务", "连续7天有服务记录"});
        ACHIEVEMENTS.put("emergency_10", new String[]{"🆘", "紧急先锋", "响应紧急求助10次"});
        ACHIEVEMENTS.put("hundred",      new String[]{"💎", "百次里程碑", "累计完成100次服务"});
    }

    @Autowired
    private AchievementRepository achievementRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 任务完成后异步检查成就
     */
    @Async
    public void checkAchievements(Long volunteerId) {
        User volunteer = userRepository.findById(volunteerId).orElse(null);
        if (volunteer == null || !"VOLUNTEER".equals(volunteer.getRole())) return;

        long completedCount = taskRepository.countByReceiverId(volunteerId);

        // 首次接单
        if (completedCount >= 1) unlockAchievement(volunteerId, "first_task");

        // 百次里程碑
        if (completedCount >= 100) unlockAchievement(volunteerId, "hundred");

        // 紧急先锋：检查紧急求助完成数
        List<Task> emergencyTasks = taskRepository.findByReceiverId(volunteerId);
        long emergencyCount = emergencyTasks.stream().filter(t -> "sos".equals(t.getType())).count();
        if (emergencyCount >= 10) unlockAchievement(volunteerId, "emergency_10");

        // 深夜守护
        boolean hasNightTask = emergencyTasks.stream().anyMatch(t -> {
            if (t.getFinishTime() == null) return false;
            Calendar cal = Calendar.getInstance();
            cal.setTime(t.getFinishTime());
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            return hour >= 22 || hour < 6;
        });
        if (hasNightTask) unlockAchievement(volunteerId, "night_guard");

        // 极速响应
        long fastCount = emergencyTasks.stream().filter(t -> t.getAcceptTime() != null &&
                (t.getAcceptTime().getTime() - t.getCreateTime().getTime()) < 60000).count();
        if (fastCount >= 10) unlockAchievement(volunteerId, "fast_response");

        // 连续7天
        checkStreakAchievement(volunteerId);

        logger.info("志愿者 {} 成就检查完成，已完成服务: {} 次", volunteerId, completedCount);
    }

    private void checkStreakAchievement(Long volunteerId) {
        List<Task> tasks = taskRepository.findByReceiverId(volunteerId);
        Set<String> days = new HashSet<>();
        for (Task t : tasks) {
            if (t.getFinishTime() != null) {
                days.add(new java.text.SimpleDateFormat("yyyy-MM-dd").format(t.getFinishTime()));
            }
        }
        // 检查最近7天是否连续
        int streak = 0;
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 7; i++) {
            String day = new java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
            if (days.contains(day)) { streak++; } else { break; }
            cal.add(Calendar.DAY_OF_YEAR, -1);
        }
        if (streak >= 7) unlockAchievement(volunteerId, "streak_7");
    }

    @Transactional(rollbackFor = Exception.class)
    public void unlockAchievement(Long userId, String type) {
        if (achievementRepository.existsByUserIdAndAchievementType(userId, type)) return;

        String[] info = ACHIEVEMENTS.get(type);
        if (info == null) return;

        Achievement a = new Achievement();
        a.setUserId(userId);
        a.setAchievementType(type);
        a.setTitle(info[1]);
        a.setDescription(info[2]);
        a.setIcon(info[0]);
        achievementRepository.save(a);
        logger.info("🏆 用户 {} 解锁成就: {}", userId, info[1]);
    }

    /**
     * 获取志愿者成长数据
     */
    public Map<String, Object> getGrowthData(Long userId) {
        Map<String, Object> data = new HashMap<>();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return data;

        long completedCount = taskRepository.countByReceiverId(userId);
        Double avgRating = taskRepository.avgRatingByReceiverId(userId);
        List<Achievement> achievements = achievementRepository.findByUserId(userId);

        // 使用积分(points)而非完成数计算等级
        int totalPoints = user.getPoints() != null ? user.getPoints() : 0;
        String level = calculateLevel(totalPoints, avgRating);
        String nextLevel = getNextLevel(level);
        int currentThreshold = getLevelThreshold(level);
        int nextThreshold = getLevelThreshold(nextLevel);
        double progress = nextThreshold > currentThreshold
                ? (totalPoints - currentThreshold) * 100.0 / (nextThreshold - currentThreshold)
                : 100;

        data.put("userId", userId);
        data.put("volunteerName", user.getName());
        data.put("level", level);
        data.put("levelIcon", getLevelIcon(level));
        data.put("completedCount", completedCount);
        data.put("totalPoints", totalPoints);
        data.put("avgRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0);
        data.put("nextLevel", nextLevel);
        data.put("progress", Math.min(100, Math.max(0, (int) progress)));
        data.put("needMore", nextThreshold - totalPoints);
        data.put("achievements", achievements);
        data.put("allAchievements", ACHIEVEMENTS.entrySet().stream().map(e -> {
            Map<String, Object> m = new HashMap<>();
            m.put("type", e.getKey());
            m.put("icon", e.getValue()[0]);
            m.put("title", e.getValue()[1]);
            m.put("desc", e.getValue()[2]);
            m.put("unlocked", achievements.stream().anyMatch(a -> e.getKey().equals(a.getAchievementType())));
            return m;
        }).toArray());

        return data;
    }

    /**
     * 计算志愿者等级（基于积分 + 评级）
     * Lv1 热心邻居：0-99分
     * Lv2 社区守护者：100-299分
     * Lv3 金牌守护者：300-999分
     * Lv4 星火使者：1000分以上
     */
    public String calculateLevel(long count, Double rating) {
        // 使用积分(points)而非完成数(count)，与论文统一
        if (count >= 1000 && (rating == null || rating >= 4.8)) return "星火使者";
        if (count >= 300  && (rating == null || rating >= 4.5)) return "金牌守护者";
        if (count >= 100  && (rating == null || rating >= 4.0)) return "社区守护者";
        return "热心邻居";
    }

    private String getNextLevel(String level) {
        switch (level) {
            case "热心邻居":  return "社区守护者";
            case "社区守护者": return "金牌守护者";
            case "金牌守护者": return "星火使者";
            default:         return "星火使者";
        }
    }

    private int getLevelThreshold(String level) {
        switch (level) {
            case "热心邻居":  return 0;
            case "社区守护者": return 100;
            case "金牌守护者": return 300;
            case "星火使者":  return 1000;
            default:         return 0;
        }
    }

    private String getLevelIcon(String level) {
        switch (level) {
            case "热心邻居":  return "🏠";
            case "社区守护者": return "🛡️";
            case "金牌守护者": return "🥇";
            case "星火使者":  return "🌟";
            default:         return "❤️";
        }
    }

    /**
     * 获取志愿者电子证书数据
     */
    public Map<String, Object> getCertificateData(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        long completedCount = taskRepository.countByReceiverId(userId);
        Double avgRating = taskRepository.avgRatingByReceiverId(userId);

        Map<String, Object> cert = new HashMap<>();
        cert.put("volunteerName", user.getName() != null ? user.getName() : user.getNickname());
        cert.put("level", calculateLevel(completedCount, avgRating));
        cert.put("totalServices", completedCount);
        cert.put("avgRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 0);
        cert.put("totalHours", completedCount * 2);
        cert.put("joinDate", user.getCreateTime() != null
                ? new SimpleDateFormat("yyyy-MM-dd").format(user.getCreateTime())
                : "2026-01-01");
        cert.put("certNumber", "XHZC-2026-" + String.format("%05d", userId));
        cert.put("phone", user.getPhone());
        return cert;
    }
}
