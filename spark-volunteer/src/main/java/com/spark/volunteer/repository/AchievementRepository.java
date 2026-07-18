package com.spark.volunteer.repository;

import com.spark.volunteer.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    List<Achievement> findByUserId(Long userId);
    org.springframework.data.domain.Page<Achievement> findByUserId(Long userId, org.springframework.data.domain.Pageable pageable);

    boolean existsByUserIdAndAchievementType(Long userId, String achievementType);

    long countByUserId(Long userId);
}
