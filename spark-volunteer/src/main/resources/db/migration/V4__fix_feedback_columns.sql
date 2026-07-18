-- ============================================
-- V4: feedback 表对齐 Entity 字段名
-- Entity 使用 score/comment，V1 schema 使用 rating/content
-- 新增 Entity 所需的列，迁移旧数据，保留旧列兼容
-- ============================================

ALTER TABLE `feedback` ADD COLUMN `score` INT DEFAULT NULL COMMENT '评分(1-5)';
ALTER TABLE `feedback` ADD COLUMN `comment` TEXT COMMENT '评价内容';

-- 迁移已有数据：将旧列值复制到新列
UPDATE `feedback` SET `score` = `rating` WHERE `score` IS NULL AND `rating` IS NOT NULL;
UPDATE `feedback` SET `comment` = `content` WHERE `comment` IS NULL AND `content` IS NOT NULL;
