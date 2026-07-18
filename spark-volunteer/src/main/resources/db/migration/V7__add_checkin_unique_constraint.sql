-- ============================================
-- V7: 签到表添加唯一约束，防止并发重复签到
-- ============================================

-- 先删除重复记录（保留最早的）
DELETE c1 FROM checkin c1
INNER JOIN checkin c2
WHERE c1.id > c2.id AND c1.user_id = c2.user_id AND c1.checkin_date = c2.checkin_date;

-- 添加唯一约束
ALTER TABLE checkin ADD UNIQUE INDEX idx_checkin_user_date_unique (user_id, checkin_date);

-- 反馈表添加唯一约束，防止刷评分
ALTER TABLE feedback ADD UNIQUE INDEX idx_feedback_task_user_unique (task_id, user_id);

-- 成就表添加唯一约束，防止重复解锁
ALTER TABLE achievement ADD UNIQUE INDEX idx_achievement_user_type_unique (user_id, achievement_type);
