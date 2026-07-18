-- ============================================
-- V6: task_participant 表添加缺失列
-- Entity: TaskParticipant.java 新增 participateTime, updateTime, remarks
-- ============================================

ALTER TABLE `task_participant` ADD COLUMN `participate_time` DATETIME DEFAULT NULL COMMENT '参与时间';
ALTER TABLE `task_participant` ADD COLUMN `update_time` DATETIME DEFAULT NULL COMMENT '更新时间';
ALTER TABLE `task_participant` ADD COLUMN `remarks` VARCHAR(500) DEFAULT NULL COMMENT '备注信息';
