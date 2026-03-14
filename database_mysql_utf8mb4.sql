-- 论坛系统数据库建库脚本（MySQL 8.x）
-- 字符集：utf8mb4

CREATE DATABASE IF NOT EXISTS software_engineering_forum
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE software_engineering_forum;


CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '登录账号',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    signature VARCHAR(255) DEFAULT NULL COMMENT '个性签名',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


CREATE TABLE IF NOT EXISTS forum_board (
    board_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '板块ID',
    board_name VARCHAR(100) NOT NULL COMMENT '板块名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '板块描述',
    sort_num INT NOT NULL DEFAULT 0 COMMENT '排序值',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_forum_board_name (board_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块表';


CREATE TABLE IF NOT EXISTS forum_post (
    post_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
    board_id BIGINT NOT NULL COMMENT '所属板块ID',
    author_id BIGINT NOT NULL COMMENT '作者用户ID',
    title VARCHAR(200) NOT NULL COMMENT '帖子标题',
    content LONGTEXT NOT NULL COMMENT '帖子内容（富文本）',
    view_count INT NOT NULL DEFAULT 0 COMMENT '浏览量',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    comment_count INT NOT NULL DEFAULT 0 COMMENT '评论数',
    is_top TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶：1是，0否',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_forum_post_board_id (board_id),
    KEY idx_forum_post_author_id (author_id),
    KEY idx_forum_post_create_time (create_time),
    CONSTRAINT fk_forum_post_board
        FOREIGN KEY (board_id) REFERENCES forum_board(board_id),
    CONSTRAINT fk_forum_post_author
        FOREIGN KEY (author_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';


CREATE TABLE IF NOT EXISTS forum_comment (
    comment_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    post_id BIGINT NOT NULL COMMENT '所属帖子ID',
    user_id BIGINT NOT NULL COMMENT '评论用户ID',
    parent_comment_id BIGINT DEFAULT NULL COMMENT '父评论ID（一级评论为NULL）',
    reply_user_id BIGINT DEFAULT NULL COMMENT '被回复用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    like_count INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常，0删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_forum_comment_post_id (post_id),
    KEY idx_forum_comment_user_id (user_id),
    KEY idx_forum_comment_parent_comment_id (parent_comment_id),
    CONSTRAINT fk_forum_comment_post
        FOREIGN KEY (post_id) REFERENCES forum_post(post_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_forum_comment_user
        FOREIGN KEY (user_id) REFERENCES sys_user(user_id),
    CONSTRAINT fk_forum_comment_parent
        FOREIGN KEY (parent_comment_id) REFERENCES forum_comment(comment_id),
    CONSTRAINT fk_forum_comment_reply_user
        FOREIGN KEY (reply_user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';


-- 可选：点赞明细表（如果需要记录用户点赞历史）
CREATE TABLE IF NOT EXISTS forum_like (
    like_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞ID',
    post_id BIGINT NOT NULL COMMENT '帖子ID',
    user_id BIGINT NOT NULL COMMENT '点赞用户ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_forum_like_post_user (post_id, user_id),
    KEY idx_forum_like_user_id (user_id),
    CONSTRAINT fk_forum_like_post
        FOREIGN KEY (post_id) REFERENCES forum_post(post_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_forum_like_user
        FOREIGN KEY (user_id) REFERENCES sys_user(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';
