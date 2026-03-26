-- 点赞表结构升级脚本（方案一：target_type + target_id）
-- 说明：基于现有数据库结构，升级 forum_like 表以支持帖子和评论点赞
-- 执行前请备份数据库！

USE software_engineering_forum;

-- ============================================
-- 第一步：备份原有数据
-- ============================================
CREATE TABLE IF NOT EXISTS forum_like_backup AS
SELECT * FROM forum_like;

COMMIT;

-- ============================================
-- 第二步：删除旧表（包括外键约束）
-- ============================================
ALTER TABLE forum_like DROP FOREIGN KEY fk_forum_like_user;
ALTER TABLE forum_like DROP FOREIGN KEY fk_forum_like_post;
DROP TABLE forum_like;

-- ============================================
-- 第三步：创建新的 forum_like 表（方案一）
-- ============================================
CREATE TABLE IF NOT EXISTS forum_like (
    like_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
    target_type TINYINT NOT NULL COMMENT '点赞目标类型：1-帖子，2-评论',
    target_id BIGINT NOT NULL COMMENT '目标ID（帖子ID或评论ID）',
    user_id BIGINT NOT NULL COMMENT '点赞用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_forum_like_target_user (target_type, target_id, user_id) COMMENT '确保同一用户不重复点赞同一目标',
    KEY idx_forum_like_user_id (user_id),
    KEY idx_forum_like_target (target_type, target_id),
    CONSTRAINT fk_forum_like_user
        FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表（支持帖子和评论）';

-- ============================================
-- 第四步：迁移备份数据到新表
-- ============================================
-- 原 forum_like_backup 中的数据都是帖子点赞，迁移时 target_type 设为 1
INSERT INTO forum_like (target_type, target_id, user_id, create_time)
SELECT 1, post_id, user_id, create_time FROM forum_like_backup;

COMMIT;

-- ============================================
-- 第五步：验证迁移结果
-- ============================================
-- 查看迁移后的数据
SELECT COUNT(*) AS total_likes FROM forum_like;
SELECT target_type, COUNT(*) AS count FROM forum_like GROUP BY target_type;

-- ============================================
-- 第六步：清理备份表（可选，确认无误后执行）
-- ============================================
-- DROP TABLE forum_like_backup;
