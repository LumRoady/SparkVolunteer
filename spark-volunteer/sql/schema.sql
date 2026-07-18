-- ============================================
-- 星火众擎 — 数据库建表脚本
-- 数据库: volunteer_db
-- 字符集: utf8mb4
-- ============================================

CREATE DATABASE IF NOT EXISTS volunteer_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE volunteer_db;

-- ==================== 1. 用户表 ====================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `username`        VARCHAR(50)   DEFAULT NULL             COMMENT '用户名（手机号注册时=phone）',
  `password`        VARCHAR(255)  DEFAULT NULL             COMMENT '密码（BCrypt加密）',
  `nickname`        VARCHAR(50)   DEFAULT NULL             COMMENT '昵称',
  `name`            VARCHAR(50)   DEFAULT NULL             COMMENT '真实姓名',
  `phone`           VARCHAR(20)   DEFAULT NULL             COMMENT '手机号',
  `avatar`          VARCHAR(255)  DEFAULT NULL             COMMENT '头像URL',
  `role`            VARCHAR(20)   NOT NULL DEFAULT 'ELDERLY' COMMENT '角色: ADMIN/VOLUNTEER/ELDERLY',
  `community`       VARCHAR(100)  DEFAULT NULL             COMMENT '所属社区',
  `address`         VARCHAR(255)  DEFAULT NULL             COMMENT '地址',
  `province`        VARCHAR(50)   DEFAULT NULL             COMMENT '省份',
  `city`            VARCHAR(50)   DEFAULT NULL             COMMENT '城市',
  `openid`          VARCHAR(100)  DEFAULT NULL             COMMENT '微信openid',
  `wechat_openid`   VARCHAR(100)  DEFAULT NULL             COMMENT '亲属微信openid(收通知用)',
  `parent_id`       BIGINT        DEFAULT NULL             COMMENT '关联的老人ID(子女记录指向老人)',
  `relation`        VARCHAR(20)   DEFAULT NULL             COMMENT '与老人关系: 子女/配偶/其他',
  `points`          INT           DEFAULT 0                COMMENT '积分',
  `completed_tasks` INT           DEFAULT 0                COMMENT '完成任务数',
  `last_checkin`    DATETIME      DEFAULT NULL             COMMENT '最后签到时间',
  `checkin_streak`  INT           DEFAULT 0                COMMENT '连续签到天数',
  `is_deleted`      INT           DEFAULT 0                COMMENT '是否删除: 0-否 1-是',
  `create_time`     DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_time`     DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_username` (`username`),
  UNIQUE KEY `idx_user_openid` (`openid`),
  KEY `idx_user_role` (`role`),
  KEY `idx_user_phone` (`phone`),
  KEY `idx_user_community` (`community`),
  KEY `idx_user_is_deleted` (`is_deleted`),
  KEY `idx_user_last_checkin` (`last_checkin`),
  KEY `idx_user_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- ==================== 2. 任务表 ====================
DROP TABLE IF EXISTS `task`;
CREATE TABLE `task` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `user_id`          BIGINT        NOT NULL                COMMENT '发布者ID(老人)',
  `requester_id`     BIGINT        DEFAULT NULL             COMMENT '请求者ID',
  `device_id`        VARCHAR(50)   DEFAULT NULL             COMMENT '设备ID(ESP32)',
  `type`             VARCHAR(30)   NOT NULL                COMMENT '任务类型: sos/life_service/consultation',
  `title`            VARCHAR(100)  NOT NULL                COMMENT '任务标题',
  `content`          TEXT                                   COMMENT '任务描述',
  `status`           INT           DEFAULT 0               COMMENT '状态: 0待接单 1已接单 2已完成 3已取消',
  `receiver_id`      BIGINT        DEFAULT NULL             COMMENT '接单志愿者ID',
  `location`         VARCHAR(255)  DEFAULT NULL             COMMENT '任务地点',
  `latitude`         DOUBLE        DEFAULT NULL             COMMENT '纬度',
  `longitude`        DOUBLE        DEFAULT NULL             COMMENT '经度',
  `urgency`          INT           DEFAULT 0               COMMENT '紧急程度: 0一般 1紧急 2非常紧急',
  `priority`         INT           DEFAULT 0               COMMENT '优先级: 0普通 1重要 2高',
  `estimated_time`   INT           DEFAULT NULL             COMMENT '预计完成时间(分钟)',
  `rating`           INT           DEFAULT NULL             COMMENT '评分(1-5)',
  `review`           TEXT                                   COMMENT '评价内容',
  `need_home_service` TINYINT(1)   DEFAULT 0               COMMENT '是否需要上门',
  `task_no`          INT           DEFAULT NULL             COMMENT '对外展示序号',
  `create_time`      DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `accept_time`      DATETIME      DEFAULT NULL             COMMENT '接单时间',
  `finish_time`      DATETIME      DEFAULT NULL             COMMENT '完成时间',
  `update_time`      DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_user_id` (`user_id`),
  KEY `idx_task_receiver_id` (`receiver_id`),
  KEY `idx_task_status` (`status`),
  KEY `idx_task_type` (`type`),
  KEY `idx_task_status_type` (`status`, `type`),
  KEY `idx_task_create_time` (`create_time`),
  KEY `idx_task_task_no` (`task_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';


-- ==================== 3. 设备表 ====================
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `device_id`        VARCHAR(50)   NOT NULL                COMMENT '设备唯一标识',
  `name`             VARCHAR(100)  DEFAULT NULL             COMMENT '设备名称',
  `device_type`      VARCHAR(30)   DEFAULT NULL             COMMENT '设备类型: ESP32_C3等',
  `user_id`          BIGINT        DEFAULT NULL             COMMENT '绑定用户ID',
  `status`           VARCHAR(20)   DEFAULT 'INACTIVE'       COMMENT '状态: ACTIVE/INACTIVE',
  `last_online_time` DATETIME      DEFAULT NULL             COMMENT '最后在线时间',
  `create_time`      DATETIME      DEFAULT NULL             COMMENT '创建时间',
  `update_time`      DATETIME      DEFAULT NULL             COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_device_device_id` (`device_id`),
  KEY `idx_device_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表(ESP32)';


-- ==================== 4. 成就表 ====================
DROP TABLE IF EXISTS `achievement`;
CREATE TABLE `achievement` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `user_id`          BIGINT        NOT NULL                COMMENT '用户ID',
  `achievement_type` VARCHAR(50)   NOT NULL                COMMENT '成就类型编码',
  `title`            VARCHAR(100)  NOT NULL                COMMENT '成就名称',
  `description`      VARCHAR(255)  DEFAULT NULL             COMMENT '成就描述',
  `icon`             VARCHAR(20)   DEFAULT NULL             COMMENT '图标emoji',
  `unlocked_at`      DATETIME      DEFAULT NULL             COMMENT '解锁时间',
  PRIMARY KEY (`id`),
  KEY `idx_achievement_user_id` (`user_id`),
  KEY `idx_achievement_user_type` (`user_id`, `achievement_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就表';


-- ==================== 5. 签到表 ====================
DROP TABLE IF EXISTS `checkin`;
CREATE TABLE `checkin` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `user_id`          BIGINT        NOT NULL                COMMENT '用户ID',
  `checkin_date`     DATE          NOT NULL                COMMENT '签到日期',
  `checkin_time`     DATETIME      DEFAULT NULL             COMMENT '签到时间',
  `status`           VARCHAR(20)   DEFAULT 'SUCCESS'        COMMENT '签到状态',
  PRIMARY KEY (`id`),
  KEY `idx_checkin_user_id` (`user_id`),
  KEY `idx_checkin_user_date` (`user_id`, `checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到表';


-- ==================== 6. 通知表 ====================
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `user_id`          BIGINT        NOT NULL                COMMENT '接收用户ID',
  `title`            VARCHAR(200)  NOT NULL                COMMENT '通知标题',
  `content`          TEXT                                   COMMENT '通知内容',
  `type`             VARCHAR(30)   DEFAULT NULL             COMMENT '通知类型',
  `is_read`          TINYINT(1)    DEFAULT 0               COMMENT '是否已读',
  `create_time`      DATETIME      DEFAULT NULL             COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_notification_user_id` (`user_id`),
  KEY `idx_notification_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';


-- ==================== 7. 消息表 ====================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `sender_id`        BIGINT        NOT NULL                COMMENT '发送者ID',
  `receiver_id`      BIGINT        NOT NULL                COMMENT '接收者ID',
  `content`          TEXT          NOT NULL                COMMENT '消息内容',
  `conversation_id`  BIGINT        DEFAULT NULL             COMMENT '会话ID',
  `status`           VARCHAR(20)   DEFAULT 'UNREAD'         COMMENT '状态: UNREAD/READ',
  `create_time`      DATETIME      DEFAULT NULL             COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_message_sender` (`sender_id`),
  KEY `idx_message_receiver` (`receiver_id`),
  KEY `idx_message_conversation` (`conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';


-- ==================== 8. 评价反馈表 ====================
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `task_id`          BIGINT        DEFAULT NULL             COMMENT '关联任务ID',
  `user_id`          BIGINT        NOT NULL                COMMENT '评价人ID',
  `target_user_id`   BIGINT        DEFAULT NULL             COMMENT '被评价人ID',
  `rating`           INT           DEFAULT NULL             COMMENT '评分(1-5)',
  `content`          TEXT                                   COMMENT '评价内容',
  `create_time`      DATETIME      DEFAULT NULL             COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_feedback_task_id` (`task_id`),
  KEY `idx_feedback_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价反馈表';


-- ==================== 9. 任务挑战表 ====================
DROP TABLE IF EXISTS `task_challenge`;
CREATE TABLE `task_challenge` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `title`            VARCHAR(200)  NOT NULL                COMMENT '挑战标题',
  `description`      TEXT                                   COMMENT '挑战描述',
  `type`             VARCHAR(30)   DEFAULT NULL             COMMENT '挑战类型',
  `target_count`     INT           DEFAULT 0               COMMENT '目标次数',
  `completed_count`  INT           DEFAULT 0               COMMENT '已完成次数',
  `reward_points`    INT           DEFAULT 0               COMMENT '奖励积分',
  `is_completed`     TINYINT(1)    DEFAULT 0               COMMENT '是否完成',
  `user_id`          BIGINT        DEFAULT NULL             COMMENT '关联用户ID',
  `start_date`       DATE          DEFAULT NULL             COMMENT '开始日期',
  `end_date`         DATE          DEFAULT NULL             COMMENT '截止日期',
  `create_time`      DATETIME      DEFAULT NULL             COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_challenge_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务挑战表';


-- ==================== 10. 任务参与者表 ====================
DROP TABLE IF EXISTS `task_participant`;
CREATE TABLE `task_participant` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
  `task_id`          BIGINT        NOT NULL                COMMENT '任务ID',
  `user_id`          BIGINT        NOT NULL                COMMENT '参与用户ID',
  `role`             VARCHAR(20)   NOT NULL                COMMENT '角色: REQUESTER/RECEIVER',
  `status`           INT           DEFAULT 0               COMMENT '参与状态',
  `confirm_time`     DATETIME      DEFAULT NULL             COMMENT '确认时间',
  `complete_time`    DATETIME      DEFAULT NULL             COMMENT '完成时间',
  `create_time`      DATETIME      DEFAULT NULL             COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_participant_task_id` (`task_id`),
  KEY `idx_participant_user_id` (`user_id`),
  KEY `idx_participant_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务参与者表';
