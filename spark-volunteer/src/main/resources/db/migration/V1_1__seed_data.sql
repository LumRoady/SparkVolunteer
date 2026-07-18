-- ============================================
-- 星火众擎 — 种子测试数据 (Flyway V1.1)
-- 密码明文: admin123 / 123456 (BCrypt加密)
-- ============================================

-- 测试用户
INSERT IGNORE INTO `user` (`id`, `username`, `password`, `nickname`, `name`, `phone`, `role`, `community`, `points`, `completed_tasks`, `is_deleted`)
VALUES
(1, 'admin',      '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员',   '管理员', '13800000001', 'ADMIN',     '星火社区', 9999, 0,  0),
(2, 'volunteer1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '热心志愿者小王', '王小明', '13800000002', 'VOLUNTEER', '星火社区', 520,  12, 0),
(3, 'elderly1',   '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '张奶奶',         '张秀英', '13800000003', 'ELDERLY',   '星火社区', 120,  0,  0);

-- 示例任务
INSERT IGNORE INTO `task` (`id`, `user_id`, `device_id`, `type`, `title`, `content`, `status`, `receiver_id`, `location`, `latitude`, `longitude`, `task_no`, `create_time`)
VALUES
(1, 3, 'DEV001', 'sos',          '紧急求助-老人摔倒', '老人在家摔倒，需要紧急医疗救助，请附近志愿者速来帮忙！', 0, NULL, '北京市朝阳区阳光小区3-1-101', 39.9042, 116.4074, 1, NOW()),
(2, 3, 'DEV001', 'life_service', '帮忙买菜',         '需要帮忙购买日常蔬菜水果：西红柿、鸡蛋、牛奶、青菜等', 0, NULL, '北京市朝阳区阳光小区2-1-302', 39.9052, 116.4084, 2, NOW()),
(3, 3, 'DEV001', 'consultation', '高血压健康咨询',   '最近血压有点不稳定，想咨询注意事项', 1, 2, '北京市朝阳区阳光小区3-2-401', 39.9062, 116.4094, 3, NOW());

INSERT IGNORE INTO `task` (`id`, `user_id`, `device_id`, `type`, `title`, `content`, `status`, `receiver_id`, `location`, `latitude`, `longitude`, `task_no`, `create_time`, `accept_time`, `finish_time`, `rating`)
VALUES
(4, 3, 'DEV001', 'life_service', '帮忙取快递', '快递单号SF1234567890，在小区南门快递柜', 2, 2, '北京市朝阳区阳光小区4-3-201', 39.9072, 116.4104, 4, NOW(), NOW(), NOW(), 5);

INSERT IGNORE INTO `task` (`id`, `user_id`, `device_id`, `type`, `title`, `content`, `status`, `location`, `latitude`, `longitude`, `task_no`, `create_time`)
VALUES
(5, 3, 'DEV001', 'sos', '紧急求助-断电维修', '家里突然断电，已联系物业处理', 3, '北京市朝阳区阳光小区5-1-601', 39.9082, 116.4114, 5, NOW());

-- 更新用户3的地址（seed数据中已有）
UPDATE `user` SET `address` = '北京市朝阳区阳光小区3-1-101' WHERE `id` = 3 AND `address` IS NULL;
