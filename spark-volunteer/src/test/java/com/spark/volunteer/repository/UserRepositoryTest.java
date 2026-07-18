package com.spark.volunteer.repository;

import com.spark.volunteer.config.TestRedisConfig;
import com.spark.volunteer.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * UserRepository 数据访问测试（@DataJpaTest + H2）
 */
@DataJpaTest
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestRedisConfig.class)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_returnsUser() {
        User user = new User();
        user.setUsername("repoTest");
        user.setPassword("encodedPassword");
        user.setRole("ELDERLY");
        user.setIsDeleted(0);
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByUsername("repoTest");

        assertTrue(found.isPresent());
        assertEquals("repoTest", found.get().getUsername());
        assertEquals("ELDERLY", found.get().getRole());
    }

    @Test
    void findByUsername_notFound() {
        Optional<User> found = userRepository.findByUsername("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void findByPhone_returnsUser() {
        User user = new User();
        user.setUsername("phoneTest");
        user.setPassword("encodedPassword");
        user.setPhone("13800138000");
        user.setRole("VOLUNTEER");
        user.setIsDeleted(0);
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByPhone("13800138000");

        assertTrue(found.isPresent());
        assertEquals("VOLUNTEER", found.get().getRole());
    }

    @Test
    void existsByUsername_returnsCorrectly() {
        User user = new User();
        user.setUsername("existTest");
        user.setPassword("encodedPassword");
        user.setRole("ELDERLY");
        user.setIsDeleted(0);
        entityManager.persistAndFlush(user);

        assertTrue(userRepository.existsByUsername("existTest"));
        assertFalse(userRepository.existsByUsername("notExist"));
    }

    @Test
    void existsByPhone_returnsCorrectly() {
        User user = new User();
        user.setUsername("phoneExist");
        user.setPassword("encodedPassword");
        user.setPhone("13900139000");
        user.setRole("ELDERLY");
        user.setIsDeleted(0);
        entityManager.persistAndFlush(user);

        assertTrue(userRepository.existsByPhone("13900139000"));
        assertFalse(userRepository.existsByPhone("00000000000"));
    }

    @Test
    void findByRole_returnsFilteredUsers() {
        User admin = new User();
        admin.setUsername("admin1");
        admin.setPassword("pw");
        admin.setRole("ADMIN");
        admin.setIsDeleted(0);
        entityManager.persist(admin);

        User volunteer = new User();
        volunteer.setUsername("vol1");
        volunteer.setPassword("pw");
        volunteer.setRole("VOLUNTEER");
        volunteer.setIsDeleted(0);
        entityManager.persistAndFlush(volunteer);

        assertEquals(1, userRepository.findByRole("ADMIN").size());
        assertEquals(1, userRepository.findByRole("VOLUNTEER").size());
        assertEquals(0, userRepository.findByRole("ELDERLY").size());
    }
}
