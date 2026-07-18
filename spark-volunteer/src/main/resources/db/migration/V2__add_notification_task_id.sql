-- ============================================
-- V2: notification 表添加 task_id 列
-- Entity: Notification.java 新增 taskId 字段
-- ============================================

ALTER TABLE `notification` ADD COLUMN `task_id` BIGINT DEFAULT NULL COMMENT '关联任务ID';
