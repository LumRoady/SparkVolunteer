-- ============================================
-- V5: task_challenge 表对齐 Entity 字段
-- Entity 使用 reward/completed/created_at/updated_at，V1 schema 使用 reward_points/is_completed/create_time
-- 新增 Entity 所需的列，迁移旧数据，保留旧列兼容
-- ============================================

-- Entity 使用 reward，V1 使用 reward_points
ALTER TABLE `task_challenge` ADD COLUMN `reward` INT DEFAULT 0 COMMENT '奖励积分';
UPDATE `task_challenge` SET `reward` = `reward_points` WHERE `reward` = 0 AND `reward_points` IS NOT NULL;

-- Entity 使用 completed (Boolean)，V1 使用 is_completed (TINYINT)
ALTER TABLE `task_challenge` ADD COLUMN `completed` TINYINT(1) DEFAULT 0 COMMENT '是否完成';
UPDATE `task_challenge` SET `completed` = `is_completed` WHERE `completed` = 0 AND `is_completed` IS NOT NULL;

-- Entity 新增字段
ALTER TABLE `task_challenge` ADD COLUMN `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系人信息';
ALTER TABLE `task_challenge` ADD COLUMN `address` VARCHAR(255) DEFAULT NULL COMMENT '挑战地点';

-- Entity 使用 created_at/updated_at，V1 使用 create_time
ALTER TABLE `task_challenge` ADD COLUMN `created_at` DATETIME DEFAULT NULL COMMENT '创建时间';
ALTER TABLE `task_challenge` ADD COLUMN `updated_at` DATETIME DEFAULT NULL COMMENT '更新时间';
UPDATE `task_challenge` SET `created_at` = `create_time` WHERE `created_at` IS NULL AND `create_time` IS NOT NULL;
