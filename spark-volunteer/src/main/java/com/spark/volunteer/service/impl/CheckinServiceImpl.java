package com.spark.volunteer.service.impl;

import com.spark.volunteer.entity.Checkin;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.exception.BusinessException;
import com.spark.volunteer.exception.NotFoundException;
import com.spark.volunteer.repository.CheckinRepository;
import com.spark.volunteer.repository.UserRepository;
import com.spark.volunteer.service.CheckinService;
import com.spark.volunteer.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 签到服务实现类
 * 实现签到相关的服务方法
 */
@Service
public class CheckinServiceImpl implements CheckinService {

    private static final Logger logger = LoggerFactory.getLogger(CheckinServiceImpl.class);

    @Autowired
    private CheckinRepository checkinRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisCacheService redisCacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Checkin checkin(Long userId) {
        // 检查用户是否存在
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new NotFoundException("用户", userId);
        }

        // 设置签到日期
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Redis SETBIT 检查今天是否已签到（O(1) 位运算，比查数据库快）
        if (redisCacheService.hasCheckedInToday(userId, dayOfMonth)) {
            throw new BusinessException("今天已经签到过了");
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date checkinDate = calendar.getTime();

        // 创建签到记录
        Checkin checkin = new Checkin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(checkinDate);
        checkin.setCheckinTime(now);
        checkin.setStatus("SUCCESS");

        // 保存签到记录到数据库
        Checkin savedCheckin = checkinRepository.save(checkin);

        // Redis：标记今天已签到（SETBIT）
        redisCacheService.markCheckin(userId, dayOfMonth);

        // 从 Redis 计算连续签到天数，更新用户信息
        long streak = redisCacheService.getCheckinStreak(userId, dayOfMonth);
        user.setLastCheckin(checkinDate);
        user.setCheckinStreak((int) streak);
        int pointsReward = 10;
        // 连续签到每满7天额外奖励30积分
        if (streak > 0 && streak % 7 == 0) {
            pointsReward += 30;
            logger.info("用户 {} 连续签到 {} 天，额外奖励 30 积分", userId, streak);
        }
        userRepository.addPointsAtomically(userId, pointsReward);

        return savedCheckin;
    }

    @Override
    public Checkin getCheckinById(Long id) {
        return checkinRepository.findById(id).orElse(null);
    }

    @Override
    public List<Checkin> getCheckinsByUserId(Long userId) {
        return checkinRepository.findByUserId(userId);
    }

    @Override
    public List<Checkin> getCheckinsByUserIdAndDateRange(Long userId, Date startDate, Date endDate) {
        return checkinRepository.findByUserIdAndCheckinDateBetween(userId, startDate, endDate);
    }

    @Override
    public boolean hasCheckedInToday(Long userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();

        Checkin checkin = checkinRepository.findByUserIdAndCheckinDate(userId, today);
        return checkin != null;
    }

    @Override
    public int getCheckinStreak(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new NotFoundException("用户", userId);
        }
        return user.getCheckinStreak() != null ? user.getCheckinStreak() : 0;
    }

    @Override
    public Page<Checkin> getCheckinsByUserId(Long userId, Pageable pageable) {
        return checkinRepository.findByUserId(userId, pageable);
    }

    @Override
    public long getTotalCheckins(Long userId) {
        return checkinRepository.countByUserId(userId);
    }
}