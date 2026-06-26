# 论坛系统完整 API 接口文档

> 本文档基于源码逐行分析，覆盖全部 43 个 API 端点。按控制器分组，每个接口包含：路径与方法、请求参数、响应格式、功能说明与执行逻辑。
>
> 后端技术栈：Java 21 · Spring Boot 3.3.5 · Spring Security 6.x · JWT 0.11.5 · MyBatis Plus 3.5.7
>
> **统一响应格式**：所有接口均返回 `ResultUtil.Result<T>`，结构为：
> ```json
> { "code": 200, "message": "操作成功", "data": {} }
> ```
> - `code: 200` — 成功；`400`/`409`/`500` — 业务错误；`403` — 鉴权失败
> - `data` — 具体返回值，无数据时为 `null`

---

## 一、认证模块 — AuthController（前缀 `/auth`）

> **权限**：全部公开访问（`permitAll`），无需携带 JWT token。
> **安全要点**：注册时强制角色为 `USER`，密码使用 BCrypt（强度 10）加密存储；登录返回的 token 中包含 `userId` 和 `role` 信息，前端需在后续请求 Header 中携带 `Authorization: Bearer <token>`。

---

### 1. `POST /auth/login` — 用户登录

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/auth/login` |
| **权限** | 公开 |
| **请求参数** | `@RequestBody User` — JSON 对象 |
| **请求字段** | `username: String`（必填，用户名）<br>`password: String`（必填，明文密码） |
| **请求示例** | `{ "username": "admin", "password": "Admin_123" }` |

**响应格式：**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {
      "userId": 1,
      "username": "admin",
      "nickname": "管理员",
      "avatar": null,
      "signature": null,
      "role": "ADMIN",
      "status": "启用",
      "createTime": "2025-01-01T00:00:00",
      "updateTime": "2025-06-01T12:00:00"
    }
  }
}
```

**执行逻辑：**

1. 校验 `username` 和 `password` 均不为空
2. 通过 `userMapper.login(user)` 查询数据库匹配用户
3. 使用 `PasswordEncoderUtil.matches()` 进行 BCrypt 密码比对；同时兼容原始明文密码的兜底比较
4. 密码不匹配或用户不存在 → 返回 `"用户名或密码错误"`
5. 验证通过后调用 `JwtUtil.generateToken(userId, role)` 生成 JWT，过期时间由配置 `jwt.expiration` 控制（默认 86400000ms = 24 小时）
6. 返回 `{ token, user }` 对象，`user` 对象的 `password` 字段已置为 `null`

---

### 2. `POST /auth/register` — 用户注册

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/auth/register` |
| **权限** | 公开 |
| **请求参数** | `@RequestBody User` — JSON 对象 |
| **请求字段** | `username: String`（必填）<br>`password: String`（必填）<br>`nickname: String`（选填，默认等于 username） |
| **请求示例** | `{ "username": "newuser", "password": "Pass_123", "nickname": "新用户" }` |

**响应格式：**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 5,
    "username": "newuser",
    "nickname": "新用户",
    "role": "USER",
    "status": "启用",
    "createTime": "2025-06-26T16:00:00",
    "updateTime": "2025-06-26T16:00:00"
  }
}
```

**执行逻辑：**

1. 校验 `username` 和 `password` 均不为空
2. 通过 `userMapper.getUserByUsername(username)` 检查用户名唯一性，已存在 → 返回 `"用户名已存在"`
3. 若未提供 `nickname`，默认设为 `username`
4. **强制设置 `role = "USER"`**（防止注册管理员账户）
5. 使用 `PasswordEncoderUtil.encode(password)` BCrypt 加密密码
6. 调用 `userMapper.register(user)` 插入数据库
7. 返回注册成功的 User 对象（`password` 已置 `null`）

---

### 3. `POST /auth/logout` — 用户退出

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/auth/logout` |
| **权限** | 公开 |
| **请求参数** | 无 |
| **响应格式** | `{ "code": 200, "message": "退出成功", "data": null }` |

**执行逻辑：**

1. 调用 `userMapper.logout(userId)`（无状态 JWT 架构下为记录性操作）
2. 若 `userId` 为空直接返回（幂等）
3. 前端应自行清除 localStorage 中的 token 和用户信息

---

## 二、个人中心模块 — UserController（前缀 `/user`）

> **权限**：所有接口均需认证（`USER` 或 `ADMIN`），需在请求头携带有效 JWT token。
> **身份识别**：当前用户身份通过 `SecurityUtil.getCurrentUserId()` 从 Spring Security 上下文中提取（来源于 JWT token），**不信任请求体中的 userId**。

---

### 4. `POST /user/profile` — 获取当前用户信息

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/user/profile` |
| **权限** | 需认证 |
| **请求参数** | 无（用户 ID 从 token 解析） |

**响应格式：**

```json
{
  "code": 200,
  "data": {
    "userId": 2,
    "username": "demo_user",
    "nickname": "演示用户",
    "avatar": "/avatar/default.png",
    "signature": "个性签名",
    "role": "USER",
    "status": "启用",
    "createTime": "2025-03-01T10:00:00",
    "updateTime": "2025-06-20T08:30:00"
  }
}
```

**执行逻辑：**

1. 从 SecurityContext 提取当前登录用户 ID
2. 调用 `userMapper.getUserById(userId)` 查询
3. 返回前将 `password` 置 `null`

---

### 5. `POST /user/update/profile` — 修改个人资料

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/user/update/profile` |
| **权限** | 需认证 |
| **请求参数** | `@RequestBody User` — JSON 对象 |
| **请求字段** | `nickname: String`<br>`avatar: String`<br>`signature: String` |
| **请求示例** | `{ "nickname": "新昵称", "signature": "新签名" }` |

**响应格式**：`{ "code": 200, "message": "修改成功", "data": null }`

**执行逻辑：**

1. 校验 `userId` 不为空
2. Controller 从 token 注入当前用户 ID
3. 调用 `userMapper.updateProfile(user)` 更新

---

### 6. `POST /user/update/password` — 修改密码

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/user/update/password` |
| **权限** | 需认证 |
| **请求参数** | `@RequestParam String oldPassword`（旧密码）<br>`@RequestParam String newPassword`（新密码） |

**响应格式**：`{ "code": 200, "message": "密码修改成功", "data": null }`

**执行逻辑：**

1. 校验 `oldPassword` 和 `newPassword` 均不为空
2. 查询当前用户 → 使用 BCrypt 验证旧密码是否匹配 → 不匹配返回 `"旧密码错误"`
3. 使用 `PasswordEncoderUtil.encode(newPassword)` 加密新密码
4. 调用 `userMapper.updatePassword(userId, encodedPassword)` 更新
5. **前端在修改密码成功后应提示用户重新登录**

---

### 7. `POST /user/posts` — 获取当前用户的帖子列表

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/user/posts` |
| **权限** | 需认证 |
| **请求参数** | 无（用户 ID 从 token 解析） |

**响应格式：**

```json
{
  "code": 200,
  "data": [
    {
      "postId": 10,
      "boardId": 1,
      "authorId": 2,
      "title": "我的帖子标题",
      "content": "<p>帖子内容 HTML</p>",
      "viewCount": 150,
      "likeCount": 12,
      "commentCount": 5,
      "isTop": 0,
      "status": "正常",
      "createTime": "2025-06-15T14:30:00",
      "updateTime": "2025-06-15T14:30:00"
    }
  ]
}
```

**执行逻辑**：调用 `userMapper.getUserPosts(userId)` 查询当前用户的所有帖子。

---

### 8. `POST /user/delete/post` — 删除自己的帖子

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/user/delete/post` |
| **权限** | 需认证 |
| **请求参数** | `@RequestParam Integer postId`（帖子 ID） |
| **响应格式** | `{ "code": 200, "message": "删除成功", "data": null }` |

**执行逻辑：**

1. 开启事务 `@Transactional`
2. 调用 `forumPostMapper.deletePost(postId)` 删除帖子
3. 调用 `forumLikeMapper.deleteByTarget(1, postId)` 级联删除该帖子的所有点赞

---

### 9. `POST /user/comments` — 获取当前用户的评论列表

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/user/comments` |
| **权限** | 需认证 |
| **请求参数** | 无（用户 ID 从 token 解析） |

**响应格式：**

```json
{
  "code": 200,
  "data": [
    {
      "commentId": 25,
      "postId": 5,
      "userId": 2,
      "parentCommentId": null,
      "replyUserId": null,
      "content": "我的评论内容",
      "likeCount": 3,
      "status": "正常",
      "createTime": "2025-06-18T09:20:00"
    }
  ]
}
```

**执行逻辑**：调用 `userMapper.getUserComments(userId)` 查询当前用户的所有评论。

---

## 三、板块模块 — ForumBoardController（前缀 `/forum`）

> **权限**：`POST /forum/boards` 与 `POST /forum/board/posts` 两个端点由 SecurityConfig 设为公开访问（`permitAll`）。这是为了让未登录用户也能浏览板块列表。

---

### 10. `POST /forum/boards` — 获取所有板块列表

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/forum/boards` |
| **权限** | **公开** |
| **请求参数** | 无 |

**响应格式：**

```json
{
  "code": 200,
  "data": [
    {
      "boardId": 1,
      "boardName": "技术讨论",
      "description": "技术话题交流区",
      "sortNum": 1,
      "status": "启用",
      "createTime": "2025-01-01T00:00:00",
      "updateTime": "2025-06-01T00:00:00"
    }
  ]
}
```

**执行逻辑**：调用 `forumBoardMapper.getBoards()` 查询所有板块，按 `sortNum` 排序。

---

### 11. `POST /forum/board/posts` — 获取指定板块的帖子列表

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/forum/board/posts` |
| **权限** | **公开** |
| **请求参数** | `@RequestBody ForumBoard` — JSON 对象 |
| **请求字段** | `boardId: Integer`（必填，板块 ID） |
| **请求示例** | `{ "boardId": 1 }` |

**响应格式**：

```json
{
  "code": 200,
  "data": [
    {
      "postId": 5,
      "boardId": 1,
      "authorId": 2,
      "title": "板块内的帖子",
      "content": "<p>内容 HTML</p>",
      "viewCount": 120,
      "likeCount": 8,
      "commentCount": 3,
      "isTop": 0,
      "createTime": "2025-06-15T14:30:00"
    }
  ]
}
```

**执行逻辑**：校验 `boardId` 不为空，调用 `forumBoardMapper.getBoardPosts(boardId)` 查询。

---

## 四、帖子模块 — ForumPostController（前缀 `/posts`）

> **权限**：`/posts/detail` 与 `/posts/list` 由 SecurityConfig 设为公开访问；其余均需认证。
> **身份校验**：编辑和删除操作中，Controller 从 token 提取当前用户 ID 传给 Service 层，Service 层负责校验帖子所有权。

---

### 12. `POST /posts/create` — 发布帖子

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/posts/create` |
| **权限** | 需认证 |
| **请求参数** | `@RequestBody ForumPost` — JSON 对象 |
| **请求字段** | `boardId: Integer`（必填，所属板块 ID）<br>`title: String`（必填，标题）<br>`content: String`（必填，富文本 HTML 内容） |
| **请求示例** | `{ "boardId": 1, "title": "新帖标题", "content": "<p>正文内容</p>" }` |

**响应格式**：

```json
{
  "code": 200,
  "message": "发布成功",
  "data": {
    "postId": 16,
    "boardId": 1,
    "authorId": 2,
    "title": "新帖标题",
    "content": "<p>正文内容</p>",
    "viewCount": 0,
    "likeCount": 0,
    "commentCount": 0,
    "isTop": 0,
    "status": "正常",
    "createTime": "2025-06-26T16:10:00",
    "updateTime": "2025-06-26T16:10:00"
  }
}
```

**执行逻辑：**

1. 从 token 提取当前用户 ID 设为 `authorId`
2. 校验 `boardId` 和 `authorId` 不为空
3. 校验 `title` 不为空或空白
4. 校验 `content` 不为空或空白
5. 调用 `forumPostMapper.insertPost(post)` 插入，返回的 post 对象含自增 ID

---

### 13. `POST /posts/detail` — 获取帖子详情

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/posts/detail` |
| **权限** | **公开** |
| **请求参数** | `@RequestParam Integer postId`（帖子 ID） |

**响应格式：** 同 12 的 `data` 结构，但 `viewCount` 会自动 +1。

**执行逻辑：**

1. 校验 `postId` 不为空
2. **开启事务**：在同一事务中先 `incrementViewCount(postId)` 再 `getPostById(postId)`，保证浏览量和详情数据一致性

---

### 14. `POST /posts/list` — 获取帖子列表（支持搜索和分页）

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/posts/list` |
| **权限** | **公开** |
| **请求参数** | `@RequestParam(required=false) Integer boardId`（板块 ID，选填）<br>`@RequestParam(required=false) String keyword`（搜索关键词，选填）<br>`@RequestParam(required=false) Integer page`（页码，默认 1）<br>`@RequestParam(required=false) Integer size`（每页条数，默认 10） |
| **请求示例** | `/posts/list?boardId=1&keyword=Java&page=1&size=10` |

**响应格式：**

```json
{
  "code": 200,
  "data": [
    {
      "postId": 10,
      "boardId": 1,
      "authorId": 3,
      "title": "Java 入门教程",
      "content": "<p>内容概要...</p>",
      "viewCount": 500,
      "likeCount": 45,
      "commentCount": 20,
      "isTop": 0,
      "createTime": "2025-06-01T10:00:00"
    }
  ]
}
```

**执行逻辑：**

1. `page` 为 null 或 < 1 默认设为 1
2. `size` 为 null 或 < 1 默认设为 10
3. 计算 `offset = (page - 1) × size`
4. 调用 `forumPostMapper.getPosts(boardId, keyword, offset, size)`
5. 支持三种查询模式：全量（无参数）、按板块过滤（仅 boardId）、按关键词搜索（仅 keyword）、组合过滤（boardId + keyword）

---

### 15. `POST /posts/update` — 编辑帖子（仅作者本人）

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/posts/update` |
| **权限** | 需认证 |
| **请求参数** | `@RequestBody ForumPost` — JSON 对象 |
| **请求字段** | `postId: Integer`（必填）<br>`title: String`（选填）<br>`content: String`（选填） |
| **请求示例** | `{ "postId": 10, "title": "修改后的标题" }` |

**响应格式**：`{ "code": 200, "message": "修改成功", "data": null }`

**执行逻辑：**

1. 从 token 提取当前用户 ID 作为 `operatorId`
2. Controller 调用 `forumPostService.updateOwnPost(post, operatorId)`
3. Service 层调用 `ensurePostOwner(postId, operatorId)`：
   - 查询帖子是否存在 → 不存在返回 `"帖子不存在"`
   - 对比 `operatorId` 和 `post.authorId` → 不一致返回 `"只能操作自己的帖子"`
4. 验证通过后调用 `forumPostMapper.updatePost(post)` 更新

---

### 16. `POST /posts/delete` — 删除帖子（仅作者本人，级联删除）

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/posts/delete` |
| **权限** | 需认证 |
| **请求参数** | `@RequestParam Integer postId`（帖子 ID） |

**响应格式**：`{ "code": 200, "message": "删除成功", "data": null }`

**执行逻辑：**

1. 从 token 提取当前用户 ID 作为 `operatorId`
2. Controller 调用 `forumPostService.deleteOwnPost(postId, operatorId)`
3. Service 层调用 `ensurePostOwner()` 验证所有权
4. **开启事务**，级联执行四步：
   - `forumPostMapper.deletePost(postId)` — 删除帖子
   - `forumLikeMapper.deleteByTarget(1, postId)` — 删除帖子的直接点赞
   - `forumLikeMapper.deleteCommentLikesByPostId(postId)` — 删除帖子下所有评论的点赞
   - `forumCommentMapper.deleteCommentsByPostId(postId)` — 删除帖子下所有评论

---

## 五、评论模块 — ForumCommentController（前缀 `/comments`）

> **权限**：`/comments/list` 公开访问；其余需认证。
> **数据结构**：评论树仅支持两级（一级评论 + 二级回复），Service 层在内存中构建树形结构。

---

### 17. `POST /comments/create` — 发表评论

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/comments/create` |
| **权限** | 需认证 |
| **请求参数** | `@RequestBody ForumComment` — JSON 对象 |
| **请求字段** | `postId: Integer`（必填，所属帖子 ID）<br>`content: String`（必填，评论内容）<br>`parentCommentId: Integer`（选填，回复一级评论时传入）<br>`replyUserId: Integer`（选填，被回复用户的 ID） |
| **请求示例** | `{ "postId": 5, "content": "好文章！", "parentCommentId": null }`<br>回复示例：`{ "postId": 5, "content": "同意楼上", "parentCommentId": 12, "replyUserId": 3 }` |

**响应格式：**

```json
{
  "code": 200,
  "message": "评论成功",
  "data": {
    "commentId": 30,
    "postId": 5,
    "userId": 2,
    "parentCommentId": null,
    "replyUserId": null,
    "content": "好文章！",
    "likeCount": 0,
    "createTime": "2025-06-26T16:20:00"
  }
}
```

**执行逻辑：**

1. 从 token 提取当前用户 ID 设为 `userId`
2. 校验 `postId` 和 `userId` 不为空，`content` 不为空或空白
3. **开启事务**：
   - 调用 `forumCommentMapper.insertComment(comment)` 插入评论
   - 调用 `forumPostMapper.incrementCommentCount(postId)` 递增帖子的评论数

---

### 18. `POST /comments/list` — 获取帖子的评论列表（树形结构）

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/comments/list` |
| **权限** | **公开** |
| **请求参数** | `@RequestParam Integer postId`（帖子 ID） |

**响应格式：**

```json
{
  "code": 200,
  "data": [
    {
      "commentId": 12,
      "postId": 5,
      "userId": 3,
      "parentCommentId": null,
      "content": "一级评论",
      "likeCount": 5,
      "children": [
        {
          "commentId": 15,
          "postId": 5,
          "userId": 2,
          "parentCommentId": 12,
          "replyUserId": 3,
          "content": "二级回复",
          "likeCount": 2,
          "children": []
        }
      ],
      "createTime": "2025-06-26T16:00:00"
    }
  ]
}
```

**执行逻辑：**

1. 调用 `forumCommentMapper.getCommentsByPostId(postId)` 获取该帖所有评论
2. **内存构建评论树**：
   - 第一遍：所有评论初始化 `children` 为空列表，收集所有根评论（`parentCommentId == null`）
   - 第二遍：将子评论（`parentCommentId != null`）挂载到对应父评论的 `children` 列表中
   - **孤儿评论处理**：如果子评论的父评论已被删除（找不到对应父评论），该子评论被降级为根评论

---

### 19. `POST /comments/delete` — 删除评论（仅作者本人）

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/comments/delete` |
| **权限** | 需认证 |
| **请求参数** | `@RequestParam Integer commentId`（评论 ID） |

**响应格式**：`{ "code": 200, "message": "删除成功", "data": null }`

**执行逻辑：**

1. 从 token 提取当前用户 ID 作为 `operatorId`
2. Controller 调用 `forumCommentService.deleteOwnComment(commentId, operatorId)`
3. Service 层校验评论存在且 `comment.userId == operatorId`，不匹配返回 `"只能删除自己的评论"`
4. **开启事务**：
   - `forumCommentMapper.deleteComment(commentId)` — 删除评论
   - `forumLikeMapper.deleteByTarget(2, commentId)` — 删除该评论的点赞
   - `forumPostMapper.decrementCommentCount(postId)` — 递减帖子的评论数
5. 评论下的二级回复不会被级联删除（设计决策：子评论仍保留在树中，只标记父评论为 null）

---

## 六、点赞模块 — ForumLikeController（前缀 `/likes`）

> **通用点赞模型**：`targetType=1` 表示帖子，`targetType=2` 表示评论。
> **权限**：`/likes/count` 公开访问；其余需认证。
> **幂等设计**：取消点赞时若未点赞不报错；重复点赞会被拦截。

---

### 20. `POST /likes/like` — 给帖子或评论点赞

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/likes/like` |
| **权限** | 需认证 |
| **请求参数** | `@RequestParam Integer targetType`（1=帖子, 2=评论）<br>`@RequestParam Integer targetId`（目标 ID） |

**响应格式**：`{ "code": 200, "message": "点赞成功", "data": null }`

**执行逻辑：**

1. 从 token 提取当前用户 ID
2. 校验 `targetType` 仅允许 1 或 2
3. 查询是否已点赞 → 已点则返回错误 `"不能重复点赞"`
4. **开启事务**：
   - `forumLikeMapper.insertLike(like)` — 插入点赞记录
   - 根据 `targetType` 递增对应表的 `likeCount`（帖子 → `forumPostMapper.incrementLikeCount`，评论 → `forumCommentMapper.incrementLikeCount`）

---

### 21. `POST /likes/unlike` — 取消点赞

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/likes/unlike` |
| **权限** | 需认证 |
| **请求参数** | `@RequestParam Integer targetType`<br>`@RequestParam Integer targetId` |

**响应格式**：`{ "code": 200, "message": "取消点赞成功", "data": null }`

**执行逻辑：**

1. 校验 `targetType` 仅允许 1 或 2
2. 查询是否已点赞 → 未点赞直接返回（幂等，不报错）
3. **开启事务**：删除点赞记录 + 递减对应表的 `likeCount`

---

### 22. `POST /likes/status` — 查询当前用户对某对象的点赞状态

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/likes/status` |
| **权限** | 需认证 |
| **请求参数** | `@RequestParam Integer targetType`<br>`@RequestParam Integer targetId` |

**响应格式**：`{ "code": 200, "data": true }` 或 `{ "code": 200, "data": false }`

**执行逻辑**：查询 `forum_like` 表中是否存在 `(targetType, targetId, userId)` 的点赞记录，返回布尔值。

---

### 23. `POST /likes/count` — 查询某对象的点赞总数

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/likes/count` |
| **权限** | **公开** |
| **请求参数** | `@RequestParam Integer targetType`<br>`@RequestParam Integer targetId` |

**响应格式**：`{ "code": 200, "data": 42 }`

**执行逻辑**：调用 `forumLikeMapper.countByTarget(targetType, targetId)` 统计点赞记录数。

---

## 七、管理后台模块 — AdminController（前缀 `/admin`）

> **权限**：所有接口均需要 `ADMIN` 角色，由 SecurityConfig 在 URL 层面拦截。
> **特点**：管理员拥有绕过所有权校验的"超级权限"——可直接删除任何人的帖子和评论。

---

### 7.1 用户管理

#### 24. `GET /admin/users` — 获取所有用户列表

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | GET |
| **相对路径** | `/admin/users` |
| **权限** | ADMIN |
| **请求参数** | 无 |

**响应格式：**

```json
{
  "code": 200,
  "data": [
    {
      "userId": 1,
      "username": "admin",
      "nickname": "管理员",
      "role": "ADMIN",
      "status": "启用",
      "createTime": "2025-01-01T00:00:00",
      "updateTime": "2025-06-01T00:00:00"
    }
  ]
}
```

**执行逻辑**：调用 `userMapper.getAllUsers()`，遍历结果将每个用户的 `password` 置 `null`。

---

#### 25. `PUT /admin/users/{userId}/status` — 启用/禁用用户

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | PUT |
| **相对路径** | `/admin/users/{userId}/status` |
| **权限** | ADMIN |
| **请求参数** | `@PathVariable Integer userId`（路径参数）<br>`@RequestBody Integer status`（JSON 数字，1=启用，0=禁用） |
| **请求示例** | `PUT /admin/users/5/status` — Body: `1` |

**响应格式**：`{ "code": 200, "message": "操作成功", "data": null }`

**执行逻辑**：调用 `userMapper.updateUserStatus(userId, status)` 更新用户状态字段。

---

### 7.2 板块管理

#### 26. `GET /admin/forums` — 获取所有板块

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | GET |
| **相对路径** | `/admin/forums` |
| **权限** | ADMIN |
| **请求参数** | 无 |

**响应格式**：同接口 10（`/forum/boards`），返回所有板块列表。

**执行逻辑**：调用 `forumBoardMapper.getBoards()`。

---

#### 27. `POST /admin/forums` — 新增板块

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/admin/forums` |
| **权限** | ADMIN |
| **请求参数** | `@RequestBody ForumBoard` |
| **请求字段** | `boardName: String`（板块名称）<br>`description: String`（板块描述）<br>`sortNum: Integer`（排序号） |
| **请求示例** | `{ "boardName": "新板块", "description": "板块描述", "sortNum": 10 }` |

**响应格式**：`{ "code": 200, "message": "新增成功", "data": { "boardId": 5, "boardName": "新板块", ... } }`

**执行逻辑**：调用 `forumBoardMapper.addBoard(forumBoard)` 插入。

---

#### 28. `PUT /admin/forums/{forumId}` — 编辑板块

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | PUT |
| **相对路径** | `/admin/forums/{forumId}` |
| **权限** | ADMIN |
| **请求参数** | `@PathVariable Integer forumId`<br>`@RequestBody ForumBoard` |

**响应格式**：`{ "code": 200, "message": "修改成功", "data": null }`

**执行逻辑**：`forumId` 注入到 board 对象后，调用 `forumBoardMapper.updateBoard(forumBoard)`。

---

#### 29. `PUT /admin/forums/{forumId}/status` — 启用/禁用板块

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | PUT |
| **相对路径** | `/admin/forums/{forumId}/status` |
| **权限** | ADMIN |
| **请求参数** | `@PathVariable Integer forumId`<br>`@RequestBody Integer status` |

**执行逻辑**：调用 `forumBoardMapper.updateBoardStatus(forumId, status)`。

---

#### 30. `PUT /admin/forums/sort` — 拖拽排序板块

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | PUT |
| **相对路径** | `/admin/forums/sort` |
| **权限** | ADMIN |
| **请求参数** | `@RequestBody List<Long> boardIds` — 按新顺序排列的板块 ID 数组 |
| **请求示例** | `[3, 1, 5, 2, 4]` |

**响应格式**：`{ "code": 200, "message": "排序成功", "data": null }`

**执行逻辑：**

1. 校验 `boardIds` 不为空，不包含 `null` 元素
2. 调用 `forumBoardMapper.sortBoards(boardIds)` 按传入顺序更新每个板块的 `sortNum`

---

#### 31. `DELETE /admin/forums/{forumId}` — 删除板块

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | DELETE |
| **相对路径** | `/admin/forums/{forumId}` |
| **权限** | ADMIN |
| **请求参数** | `@PathVariable Integer forumId` |

**响应格式**：`{ "code": 200, "message": "删除成功", "data": null }`

**执行逻辑**：调用 `forumBoardMapper.deleteBoard(forumId)`。**注意**：此操作不会级联删除板块下的帖子。

---

### 7.3 内容管理

#### 32. `PUT /admin/posts/{postId}/top` — 置顶/取消置顶帖子

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | PUT |
| **相对路径** | `/admin/posts/{postId}/top` |
| **权限** | ADMIN |
| **请求参数** | `@PathVariable Integer postId`<br>`@RequestBody Integer isTop`（1=置顶, 0=取消） |

**响应格式**：`{ "code": 200, "message": "操作成功", "data": null }`

**执行逻辑**：校验 `postId` 和 `isTop` 不为空，调用 `forumPostMapper.updateTop(postId, isTop)`。

---

#### 33. `DELETE /admin/posts/{postId}` — 管理员删除帖子（级联删除）

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | DELETE |
| **相对路径** | `/admin/posts/{postId}` |
| **权限** | ADMIN |
| **请求参数** | `@PathVariable Integer postId` |

**响应格式**：`{ "code": 200, "message": "删除成功", "data": null }`

**执行逻辑**：管理员直接调用 `forumPostService.deletePost(postId)`，**绕过所有权校验**，执行完整级联删除（帖子 → 帖子点赞 → 评论点赞 → 评论）。

---

#### 34. `DELETE /admin/comments/{commentId}` — 管理员删除评论

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | DELETE |
| **相对路径** | `/admin/comments/{commentId}` |
| **权限** | ADMIN |
| **请求参数** | `@PathVariable Integer commentId` |

**响应格式**：`{ "code": 200, "message": "删除成功", "data": null }`

**执行逻辑**：管理员直接调用 `forumCommentService.deleteComment(commentId)`，绕过所有权校验，级联执行（评论 → 评论点赞 → 递减帖子评论数）。

---

#### 35. `GET /admin/comments` — 获取所有评论

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | GET |
| **相对路径** | `/admin/comments` |
| **权限** | ADMIN |
| **请求参数** | 无 |

**响应格式**：所有评论的扁平列表（不构建树形结构），每个评论的 `children` 初始化空列表。

**执行逻辑**：调用 `forumCommentMapper.getAllComments()`，遍历初始化 `children = []`。

---

### 7.4 数据统计（Spark 离线分析）

> **数据来源**：所有统计接口的数据均来自 Spark 离线跑批写入的 `stat_*` 统计表（共 6 张），**不直接查询业务表**。容忍日级延迟，Spark 每日 cron 03:00 跑批或手动触发。
>
> **null 安全处理**：所有数值字段经 `safe()` 方法将 `null` 转为 0，避免 NPE。

#### 36. `GET /admin/statistics/overview` — 统计概览

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | GET |
| **相对路径** | `/admin/statistics/overview` |
| **权限** | ADMIN |
| **请求参数** | 无 |

**响应格式：**

```json
{
  "code": 200,
  "data": {
    "userCount": 150,
    "activeUserCount": 45,
    "enabledBoardCount": 5,
    "postCount": 320,
    "commentCount": 1200,
    "likeCount": 3500,
    "updateTime": "2025-06-26T03:00:00"
  }
}
```

**执行逻辑**：调用 `statMapper.getOverview()`，`null` 字段默认 0。

**定位**：管理后台统计看板的顶层概览卡片，展示论坛全局数据快照。

---

#### 37. `GET /admin/statistics/hot-posts` — 热门帖子排行

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | GET |
| **相对路径** | `/admin/statistics/hot-posts` |
| **权限** | ADMIN |
| **请求参数** | `@RequestParam(required=false) Integer limit`（返回数量，默认 10，上限 50） |

**响应格式：**

```json
{
  "code": 200,
  "data": [
    {
      "rankNum": 1,
      "postId": 5,
      "boardId": 1,
      "authorId": 3,
      "title": "热门帖标题",
      "viewCount": 1200,
      "likeCount": 85,
      "commentCount": 42,
      "hotScore": 296.0,
      "updateTime": "2025-06-26T03:00:00"
    }
  ]
}
```

**热度分计算**（Spark 侧）：`hotScore = likeCount × 2 + commentCount × 3 + viewCount × 0.1`

**执行逻辑**：`limit` 默认 10，截断至 max(1, min(limit, 50))，调用 `statMapper.getHotPosts(limit)`。

---

#### 38. `GET /admin/statistics/board-active` — 板块活跃度排行

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | GET |
| **相对路径** | `/admin/statistics/board-active` |
| **权限** | ADMIN |
| **请求参数** | 无 |

**响应格式：**

```json
{
  "code": 200,
  "data": [
    {
      "boardId": 1,
      "boardName": "技术讨论",
      "postCount": 85,
      "viewSum": 15000,
      "likeSum": 420,
      "commentSum": 310,
      "activeScore": 78.5,
      "updateTime": "2025-06-26T03:00:00"
    }
  ]
}
```

**执行逻辑**：调用 `statMapper.getBoardActive()`。

---

#### 39. `GET /admin/statistics/active-users` — 用户活跃度排行

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | GET |
| **相对路径** | `/admin/statistics/active-users` |
| **权限** | ADMIN |
| **请求参数** | `@RequestParam(required=false) Integer limit`（默认 10，上限 50） |

**响应格式：**

```json
{
  "code": 200,
  "data": [
    {
      "rankNum": 1,
      "userId": 3,
      "username": "active_user",
      "nickname": "活跃用户",
      "postCount": 25,
      "commentCount": 80,
      "likeCount": 150,
      "activeScore": 95.0,
      "updateTime": "2025-06-26T03:00:00"
    }
  ]
}
```

**执行逻辑**：`limit` 默认 10，截断至 max(1, min(limit, 50))，调用 `statMapper.getActiveUsers(limit)`。

---

#### 40. `GET /admin/statistics/content-quality` — 内容质量分析

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | GET |
| **相对路径** | `/admin/statistics/content-quality` |
| **权限** | ADMIN |
| **请求参数** | `@RequestParam(required=false) String qualityType`（分类筛选，选填） |

**qualityType 枚举值：**

| 值 | 含义 | 判定标准（Spark 侧） |
|----|------|-------------------|
| `HIGH_LIKE_LOW_COMMENT` | 高赞低评论（优质干货） | 点赞 >= 10 且 评论 <= 5 |
| `HIGH_COMMENT_LOW_LIKE` | 高评论低赞（争议帖） | 评论 >= 8 且 点赞 <= 3 |
| `HOT` | 综合热门 | 热度 >= 40 |

**响应格式：**

```json
{
  "code": 200,
  "data": [
    {
      "postId": 8,
      "title": "高质量帖子",
      "likeCount": 25,
      "commentCount": 2,
      "viewCount": 500,
      "qualityType": "HIGH_LIKE_LOW_COMMENT",
      "updateTime": "2025-06-26T03:00:00"
    }
  ]
}
```

**执行逻辑**：`qualityType` 为空时返回全量，否则按分类过滤。调用 `statMapper.getContentQuality(qualityType)`。

---

#### 41. `GET /admin/statistics/daily-trend` — 每日趋势

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | GET |
| **相对路径** | `/admin/statistics/daily-trend` |
| **权限** | ADMIN |
| **请求参数** | `@RequestParam(required=false) Integer days`（最近天数，默认 7，上限 90） |

**响应格式：**

```json
{
  "code": 200,
  "data": [
    {
      "statDate": "2025-06-20",
      "newUserCount": 5,
      "newPostCount": 12,
      "newCommentCount": 45,
      "newLikeCount": 80,
      "updateTime": "2025-06-26T03:00:00"
    }
  ]
}
```

**执行逻辑**：`days` 默认 7，截断至 max(1, min(days, 90))，调用 `statMapper.getDailyTrend(days)`。

**定位**：前端 ECharts 折线图的唯一数据源，展示论坛增长趋势。

---

#### 42. `POST /admin/statistics/trigger-spark` — 手动触发 Spark 跑批

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | POST |
| **相对路径** | `/admin/statistics/trigger-spark` |
| **权限** | ADMIN |
| **请求参数** | 无 |

**响应格式：**

```json
// 成功触发
{ "code": 200, "data": { "state": "RUNNING", "message": "Spark 跑批已触发", "taskId": "uuid", "startTime": "2025-06-26T16:30:00" } }

// 已有任务在运行
{ "code": 200, "data": { "state": "RUNNING", "message": "Spark 跑批正在运行中", "taskId": "uuid" } }

// 未启用
{ "code": 200, "data": { "state": "DISABLED", "message": "Spark 跑批未启用（缺少配置）" } }
```

**执行逻辑：**

1. 检查 `spark.enabled` 配置是否为 `true` → 否则返回 `DISABLED`
2. 检查 `spark.db-user` 和 `spark.db-pass` 是否配置 → 否则返回 `DISABLED`
3. 使用 `AtomicReference` CAS 操作保证同时只有一个跑批任务运行（并发控制）
4. 若已有任务在运行，返回 `RUNNING` + 已有任务信息
5. 新任务：启动守护线程 `new Thread(this::executeSpark)` 异步执行，立即返回 `RUNNING`
6. 后台线程通过 `ProcessBuilder` 执行 `spark-submit` 命令，传入 `--class cn.edu.zut.sparkstats.ForumSparkAnalysis` 和 JDBC 参数
7. Spark 进程读取业务表（`forum_post`/`forum_comment`/`forum_like`/`sys_user`），聚合后写入 6 张 `stat_*` 统计表

**状态机**：`READY`（初始）→ `RUNNING`（触发后）→ `SUCCESS` / `FAILED` / `DISABLED`

---

#### 43. `GET /admin/statistics/spark-status` — 查询跑批状态

| 项目 | 说明 |
|------|------|
| **HTTP 方法** | GET |
| **相对路径** | `/admin/statistics/spark-status` |
| **权限** | ADMIN |
| **请求参数** | 无 |

**响应格式：**

```json
// 运行中
{ "code": 200, "data": { "state": "RUNNING", "taskId": "uuid", "startTime": "2025-06-26T16:30:00", "endTime": null, "durationMillis": null, "message": null } }

// 成功
{ "code": 200, "data": { "state": "SUCCESS", "taskId": "uuid", "startTime": "2025-06-26T16:30:00", "endTime": "2025-06-26T16:32:15", "durationMillis": 135000, "message": null } }

// 失败
{ "code": 200, "data": { "state": "FAILED", "taskId": "uuid", "startTime": "2025-06-26T16:30:00", "endTime": "2025-06-26T16:30:10", "durationMillis": 10000, "message": "Exit code 1: ..." } }
```

**执行逻辑**：纯内存查询，读取 `lastRun`（AtomicReference）中保存的最新一次运行状态快照，返回包含 `state`/`taskId`/`startTime`/`endTime`/`durationMillis`/`message` 的完整状态 Map。

**定位**：前端轮询此接口（每 2 秒一次）追踪 Spark 跑批进度，直到状态变为 `SUCCESS` 或 `FAILED`。

---

## 附录 A：权限矩阵总表

| 端点 | 方法 | 公开 | USER | ADMIN | JWT 必需 |
|------|------|:---:|:----:|:-----:|:--------:|
| `/auth/login` | POST | ✅ | ✅ | ✅ | ❌ |
| `/auth/register` | POST | ✅ | ✅ | ✅ | ❌ |
| `/auth/logout` | POST | ✅ | ✅ | ✅ | ❌ |
| `/user/profile` | POST | ❌ | ✅ | ✅ | ✅ |
| `/user/update/profile` | POST | ❌ | ✅ | ✅ | ✅ |
| `/user/update/password` | POST | ❌ | ✅ | ✅ | ✅ |
| `/user/posts` | POST | ❌ | ✅ | ✅ | ✅ |
| `/user/delete/post` | POST | ❌ | ✅ | ✅ | ✅ |
| `/user/comments` | POST | ❌ | ✅ | ✅ | ✅ |
| `/forum/boards` | POST | ✅ | ✅ | ✅ | ❌ |
| `/forum/board/posts` | POST | ✅ | ✅ | ✅ | ❌ |
| `/posts/create` | POST | ❌ | ✅ | ✅ | ✅ |
| `/posts/detail` | POST | ✅ | ✅ | ✅ | ❌ |
| `/posts/list` | POST | ✅ | ✅ | ✅ | ❌ |
| `/posts/update` | POST | ❌ | ✅ | ✅ | ✅ |
| `/posts/delete` | POST | ❌ | ✅ | ✅ | ✅ |
| `/comments/create` | POST | ❌ | ✅ | ✅ | ✅ |
| `/comments/list` | POST | ✅ | ✅ | ✅ | ❌ |
| `/comments/delete` | POST | ❌ | ✅ | ✅ | ✅ |
| `/likes/like` | POST | ❌ | ✅ | ✅ | ✅ |
| `/likes/unlike` | POST | ❌ | ✅ | ✅ | ✅ |
| `/likes/status` | POST | ❌ | ✅ | ✅ | ✅ |
| `/likes/count` | POST | ✅ | ✅ | ✅ | ❌ |
| `/admin/users` | GET | ❌ | ❌ | ✅ | ✅ |
| `/admin/users/{userId}/status` | PUT | ❌ | ❌ | ✅ | ✅ |
| `/admin/forums` | GET | ❌ | ❌ | ✅ | ✅ |
| `/admin/forums` | POST | ❌ | ❌ | ✅ | ✅ |
| `/admin/forums/{forumId}` | PUT | ❌ | ❌ | ✅ | ✅ |
| `/admin/forums/{forumId}/status` | PUT | ❌ | ❌ | ✅ | ✅ |
| `/admin/forums/sort` | PUT | ❌ | ❌ | ✅ | ✅ |
| `/admin/forums/{forumId}` | DELETE | ❌ | ❌ | ✅ | ✅ |
| `/admin/posts/{postId}/top` | PUT | ❌ | ❌ | ✅ | ✅ |
| `/admin/posts/{postId}` | DELETE | ❌ | ❌ | ✅ | ✅ |
| `/admin/comments/{commentId}` | DELETE | ❌ | ❌ | ✅ | ✅ |
| `/admin/comments` | GET | ❌ | ❌ | ✅ | ✅ |
| `/admin/statistics/overview` | GET | ❌ | ❌ | ✅ | ✅ |
| `/admin/statistics/hot-posts` | GET | ❌ | ❌ | ✅ | ✅ |
| `/admin/statistics/board-active` | GET | ❌ | ❌ | ✅ | ✅ |
| `/admin/statistics/active-users` | GET | ❌ | ❌ | ✅ | ✅ |
| `/admin/statistics/content-quality` | GET | ❌ | ❌ | ✅ | ✅ |
| `/admin/statistics/daily-trend` | GET | ❌ | ❌ | ✅ | ✅ |
| `/admin/statistics/trigger-spark` | POST | ❌ | ❌ | ✅ | ✅ |
| `/admin/statistics/spark-status` | GET | ❌ | ❌ | ✅ | ✅ |

---

## 附录 B：实体字段速查表

### User（用户）
| 字段 | 类型 | 说明 |
|------|------|------|
| `userId` | Integer | 主键 |
| `username` | String | 用户名 |
| `password` | String | BCrypt 加密 |
| `nickname` | String | 昵称 |
| `avatar` | String | 头像 URL |
| `signature` | String | 个性签名 |
| `role` | String | `USER` / `ADMIN` |
| `status` | String | 启用/禁用 |

### ForumPost（帖子）
| 字段 | 类型 | 说明 |
|------|------|------|
| `postId` | Integer | 主键 |
| `boardId` | Integer | 所属板块 |
| `authorId` | Integer | 作者 |
| `title` | String | 标题 |
| `content` | String | 富文本 HTML |
| `viewCount` | Integer | 浏览量 |
| `likeCount` | Integer | 点赞数 |
| `commentCount` | Integer | 评论数 |
| `isTop` | Integer | 置顶 0/1 |

### ForumComment（评论）
| 字段 | 类型 | 说明 |
|------|------|------|
| `commentId` | Integer | 主键 |
| `postId` | Integer | 所属帖子 |
| `userId` | Integer | 评论者 |
| `parentCommentId` | Integer | 父评论（null=根评论） |
| `replyUserId` | Integer | 回复目标用户 |
| `content` | String | 评论内容 |
| `likeCount` | Integer | 点赞数 |

### ForumLike（点赞）
| 字段 | 类型 | 说明 |
|------|------|------|
| `likeId` | Integer | 主键 |
| `targetType` | Integer | 1=帖子, 2=评论 |
| `targetId` | Integer | 目标 ID |
| `userId` | Integer | 点赞用户 |

### ForumBoard（板块）
| 字段 | 类型 | 说明 |
|------|------|------|
| `boardId` | Integer | 主键 |
| `boardName` | String | 板块名称 |
| `description` | String | 板块描述 |
| `sortNum` | Integer | 排序号 |
| `status` | String | 启用/禁用 |

---

*本文档基于源码 `src/main/java/cn/edu/zut/softwareengineering/controller/` 下全部 7 个 Controller 文件及相关 Service/Entity/Mapper 逆向分析整理。统计共 **43 个 API 端点**，覆盖认证、用户、板块、帖子、评论、点赞、管理后台、数据统计全部模块。*
