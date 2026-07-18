-- ============================================
-- V3: message 表添加缺失列
-- Entity: Message.java 新增 messageType, taskId, isDeleted, updateTime
-- ============================================

ALTER TABLE `message` ADD COLUMN `message_type` VARCHAR(30) DEFAULT NULL COMMENT '消息类型';
ALTER TABLE `message` ADD COLUMN `task_id` BIGINT DEFAULT NULL COMMENT '关联任务ID';
ALTER TABLE `message` ADD COLUMN `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '是否删除';
ALTER TABLE `message` ADD COLUMN `update_time` DATETIME DEFAULT NULL COMMENT '更新时间';
