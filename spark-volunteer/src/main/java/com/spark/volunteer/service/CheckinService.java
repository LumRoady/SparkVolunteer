package com.spark.volunteer.service;

import com.spark.volunteer.entity.Checkin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Date;
import java.util.List;

/**
 * 签到服务接口
 * 定义签到相关的服务方法
 */
public interface CheckinService {

    // 用户签到
    Checkin checkin(Long userId);

    // 根据ID获取签到记录
    Checkin getCheckinById(Long id);

    // 获取用户的所有签到记录
    List<Checkin> getCheckinsByUserId(Long userId);

    // 获取用户在指定日期范围内的签到记录
    List<Checkin> getCheckinsByUserIdAndDateRange(Long userId, Date startDate, Date endDate);

    // 检查用户今天是否已签到
    boolean hasCheckedInToday(Long userId);

    // 获取用户的连续签到天数
    int getCheckinStreak(Long userId);

    // 分页获取用户的签到记录
    Page<Checkin> getCheckinsByUserId(Long userId, Pageable pageable);

    // 统计用户的总签到次数
    long getTotalCheckins(Long userId);
}