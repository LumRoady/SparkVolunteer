package com.spark.volunteer.repository;

import com.spark.volunteer.entity.Checkin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * 签到记录数据访问接口
 * 继承JpaRepository，自动拥有CRUD功能
 */
@Repository
public interface CheckinRepository extends JpaRepository<Checkin, Long> {

    // 根据用户ID查找签到记录
    List<Checkin> findByUserId(Long userId);
    org.springframework.data.domain.Page<Checkin> findByUserId(Long userId, org.springframework.data.domain.Pageable pageable);

    // 根据用户ID和签到日期查找签到记录
    Checkin findByUserIdAndCheckinDate(Long userId, Date checkinDate);

    // 根据用户ID和签到日期范围查找签到记录
    List<Checkin> findByUserIdAndCheckinDateBetween(Long userId, Date startDate, Date endDate);

    // 统计用户的签到次数
    long countByUserId(Long userId);
}