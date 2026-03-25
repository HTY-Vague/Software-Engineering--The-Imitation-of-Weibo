# 论坛系统Service功能及修饰符对应表

## 1. 用户模块 (UserService)

| 功能 | Controller方法 | Service方法 | Service返回类型 | Controller返回类型 |
|------|---------------|-------------|-----------------|-------------------|
| 用户注册 | `POST /auth/register` | `register(User user)` | `User` | `ResponseEntity<User>` |
| 用户登录 | `POST /auth/login` | `login(User user)` | `User` | `ResponseEntity<User>` |
| 用户退出 | `POST /auth/logout` | `logout(User user)` | `User` | `ResponseEntity<User>` |
| 获取个人信息 | `GET /user/profile` | `getProfile(Long userId)` | `User` | `ResponseEntity<User>` |
| 修改个人信息 | `PUT /user/profile` | `updateProfile(User user)` | `void` | `void` |
| 修改密码 | `PUT /user/password` | `updatePassword(Long userId, String oldPassword, String newPassword)` | `void` | `void` |
| 我的帖子列表 | `GET /user/posts` | `getMyPosts(Long userId)` | `List<ForumPost>` | `ResponseEntity<List<ForumPost>>` |
| 我的评论列表 | `GET /user/comments` | `getMyComments(Long userId)` | `List<ForumComment>` | `ResponseEntity<List<ForumComment>>` |

---

## 2. 板块模块 (ForumBoardService)

| 功能 | Controller方法 | Service方法 | Service返回类型 | Controller返回类型 |
|------|---------------|-------------|-----------------|-------------------|
| 板块列表 | `GET /boards` | `getAllBoards()` | `List<ForumBoard>` | `ResponseEntity<List<ForumBoard>>` |
| 新增板块 | `POST /admin/boards` | `addBoard(ForumBoard board)` | `void` | `void` |
| 修改板块 | `PUT /admin/boards/{boardId}` | `updateBoard(ForumBoard board)` | `void` | `void` |
| 启用/禁用板块 | `PUT /admin/boards/{boardId}/status` | `updateBoardStatus(Long boardId, Integer status)` | `void` | `void` |
| 板块排序 | `PUT /admin/boards/sort` | `sortBoards(List<ForumBoard> boards)` | `void` | `void` |
| 查看板块帖子 | `GET /boards/{boardId}/posts` | `getBoardPosts(Long boardId)` | `List<ForumPost>` | `ResponseEntity<List<ForumPost>>` |

---

## 3. 帖子模块 (ForumPostService)

| 功能 | Controller方法 | Service方法 | Service返回类型 | Controller返回类型 |
|------|---------------|-------------|-----------------|-------------------|
| 发布帖子 | `POST /posts` | `createPost(ForumPost post)` | `void` | `void` |
| 帖子列表 | `GET /posts` | `getAllPosts()` | `List<ForumPost>` | `ResponseEntity<List<ForumPost>>` |
| 帖子详情 | `GET /posts/{postId}` | `getPostById(Long postId)` | `ForumPost` | `ResponseEntity<ForumPost>` |
| 编辑帖子 | `PUT /posts/{postId}` | `updatePost(ForumPost post)` | `void` | `void` |
| 删除帖子 | `DELETE /posts/{postId}` | `deletePost(Long postId)` | `void` | `void` |
| 帖子搜索 | `GET /posts/search?keyword=xxx` | `searchPosts(String keyword)` | `List<ForumPost>` | `ResponseEntity<List<ForumPost>>` |
| 浏览量统计 | `PUT /posts/{postId}/view` | `incrementViewCount(Long postId)` | `void` | `void` |
| 帖子置顶 | `PUT /admin/posts/{postId}/top` | `setPostTop(Long postId, Boolean isTop)` | `void` | `void` |

---

## 4. 评论模块 (ForumCommentService)

| 功能 | Controller方法 | Service方法 | Service返回类型 | Controller返回类型 |
|------|---------------|-------------|-----------------|-------------------|
| 发表评论 | `POST /comments` | `createComment(ForumComment comment)` | `void` | `void` |
| 评论列表 | `GET /posts/{postId}/comments` | `getCommentsByPostId(Long postId)` | `List<ForumComment>` | `ResponseEntity<List<ForumComment>>` |
| 删除评论 | `DELETE /comments/{commentId}` | `deleteComment(Long commentId)` | `void` | `void` |
| 评论点赞 | `POST /comments/{commentId}/like` | `likeComment(Long commentId)` | `void` | `void` |

---

## 5. 后台管理模块 (AdminService)

| 功能 | Controller方法 | Service方法 | Service返回类型 | Controller返回类型 |
|------|---------------|-------------|-----------------|-------------------|
| 管理员登录 | `POST /admin/login` | `login(User user)` | `User` | `ResponseEntity<User>` |
| 获取用户列表 | `GET /admin/users` | `getAllUsers()` | `List<User>` | `ResponseEntity<List<User>>` |
| 更新用户状态 | `PUT /admin/users/{userId}/status` | `updateUserStatus(Long userId, Integer status)` | `void` | `void` |
| 删除违规帖子 | `DELETE /admin/posts/{postId}` | `deletePost(Long postId)` | `void` | `void` |
| 删除违规评论 | `DELETE /admin/comments/{commentId}` | `deleteComment(Long commentId)` | `void` | `void` |

---

## 备注

1. **Service返回类型规则**：
   - 查询操作：返回实体对象或列表 `User` / `List<User>`
   - 增删改操作：返回 `void`（可通过影响行数判断）

2. **Controller返回类型规则**：
   - 查询操作：返回 `ResponseEntity<T>`（T为实体或列表）
   - 增删改操作：返回 `void`（Spring自动返回200 OK）

3. **路径命名规范**：
   - `/auth/*` - 认证相关
   - `/user/*` - 用户个人信息管理
   - `/admin/*` - 管理员功能
   - `/boards/*` - 板块管理
   - `/posts/*` - 帖子管理
   - `/comments/*` - 评论管理
