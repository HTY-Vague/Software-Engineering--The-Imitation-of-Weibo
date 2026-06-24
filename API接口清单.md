# API 接口清单

> 基于当前后端代码整理，供前端联调、接口测试和验收使用。除 `/auth/**` 和 API 文档地址外，其余接口均需要在请求头携带 `Authorization: Bearer <token>`。

## 一、通用响应格式

所有业务接口统一返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | number | 业务状态码，成功为 `200` |
| `message` | string | 响应消息 |
| `data` | any | 响应数据，无数据时为 `null` |

## 二、认证接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 用户注册 | POST | `/auth/register` | 公开 | 注册普通用户 |
| 用户登录 | POST | `/auth/login` | 公开 | 登录成功返回 `token` 和用户信息 |
| 用户注销 | POST | `/auth/logout` | 公开 | 无状态 JWT 场景下返回成功 |

### 登录请求示例

```json
{
  "username": "test",
  "password": "123456"
}
```

### 登录响应 data 示例

```json
{
  "token": "jwt-token",
  "user": {
    "userId": 1,
    "username": "test",
    "nickname": "测试用户",
    "role": "USER"
  }
}
```

## 三、用户接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 获取个人资料 | POST | `/user/profile` | USER/ADMIN | 使用 token 中的当前用户 |
| 更新个人资料 | POST | `/user/update/profile` | USER/ADMIN | 只能更新当前登录用户资料 |
| 修改密码 | POST | `/user/update/password` | USER/ADMIN | 请求参数：`oldPassword`、`newPassword` |
| 我的帖子 | POST | `/user/posts` | USER/ADMIN | 查询当前登录用户帖子 |
| 删除我的帖子 | POST | `/user/delete/post` | USER/ADMIN | 请求参数：`postId`，只能删除自己的帖子 |
| 我的评论 | POST | `/user/comments` | USER/ADMIN | 查询当前登录用户评论 |

## 四、板块接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 板块列表 | POST | `/forum/boards` | USER/ADMIN | 查询全部板块 |
| 板块帖子 | POST | `/forum/board/posts` | USER/ADMIN | 请求体传 `boardId` |

### 板块帖子请求示例

```json
{
  "boardId": 1
}
```

## 五、帖子接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 发布帖子 | POST | `/posts/create` | USER/ADMIN | 作者 ID 使用当前 token 用户 |
| 帖子详情 | POST | `/posts/detail` | USER/ADMIN | 请求参数：`postId`；浏览量自动递增 |
| 帖子列表 | POST | `/posts/list` | USER/ADMIN | 支持 `boardId`、`keyword`、`page`、`size` |
| 更新帖子 | POST | `/posts/update` | USER/ADMIN | 普通用户只能更新自己的帖子 |
| 删除帖子 | POST | `/posts/delete` | USER/ADMIN | 普通用户只能删除自己的帖子 |

### 发布帖子请求示例

```json
{
  "boardId": 1,
  "title": "帖子标题",
  "content": "帖子内容"
}
```

## 六、评论接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 发表评论 | POST | `/comments/create` | USER/ADMIN | 评论用户 ID 使用当前 token 用户 |
| 评论列表 | POST | `/comments/list` | USER/ADMIN | 请求参数：`postId`；返回两级评论树 |
| 删除评论 | POST | `/comments/delete` | USER/ADMIN | 普通用户只能删除自己的评论 |

### 发表评论请求示例

```json
{
  "postId": 1,
  "parentCommentId": null,
  "replyUserId": null,
  "content": "评论内容"
}
```

## 七、点赞接口

| 接口 | 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|------|
| 点赞 | POST | `/likes/like` | USER/ADMIN | 参数：`targetType`、`targetId` |
| 取消点赞 | POST | `/likes/unlike` | USER/ADMIN | 参数：`targetType`、`targetId` |
| 点赞状态 | POST | `/likes/status` | USER/ADMIN | 查询当前用户是否已点赞 |
| 点赞数 | POST | `/likes/count` | USER/ADMIN | 查询目标对象点赞数 |

`targetType` 约定：

| 值 | 说明 |
|----|------|
| `1` | 帖子 |
| `2` | 评论 |

## 八、管理员接口

所有 `/admin/**` 接口仅允许 `ADMIN` 角色访问。

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 用户列表 | GET | `/admin/users` | 查询全部用户 |
| 更新用户状态 | PUT | `/admin/users/{userId}/status` | 启用/禁用用户 |
| 板块列表 | GET | `/admin/forums` | 查询全部板块 |
| 新增板块 | POST | `/admin/forums` | 新增板块 |
| 更新板块 | PUT | `/admin/forums/{forumId}` | 更新板块 |
| 更新板块状态 | PUT | `/admin/forums/{forumId}/status` | 启用/禁用板块 |
| 板块排序 | PUT | `/admin/forums/sort` | 请求体传板块 ID 数组 |
| 删除板块 | DELETE | `/admin/forums/{forumId}` | 删除板块 |
| 帖子置顶 | PUT | `/admin/posts/{postId}/top` | 请求体传 `0` 或 `1` |
| 删除帖子 | DELETE | `/admin/posts/{postId}` | 管理员删除违规帖子 |
| 删除评论 | DELETE | `/admin/comments/{commentId}` | 管理员删除违规评论 |
| 统计概览 | GET | `/admin/statistics/overview` | 用户、板块、帖子、评论、点赞数量。数据来源：Spark 跑批写入的 `stat_overview` 表 |
| 热门帖子 | GET | `/admin/statistics/hot-posts` | 支持参数：`limit`，默认 10，最大 50。数据来源：Spark 跑批写入的 `stat_hot_post` 表，按热度分排序 |
| 板块活跃度 | GET | `/admin/statistics/board-active` | 板块活跃度排行。数据来源：Spark 跑批写入的 `stat_board_active` 表 |
| 用户活跃度 | GET | `/admin/statistics/active-users` | 支持参数：`limit`，默认 10，最大 50。数据来源：Spark 跑批写入的 `stat_active_user` 表 |
| 内容质量分析 | GET | `/admin/statistics/content-quality` | 支持参数：`qualityType`，可选值 `HIGH_LIKE_LOW_COMMENT`（高赞低评论）、`HIGH_COMMENT_LOW_LIKE`（高评论低赞）、`HOT`（综合热门），不传返回全部。数据来源：Spark 跑批写入的 `stat_content_quality` 表 |
| 每日趋势 | GET | `/admin/statistics/daily-trend` | 支持参数：`days`，默认 7，最大 90。数据来源：Spark 跑批写入的 `stat_daily_trend` 表 |
| 触发 Spark 跑批 | POST | `/admin/statistics/trigger-spark` | 手动异步触发服务器上的 spark-submit 刷新统计表。需配置 `SPARK_ENABLED=true` 及数据库凭据环境变量。返回 `state`（DISABLED/RUNNING）和 `taskId` |
| Spark 跑批状态 | GET | `/admin/statistics/spark-status` | 查询最近一次跑批状态：`state`（READY/RUNNING/SUCCESS/FAILED）、`taskId`、`startTime`、`endTime`、`durationMillis`、`message` |

> 上述统计接口的数据由服务器上的 Spark 离线分析程序（`/opt/forum-stats.jar`）每日凌晨 03:00 通过 cron 跑批写入 `stat_*` 表，Spring Boot 端只读展示。统计数据容忍日级延迟，不需要实时。课程演示时可通过 `POST /admin/statistics/trigger-spark` 手动触发跑批立即刷新统计数据，再用 `GET /admin/statistics/spark-status` 查询执行状态。详见 `后端文档/后端部署说明.md` 中 Spark 分析部署章节。

### 热门帖子响应示例

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "rankNum": 1,
      "postId": 4,
      "boardId": 2,
      "authorId": 1,
      "title": "校园周边美食推荐",
      "viewCount": 890,
      "likeCount": 35,
      "commentCount": 20,
      "hotScore": 219.0,
      "updateTime": "2026-06-23T20:50:17"
    }
  ]
}
```

### 内容质量分析 qualityType 取值

| 值 | 说明 | 判定规则 |
|----|------|---------|
| `HIGH_LIKE_LOW_COMMENT` | 高赞低评论（优质干货） | 点赞数 ≥ 10 且 评论数 ≤ 5 |
| `HIGH_COMMENT_LOW_LIKE` | 高评论低赞（争议/讨论帖） | 评论数 ≥ 8 且 点赞数 ≤ 3 |
| `HOT` | 综合热门 | 热度分 ≥ 40（热度 = 点赞×2 + 评论×3 + 浏览×0.1） |

## 九、文档接口

| 工具 | 地址 |
|------|------|
| Knife4j | `http://localhost:8080/doc.html` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
