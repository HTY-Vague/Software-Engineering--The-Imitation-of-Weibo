-- =============================================================
-- Spark 离线分析统计表
-- 由 Spark 程序每日跑批覆盖写入，Spring Boot 只读
-- =============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 统计概览快照表（每次跑批覆盖一行，保留 id=1）
DROP TABLE IF EXISTS `stat_overview`;
CREATE TABLE `stat_overview` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键（固定为1）',
  `user_count` int NOT NULL DEFAULT '0' COMMENT '用户总数',
  `active_user_count` int NOT NULL DEFAULT '0' COMMENT '启用用户数',
  `enabled_board_count` int NOT NULL DEFAULT '0' COMMENT '启用板块数',
  `post_count` int NOT NULL DEFAULT '0' COMMENT '帖子总数（正常状态）',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论总数（正常状态）',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞总数',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '统计更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='统计概览快照表（Spark 跑批写入）';

-- 初始化一行占位数据，保证 Spring Boot 启动即可读
INSERT INTO `stat_overview` (`id`, `user_count`, `active_user_count`, `enabled_board_count`, `post_count`, `comment_count`, `like_count`)
VALUES (1, 0, 0, 0, 0, 0, 0);

-- 2. 热门帖子榜（跑批时整表清空重写）
DROP TABLE IF EXISTS `stat_hot_post`;
CREATE TABLE `stat_hot_post` (
  `rank_num` int NOT NULL COMMENT '排名（1开始）',
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `board_id` bigint NOT NULL COMMENT '板块ID',
  `author_id` bigint NOT NULL COMMENT '作者ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览量',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
  `hot_score` double NOT NULL DEFAULT '0' COMMENT '热度分（点赞*2 + 评论*3 + 浏览*0.1）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '统计更新时间',
  PRIMARY KEY (`rank_num`),
  KEY `idx_stat_hot_post_score` (`hot_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门帖子榜（Spark 跑批写入）';

-- 3. 板块活跃度表（跑批时整表清空重写）
DROP TABLE IF EXISTS `stat_board_active`;
CREATE TABLE `stat_board_active` (
  `board_id` bigint NOT NULL COMMENT '板块ID',
  `board_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '板块名称',
  `post_count` int NOT NULL DEFAULT '0' COMMENT '该板块帖子数',
  `view_sum` bigint NOT NULL DEFAULT '0' COMMENT '该板块浏览量总和',
  `like_sum` int NOT NULL DEFAULT '0' COMMENT '该板块点赞数总和',
  `comment_sum` int NOT NULL DEFAULT '0' COMMENT '该板块评论数总和',
  `active_score` double NOT NULL DEFAULT '0' COMMENT '活跃度（帖子*5 + 点赞*2 + 评论*3 + 浏览*0.1）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '统计更新时间',
  PRIMARY KEY (`board_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='板块活跃度统计（Spark 跑批写入）';

-- 4. 用户活跃度排行（跑批时整表清空重写，Top 50）
DROP TABLE IF EXISTS `stat_active_user`;
CREATE TABLE `stat_active_user` (
  `rank_num` int NOT NULL COMMENT '排名（1开始）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '昵称',
  `post_count` int NOT NULL DEFAULT '0' COMMENT '发帖数',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '获赞数（帖子和评论点赞合计）',
  `active_score` double NOT NULL DEFAULT '0' COMMENT '活跃度（发帖*5 + 评论*2 + 获赞*1）',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '统计更新时间',
  PRIMARY KEY (`rank_num`),
  KEY `idx_stat_active_user_score` (`active_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户活跃度排行（Spark 跑批写入）';

-- 5. 内容质量分析（跑批时整表清空重写）
--   quality_type: HIGH_LIKE_LOW_COMMENT=高赞低评论（优质干货）
--                 HIGH_COMMENT_LOW_LIKE=高评论低赞（争议帖）
--                 HOT=综合热门
DROP TABLE IF EXISTS `stat_content_quality`;
CREATE TABLE `stat_content_quality` (
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '帖子标题',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览量',
  `quality_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '质量分类',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '统计更新时间',
  PRIMARY KEY (`post_id`),
  KEY `idx_stat_content_quality_type` (`quality_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='内容质量分析（Spark 跑批写入）';

-- 6. 每日趋势（追加写，按天一行）
DROP TABLE IF EXISTS `stat_daily_trend`;
CREATE TABLE `stat_daily_trend` (
  `stat_date` date NOT NULL COMMENT '统计日期',
  `new_user_count` int NOT NULL DEFAULT '0' COMMENT '当日新增用户数',
  `new_post_count` int NOT NULL DEFAULT '0' COMMENT '当日新增帖子数',
  `new_comment_count` int NOT NULL DEFAULT '0' COMMENT '当日新增评论数',
  `new_like_count` int NOT NULL DEFAULT '0' COMMENT '当日新增点赞数',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '写入时间',
  PRIMARY KEY (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日趋势统计（Spark 跑批追加写入）';

SET FOREIGN_KEY_CHECKS = 1;
