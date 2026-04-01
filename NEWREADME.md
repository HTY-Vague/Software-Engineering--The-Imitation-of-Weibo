# 极简基础论坛系统 (Simple Forum System)

> 一个面向课程设计的最小可用论坛系统（MVP），聚焦论坛核心业务闭环：
> **注册 → 登录 → 浏览板块 → 查看帖子 → 发布帖子 → 评论互动 → 管理员管理**

## 项目简介

本项目是一个极简版论坛系统，去除贴吧类复杂功能（如用户自建板块、吧主管理、关注系统等），仅保留论坛核心能力，目标是：

- 系统结构清晰，便于理解与维护
- 开发成本可控，适合课程设计周期
- 具备完整业务闭环，可演示可扩展

该项目适合作为软件工程课程设计、前后端分离实践项目和基础社区系统原型。

## 项目目标

实现一个最小可用论坛系统（MVP），具备以下能力：

- 用户注册与登录
- 板块浏览
- 帖子发布与管理
- 评论互动
- 管理员后台管理

## 系统角色与权限

### 普通用户

- 注册账号
- 登录系统
- 浏览板块
- 浏览帖子
- 发布帖子
- 编辑或删除自己的帖子
- 评论帖子
- 删除自己的评论
- 维护个人信息与密码

### 管理员

- 管理板块（新增 / 编辑 / 删除 / 启用禁用 / 排序）
- 管理用户（状态管理）
- 删除违规帖子
- 删除违规评论
- 帖子置顶
- （可选）处理举报与查看统计数据

## 核心功能模块

### 1) 用户模块

- 用户注册
- 用户登录
- JWT 身份认证
- 修改个人信息
- 修改密码
- 个人中心（我的帖子 / 我的评论）

### 2) 板块模块

- 板块列表展示
- 查看板块帖子
- 管理员新增板块
- 修改板块信息
- 启用 / 禁用板块
- 板块排序
- 删除板块

### 3) 帖子模块

- 发布帖子（富文本）
- 帖子列表
- 帖子详情
- 浏览量统计
- 编辑帖子
- 删除帖子（逻辑删除）
- 帖子搜索
- 帖子置顶（管理员）

### 4) 评论模块

- 一级评论
- 二级回复（楼中楼）
- 评论列表展示
- 删除评论
- 评论点赞

### 5) 点赞模块

- 帖子点赞
- 评论点赞
- 点赞数统计
- 查询用户点赞历史

### 6) 后台管理模块

- 管理员登录
- 用户管理（启用/禁用、删除）
- 板块管理（增删改查、排序）
- 内容管理（帖子 / 评论删除）
- 举报处理（可选）
- 数据统计（可选）

## 技术架构

系统采用前后端分离架构：

```text
前端应用（Vue3）
    ↓
REST API（Spring Boot）
    ↓
MySQL 数据库
```

### 架构说明

- 前端负责页面渲染、交互逻辑与状态管理
- 后端提供统一 RESTful API、权限认证与业务处理
- 数据层负责结构化存储（用户、板块、帖子、评论等）

## 技术栈

### 后端

- **Java**：21
- **Spring Boot**：3.3.5
- **Spring Security**：6.x（Spring Boot 内置）
- **JWT 认证**：0.11.5
- **MyBatis Plus**：3.5.7
- **数据库驱动**：MySQL Connector/J
- **密码加密**：BCrypt
- **API 文档**：Knife4j 4.4.0
- **工具库**：Hutool 5.8.27
- **参数校验**：Validation（Spring Boot 内置）
- **构建工具**：Maven 3.8+

### 前端

- Vue 3
- Vite
- Element Plus
- Axios
- Pinia
- Vue Router
- WangEditor（富文本编辑器）

### 数据库

- MySQL 8.x

## 后端架构设计

### 分层架构

```
controller/  ← Controller层（控制器）
    ↓
service/     ← Service层（业务逻辑）
    ↓
mapper/      ← Mapper层（数据访问）
    ↓
MySQL数据库   ← 数据库存储
```

### Controller 分层设计

- `AuthController.java` - 认证相关（注册、登录、退出）
- `UserController.java` - 用户个人信息管理
- `ForumBoardController.java` - 板块管理
- `ForumPostController.java` - 帖子管理
- `ForumCommentController.java` - 评论管理
- `ForumLikeController.java` - 点赞管理
- `AdminController.java` - 后台管理（用户管理、板块管理、内容管理）

### Service 接口设计

- `UserService` - 用户业务逻辑
- `ForumBoardService` - 板块业务逻辑
- `ForumPostService` - 帖子业务逻辑
- `ForumCommentService` - 评论业务逻辑
- `ForumLikeService` - 点赞业务逻辑

### Mapper 接口设计

- `UserMapper` - 用户数据访问
- `ForumBoardMapper` - 板块数据访问
- `ForumPostMapper` - 帖子数据访问
- `ForumCommentMapper` - 评论数据访问

## 数据库设计

### 核心数据表

- `sys_user`：用户表
- `forum_board`：板块表
- `forum_post`：帖子表
- `forum_comment`：评论表
- `forum_like`：点赞表（支持帖子和评论点赞）

### 点赞表结构升级

点赞表采用**通用点赞设计**，支持帖子和评论的统一点赞管理。表结构包含：

- `like_id`：点赞ID
- `target_type`：点赞目标类型（1=帖子，2=评论）
- `target_id`：目标ID（帖子ID或评论ID）
- `user_id`：点赞用户ID
- `create_time`：点赞时间

通过 `(target_type, target_id, user_id)` 唯一约束确保同一用户不重复点赞同一对象。

### 代码侧命名（驼峰）

- 实体类：`User`、`ForumBoard`、`ForumPost`、`ForumComment`、`ForumLike`
- Mapper 接口：`UserMapper`、`ForumBoardMapper`、`ForumPostMapper`、`ForumCommentMapper`、`ForumLikeMapper`
- Service 接口：`UserService`、`ForumBoardService`、`ForumPostService`、`ForumCommentService`、`ForumLikeService`
- Mapper XML：`UserMapper.xml`、`ForumBoardMapper.xml`、`ForumPostMapper.xml`、`ForumCommentMapper.xml`、`ForumLikeMapper.xml`

## API 接口设计规范

### RESTful 接口设计

| 模块 | 路径前缀 | 说明 |
|--------|-----------|------|
| 认证 | `/auth/*` | 注册、登录、退出 |
| 用户 | `/user/*` | 个人信息、我的帖子、我的评论 |
| 板块 | `/boards/*` | 板块列表、板块详情 |
| 帖子 | `/posts/*` | 帖子列表、发布、编辑、删除 |
| 评论 | `/comments/*` | 评论列表、发表评论 |
| 点赞 | `/likes/*` | 点赞操作、点赞列表、取消点赞 |
| 管理员 | `/admin/*` | 用户管理、板块管理、内容管理 |

### API 文档访问

系统集成 **Knife4j** 作为 API 文档工具，启动后可通过以下地址访问：http://122.51.87.179:8080/doc.html


Swagger 原生 UI 地址：http://122.51.87.179:8080/swagger-ui.html

### HTTP 方法规范

| HTTP 方法 | 用途 | 返回值 |
|----------|------|--------|
| GET | 查询 | 实体或列表 `T` / `List<T>` |
| POST | 创建 | 实体 `T` |
| PUT | 更新 | `void` |
| DELETE | 删除 | `void` |

### Controller 返回类型规范

| 操作类型 | Controller返回类型 | Service返回类型 | 说明 |
|---------|------------------|-----------------|------|
| 查询单个 | `ResponseEntity<T>` | `T` | 返回实体对象 |
| 查询列表 | `ResponseEntity<List<T>>` | `List<T>` | 返回实体列表 |
| 新增 | `ResponseEntity<T>` | `T` | 返回创建的实体 |
| 更新 | `void` | `void` | Spring自动返回200 OK |
| 删除 | `void` | `void` | Spring自动返回200 OK |

### Service 方法命名规范

| 操作 | Service方法名 | 示例 |
|------|----------------|------|
| 查询单个 | `get + 实体名` | `getUserById()`, `getPostById()` |
| 查询列表 | `get + 复数实体名` | `getAllUsers()`, `getAllPosts()` |
| 创建 | `create + 实体名` | `createUser()`, `createPost()` |
| 更新 | `update + 实体名` | `updateUser()`, `updatePost()` |
| 删除 | `delete + 实体名` | `deleteUser()`, `deletePost()` |
| 登录/注册 | `login()`, `register()` | `login()`, `register()` |

## 项目目录结构

```text
src/main/java/cn/edu/zut/softwareengineering/
├── controller/              ← 控制器层
│   ├── AuthController.java
│   ├── UserController.java
│   ├── ForumBoardController.java
│   ├── ForumPostController.java
│   ├── ForumCommentController.java
│   ├── ForumLikeController.java
│   └── AdminController.java
├── service/                 ← 业务逻辑层
│   ├── UserService.java
│   ├── ForumBoardService.java
│   ├── ForumPostService.java
│   ├── ForumCommentService.java
│   ├── ForumLikeService.java
│   └── Impl/             ← Service实现类
│       ├── UserServiceImpl.java
│       ├── ForumBoardServiceImpl.java
│       ├── ForumPostServiceImpl.java
│       ├── ForumCommentServiceImpl.java
│       └── ForumLikeServiceImpl.java
├── mapper/                  ← 数据访问层
│   ├── UserMapper.java
│   ├── ForumBoardMapper.java
│   ├── ForumPostMapper.java
│   ├── ForumCommentMapper.java
│   └── ForumLikeMapper.java
├── entity/                   ← 实体类
│   ├── User.java
│   ├── ForumBoard.java
│   ├── ForumPost.java
│   ├── ForumComment.java
│   └── ForumLike.java
├── config/                   ← 配置类
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   └── MyBatisConfig.java
└── utils/                    ← 工具类
    ├── JwtUtil.java
    ├── PasswordEncoderUtil.java
    └── ResultUtil.java
```

## 安全设计

- **密码加密**：BCrypt（单向哈希加密，Spring Security 默认支持）
- **身份认证**：JWT（无状态令牌认证）
- **权限控制**：Spring Security（接口级鉴权）
- **防 SQL 注入**：MyBatis Plus 参数化查询
- **XSS 防护**：富文本内容过滤与输出安全处理

## 非功能需求

### 系统规模

- 支持约 1000 用户规模

### 性能目标

- 主要接口响应时间 < 500ms（常规负载）

### 可扩展性

后续可扩展方向：

- 私信系统
- 用户等级
- 标签系统
- 多管理员协作
- 移动端适配

## 快速开始

### 环境要求

- **JDK 21**（Spring Boot 3.3.5 推荐）
- **Maven** 3.8+
- **Node.js** 18+
- **MySQL** 8.x
- **IDE**：IntelliJ IDEA 或 VS Code（Spring Boot插件）

### 云服务器部署

#### 服务器信息

- **应用服务器**：122.51.87.179
- **数据库服务器**：101.35.44.133

#### 1) 配置数据库

在 `src/main/resources/application.properties` 中配置数据库连接：

```properties
spring.datasource.url=jdbc:mysql://101.35.44.133:3306/software_engineering_forum
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
```

#### 2) 初始化数据库

在数据库服务器（101.35.44.133）上执行 `SoftwareEngineering.sql` 创建数据库和表：

```bash
mysql -u root -p < SoftwareEngineering.sql
```

或在 MySQL 客户端中直接执行脚本文件。

#### 3) 部署后端

在应用服务器（122.51.87.179）上执行：

```bash
# 方式1：使用 Maven
mvn spring-boot:run

# 方式2：构建 JAR 并运行
mvn clean package
java -jar target/Software-Engineering-0.0.1-SNAPSHOT.jar
```

后端启动成功后，访问 API 文档：
- Knife4j UI：http://122.51.87.179:8080/doc.html
- Swagger UI：http://122.51.87.179:8080/swagger-ui.html

#### 4) 部署前端（如适用）

在应用服务器（122.51.87.179）上执行：

```bash
cd forum-web
npm install
npm run build
# 将dist目录部署到Web服务器
```

## 开发周期（课程设计建议）

- **第 1 周**：项目搭建 + 用户模块
- **第 2 周**：板块模块 + 帖子模块
- **第 3 周**：评论模块 + 前端页面开发
- **第 4 周**：后台管理 + 测试部署

## 课程设计交付建议

- 需求说明书（功能与角色权限）
- 数据库设计文档（ER 图 + 表结构）
- 接口文档（可使用 Swagger / Apifox）
- 测试报告（功能测试 + 基础性能测试）
- 项目演示视频或答辩 PPT

## 相关文档

- [数据库设计.md](./数据库设计.md) - 数据库表结构详细设计
- [Service功能及修饰符对应表.md](./Service功能及修饰符对应表.md) - Service接口方法设计规范
- [forum_like_migration.sql](./forum_like_migration.sql) - 点赞表结构升级脚本（数据迁移）

## License

本项目用于学习与课程设计场景

