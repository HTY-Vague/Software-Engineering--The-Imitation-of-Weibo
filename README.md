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

- 管理板块（新增 / 编辑 / 启用禁用 / 排序）
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
- 评论点赞（可选）

### 5) 后台管理模块

- 管理员登录
- 用户管理
- 板块管理
- 内容管理（帖子 / 评论）
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

### 前端

- Vue 3
- Vite
- Element Plus
- Axios
- Pinia
- Vue Router
- WangEditor（富文本编辑器）

### 后端

- Spring Boot 3
- Spring Security
- JWT 认证
- MyBatis Plus

### 数据库

- MySQL 8

### 可选组件

- Redis（缓存 / 热点数据）
- Docker（容器化部署）
- OSS 对象存储（图片等静态资源）

## 系统架构图（逻辑）

```text
用户浏览器
    ↓
Vue 前端应用
    ↓
REST API
    ↓
Spring Boot 后端服务
    ↓
MySQL 数据库
```

## 数据库设计

### 核心数据表

- `sys_user`：用户表
- `forum_board`：板块表
- `forum_post`：帖子表
- `forum_comment`：评论表
- `forum_like`：点赞表（可选）

### 代码侧命名（驼峰）

- 实体类：`User`、`ForumBoard`、`ForumPost`、`ForumComment`、`ForumLike`
- Mapper XML：`UserMapper.xml`、`ForumBoardMapper.xml`、`ForumPostMapper.xml`、`ForumCommentMapper.xml`、`ForumLikeMapper.xml`

### 表结构字段（核心）

#### 1) 用户表 `sys_user`

| 字段名 | 说明 |
| --- | --- |
| user_id | 用户 ID（主键） |
| username | 登录账号（唯一） |
| password | 密码（加密存储） |
| nickname | 昵称 |
| avatar | 头像地址 |
| signature | 个性签名 |
| role | 角色（USER / ADMIN） |
| status | 状态（启用 / 禁用） |
| create_time | 创建时间 |
| update_time | 更新时间 |

#### 2) 板块表 `forum_board`

| 字段名 | 说明 |
| --- | --- |
| board_id | 板块 ID（主键） |
| board_name | 板块名称 |
| description | 板块描述 |
| sort_num | 排序值 |
| status | 状态（启用 / 禁用） |
| create_time | 创建时间 |
| update_time | 更新时间 |

#### 3) 帖子表 `forum_post`

| 字段名 | 说明 |
| --- | --- |
| post_id | 帖子 ID（主键） |
| board_id | 所属板块 ID |
| author_id | 作者用户 ID |
| title | 帖子标题 |
| content | 帖子内容（富文本） |
| view_count | 浏览量 |
| like_count | 点赞数 |
| comment_count | 评论数 |
| is_top | 是否置顶 |
| status | 状态（正常 / 删除） |
| create_time | 创建时间 |
| update_time | 更新时间 |

#### 4) 评论表 `forum_comment`

| 字段名 | 说明 |
| --- | --- |
| comment_id | 评论 ID（主键） |
| post_id | 所属帖子 ID |
| user_id | 评论用户 ID |
| parent_comment_id | 父评论 ID（一级评论为 0 或 NULL） |
| reply_user_id | 被回复用户 ID |
| content | 评论内容 |
| like_count | 点赞数 |
| status | 状态（正常 / 删除） |
| create_time | 创建时间 |
| update_time | 更新时间 |

## 核心业务流程

### 用户发帖流程

```text
用户登录
→ 选择板块
→ 发布帖子
→ 帖子写入数据库
→ 帖子列表展示
```

### 评论流程

```text
用户发表评论
→ 创建评论记录
→ 更新帖子评论数
→ 评论列表刷新
```

## 安全设计

- **密码加密**：BCrypt
- **身份认证**：JWT
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

## 项目目录结构（示例）

```text
forum-server/
├─ controller/
├─ service/
├─ mapper/
├─ entity/
├─ config/
└─ utils/

forum-web/
├─ views/
├─ components/
├─ router/
├─ store/
└─ api/
```

## 快速开始

### 环境要求

- JDK 17+（建议与 Spring Boot 3 匹配）
- Maven 3.8+
- Node.js 18+
- MySQL 8

### 本地运行

#### 1) 启动后端

```bash
cd forum-server
mvn spring-boot:run
```

#### 2) 启动前端

```bash
cd forum-web
npm install
npm run dev
```

### Docker 部署

```bash
docker-compose up -d
```

> 如启用 Docker，请确保已正确配置数据库连接、JWT 密钥及端口映射。

## 开发周期（课程设计建议）

- **第 1 周**：项目搭建 + 用户模块
- **第 2 周**：板块模块 + 帖子模块
- **第 3 周**：前端页面开发
- **第 4 周**：后台管理 + 测试部署

## 课程设计交付建议

- 需求说明书（功能与角色权限）
- 数据库设计文档（ER 图 + 表结构）
- 接口文档（可使用 Swagger / Apifox）
- 测试报告（功能测试 + 基础性能测试）
- 项目演示视频或答辩 PPT

## License

本项目用于学习与课程设计场景，可根据实际需要补充开源许可证（如 MIT）。

---

如果你需要，我也可以继续为这个项目补充：

- `接口文档模板（REST API 清单）`
- `数据库初始化 SQL`
- `课程设计答辩版功能清单与分工说明`
