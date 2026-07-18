-- ============================================
-- V8: 修复列类型，对齐 JPA Entity 注解
-- Hibernate validate 要求: @Lob String → LONGTEXT, String → TEXT
-- ============================================

-- task.content: 当前 TEXT → 改为 LONGTEXT (Entity 有 @Lob 注解)
ALTER TABLE task MODIFY COLUMN content LONGTEXT COMMENT '任务描述';

-- feedback.comment: 当前 TEXT → 改为 LONGTEXT (Entity 有 @Lob 注解)
ALTER TABLE feedback MODIFY COLUMN comment LONGTEXT COMMENT '评价内容';

-- feedback.content: 同上 (如果存在旧列也统一)
ALTER TABLE feedback MODIFY COLUMN content LONGTEXT COMMENT '评价内容';

-- message.content: 当前 TEXT → LONGTEXT (Entity 有 columnDefinition="TEXT"，但 @Lob String 期望 CLOB)
-- 如果 message entity 用的是 columnDefinition="TEXT" 则保持 TEXT，不做修改
-- 如果报 validate 错误再取消下面注释:
-- ALTER TABLE message MODIFY COLUMN content LONGTEXT COMMENT '消息内容';
