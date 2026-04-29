-- Demo data for software_engineering_forum.
-- Run this after SoftwareEngineering.sql.
--
-- Password fields below are placeholders. Generate BCrypt hashes through the
-- /auth/register API or PasswordEncoderUtil before using these accounts in a
-- real demo environment.

USE software_engineering_forum;

SET @admin_password_hash = 'REPLACE_WITH_BCRYPT_HASH_FOR_Admin_123';
SET @user_password_hash = 'REPLACE_WITH_BCRYPT_HASH_FOR_User_123';

INSERT INTO sys_user (username, password, nickname, avatar, signature, role, status)
VALUES
    ('admin', @admin_password_hash, '管理员', NULL, '课程设计演示管理员', 'ADMIN', 1),
    ('demo_user', @user_password_hash, '演示用户', NULL, '用于课程设计演示的普通用户', 'USER', 1)
ON DUPLICATE KEY UPDATE
    nickname = VALUES(nickname),
    avatar = VALUES(avatar),
    signature = VALUES(signature),
    role = VALUES(role),
    status = VALUES(status);

INSERT INTO forum_board (board_name, description, sort_num, status)
VALUES
    ('公告板', '课程设计公告与系统说明', 1, 1),
    ('技术交流', '后端、前端、数据库与部署问题交流', 2, 1),
    ('校园生活', '校园动态与日常分享', 3, 1)
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    sort_num = VALUES(sort_num),
    status = VALUES(status);

INSERT INTO forum_post (board_id, author_id, title, content, view_count, like_count, comment_count, is_top, status)
SELECT b.board_id, u.user_id, '欢迎使用仿微博论坛系统', '这里是课程设计演示环境的置顶欢迎帖。', 15, 0, 0, 1, 1
FROM forum_board b
JOIN sys_user u ON u.username = 'admin'
WHERE b.board_name = '公告板'
  AND NOT EXISTS (
      SELECT 1 FROM forum_post p WHERE p.title = '欢迎使用仿微博论坛系统'
  );

INSERT INTO forum_post (board_id, author_id, title, content, view_count, like_count, comment_count, is_top, status)
SELECT b.board_id, u.user_id, '后端接口联调记录', '本帖用于演示帖子发布、评论和点赞主链路。', 8, 0, 0, 0, 1
FROM forum_board b
JOIN sys_user u ON u.username = 'demo_user'
WHERE b.board_name = '技术交流'
  AND NOT EXISTS (
      SELECT 1 FROM forum_post p WHERE p.title = '后端接口联调记录'
  );

INSERT INTO forum_comment (post_id, user_id, parent_comment_id, reply_user_id, content, like_count, status)
SELECT p.post_id, u.user_id, NULL, NULL, '演示评论：接口返回格式正常。', 0, 1
FROM forum_post p
JOIN sys_user u ON u.username = 'demo_user'
WHERE p.title = '欢迎使用仿微博论坛系统'
  AND NOT EXISTS (
      SELECT 1 FROM forum_comment c
      WHERE c.post_id = p.post_id
        AND c.user_id = u.user_id
        AND c.content = '演示评论：接口返回格式正常。'
  );
