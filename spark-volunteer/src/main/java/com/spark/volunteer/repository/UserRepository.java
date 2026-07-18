package com.spark.volunteer.repository;

import com.spark.volunteer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 * 继承JpaRepository，自动拥有CRUD功能
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查找用户
    Optional<User> findByUsername(String username);

    // 根据手机号查找用户
    Optional<User> findByPhone(String phone);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // 检查手机号是否存在
    boolean existsByPhone(String phone);

    // 根据角色查找用户
    List<User> findByRole(String role);

    // 根据角色统计用户数
    long countByRole(String role);

    // 查询所有不重复的社区名
    @Query("SELECT DISTINCT u.community FROM User u WHERE u.community IS NOT NULL")
    List<String> findDistinctCommunities();
    
    // 根据openid查找用户
    User findByOpenid(String openid);

    // 根据角色和社区查找用户（同社区志愿者）
    List<User> findByRoleAndCommunity(String role, String community);

    // 根据亲属关联的老人ID查找子女/亲属
    List<User> findByParentId(Long parentId);

    // ===== 原子操作（防竞态） =====

    /** 原子增加积分: UPDATE user SET points = points + :delta WHERE id = :userId */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.points = COALESCE(u.points, 0) + :delta WHERE u.id = :userId")
    int addPointsAtomically(@Param("userId") Long userId, @Param("delta") int delta);

    /** 原子增加完成任务数 */
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.completedTasks = COALESCE(u.completedTasks, 0) + :delta WHERE u.id = :userId")
    int addCompletedTasksAtomically(@Param("userId") Long userId, @Param("delta") int delta);
}
