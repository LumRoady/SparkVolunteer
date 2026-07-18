/*
 * Copyright (c) 2026 星火众擎 (Spark Volunteer Platform)
 * All rights reserved.
 *
 * 本软件著作权归星火众擎团队所有，受《中华人民共和国著作权法》保护。
 * 未经著作权人书面许可，任何单位或个人不得复制、修改、传播、发行本软件。
 */

package com.spark.volunteer.config;

import com.spark.volunteer.entity.Task;
import com.spark.volunteer.entity.User;
import com.spark.volunteer.repository.TaskRepository;
import com.spark.volunteer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

/**
 * 开发环境测试数据初始化器
 * 仅在 dev 环境下运行，创建测试账号和示例任务
 */
@Configuration
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        long userCount = userRepository.count();
        logger.info("当前数据库用户数量: {}", userCount);

        // 如果用户数量为0，创建测试账号
        if (userCount == 0) {
            logger.info("开始创建测试账号...");

            // 管理员账号（超级账号）
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("系统管理员");
            admin.setRole("ADMIN");
            admin.setName("管理员");
            admin.setPhone("13800000001");
            admin.setCommunity("星火社区");
            admin.setPoints(9999);
            admin.setIsDeleted(0);
            userRepository.save(admin);
            logger.info("✅ 管理员账号: admin / admin123");

            // 志愿者账号
            User volunteer = new User();
            volunteer.setUsername("volunteer1");
            volunteer.setPassword(passwordEncoder.encode("123456"));
            volunteer.setNickname("热心志愿者小王");
            volunteer.setRole("VOLUNTEER");
            volunteer.setName("王小明");
            volunteer.setPhone("13800000002");
            volunteer.setCommunity("星火社区");
            volunteer.setPoints(520);
            volunteer.setCompletedTasks(12);
            volunteer.setIsDeleted(0);
            userRepository.save(volunteer);
            logger.info("✅ 志愿者账号: volunteer1 / 123456");

            // 老人账号
            User elderly = new User();
            elderly.setUsername("elderly1");
            elderly.setPassword(passwordEncoder.encode("123456"));
            elderly.setNickname("张奶奶");
            elderly.setRole("ELDERLY");
            elderly.setName("张秀英");
            elderly.setPhone("13800000003");
            elderly.setAddress("北京市朝阳区某某小区3号楼1单元101");
            elderly.setCommunity("星火社区");
            elderly.setPoints(120);
            elderly.setIsDeleted(0);
            userRepository.save(elderly);
            logger.info("✅ 老人账号: elderly1 / 123456");

            logger.info("测试账号创建完成，共3个账号");
        } else {
            logger.info("数据库已有用户，跳过测试账号创建");
        }

        long taskCount = taskRepository.count();
        logger.info("当前数据库任务数量: {}", taskCount);

        // 如果任务数量少于5个，添加示例任务
        if (taskCount < 5) {
            logger.info("开始添加示例任务...");

            // 示例任务1（紧急求助-待接单）
            Task task1 = new Task();
            task1.setUserId(3L);  // 老人用户
            task1.setDeviceId("DEV001");
            task1.setType("sos");
            task1.setTitle("紧急求助-老人摔倒");
            task1.setContent("老人在家摔倒，需要紧急医疗救助，请附近志愿者速来帮忙！");
            task1.setStatus(0);
            task1.setLocation("北京市朝阳区某某小区3号楼1单元101");
            task1.setLatitude(39.9042);
            task1.setLongitude(116.4074);
            task1.setTaskNo(1);
            task1.setCreateTime(new Date());
            taskRepository.save(task1);

            // 示例任务2（生活服务-待接单）
            Task task2 = new Task();
            task2.setUserId(3L);
            task2.setDeviceId("DEV001");
            task2.setType("life_service");
            task2.setTitle("帮忙买菜");
            task2.setContent("需要帮忙购买一些日常蔬菜和水果，包括西红柿、鸡蛋、牛奶、青菜等");
            task2.setStatus(0);
            task2.setLocation("北京市朝阳区某某小区2号楼1单元302");
            task2.setLatitude(39.9052);
            task2.setLongitude(116.4084);
            task2.setTaskNo(2);
            task2.setCreateTime(new Date());
            taskRepository.save(task2);

            // 示例任务3（日常咨询-已接单）
            Task task3 = new Task();
            task3.setUserId(3L);
            task3.setDeviceId("DEV001");
            task3.setType("consultation");
            task3.setTitle("高血压健康咨询");
            task3.setContent("想咨询一些关于高血压的健康问题，最近血压有点不稳定，需要注意什么？");
            task3.setStatus(1);
            task3.setReceiverId(2L);  // 志愿者
            task3.setLocation("北京市朝阳区某某小区3号楼2单元401");
            task3.setLatitude(39.9062);
            task3.setLongitude(116.4094);
            task3.setTaskNo(3);
            task3.setCreateTime(new Date());
            task3.setAcceptTime(new Date());
            taskRepository.save(task3);

            // 示例任务4（生活服务-已完成）
            Task task4 = new Task();
            task4.setUserId(3L);
            task4.setDeviceId("DEV001");
            task4.setType("life_service");
            task4.setTitle("帮忙取快递");
            task4.setContent("帮忙取一下快递，快递单号是SF1234567890，在小区南门快递柜");
            task4.setStatus(2);
            task4.setReceiverId(2L);
            task4.setLocation("北京市朝阳区某某小区4号楼3单元201");
            task4.setLatitude(39.9072);
            task4.setLongitude(116.4104);
            task4.setTaskNo(4);
            task4.setCreateTime(new Date());
            task4.setAcceptTime(new Date());
            task4.setFinishTime(new Date());
            task4.setRating(5);
            taskRepository.save(task4);

            // 示例任务5（紧急求助-已取消）
            Task task5 = new Task();
            task5.setUserId(3L);
            task5.setDeviceId("DEV001");
            task5.setType("sos");
            task5.setTitle("紧急求助-断电维修");
            task5.setContent("家里突然断电，需要电工帮忙检查电路，已联系物业处理");
            task5.setStatus(3);
            task5.setLocation("北京市朝阳区某某小区5号楼1单元601");
            task5.setLatitude(39.9082);
            task5.setLongitude(116.4114);
            task5.setTaskNo(5);
            task5.setCreateTime(new Date());
            taskRepository.save(task5);

            logger.info("示例任务添加完成，共5个任务（覆盖4种状态）");
        } else {
            logger.info("数据库已有足够任务数据，跳过");
        }
    }
}
