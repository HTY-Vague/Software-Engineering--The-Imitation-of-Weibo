# 极简基础论坛系统 (Simple Forum System)

> 一个面向课程设计的最小可用论坛系统（MVP），聚焦论坛核心业务闭环：
> **注册 → 登录 → 浏览板块 → 查看帖子 → 发布帖子 → 评论互动 → 管理员管理**

## 项目简介

本项目是一个极简版论坛系统，去除贴吧类复杂功能（如用户自建板块、吧主管理、关注系统等），仅保留论坛核心能力，目标是：

- 系统结构清晰，便于理解与维护
- 开发成本可控，适合课程设计周期
- 具备完整业务闭环，可演示可扩展

系统由三部分组成，共享同一套 REST 接口与 JWT 鉴权体系：

| 组成 | 技术形态 | 说明 |
|------|----------|------|
| 后端服务 | Spring Boot 单体应用 | 唯一的数据与业务权威 |
| Web 前端 | Vue 3 单页应用 | 桌面浏览器访问，功能完整 |
| 移动客户端 | HarmonyOS ArkTS/ArkUI | 手机端，覆盖核心业务与管理端 |

该项目适合作为软件工程课程设计、前后端分离实践项目和基础社区系统原型。

## 核心功能

### 用户与认证

- 用户注册、登录、退出
- JWT 无状态身份认证，BCrypt 密码加密
- 修改个人信息、修改密码
- 个人中心：我的帖子、我的评论

### 板块

- 板块列表展示与板块内帖子浏览
- 管理员新增、编辑、删除板块
- 板块启用/禁用与排序

### 帖子

- 发布帖子（Web 端富文本，移动端纯文本）
- 帖子列表、详情、浏览量统计
- 标题与内容搜索
- 编辑、删除自己的帖子（逻辑删除）
- 管理员置顶帖子

### 评论

- 一级评论与二级回复（楼中楼）
- 评论列表树形展示
- 删除自己的评论，管理员删除违规评论

### 点赞

- 帖子点赞与评论点赞
- 点赞、取消点赞、点赞状态查询
- 点赞数统计

### 后台管理

- 用户管理：列表、启用/禁用
- 板块管理：增删改、启停、排序
- 内容管理：帖子置顶、帖子与评论删除
- 数据统计：概览指标、热门帖子、板块活跃度、用户活跃度、内容质量、每日趋势（由 Spark 离线跑批生成）

### 系统角色

| 角色 | 权限范围 |
|------|----------|
| 普通用户 | 注册登录、浏览板块与帖子、发布/编辑/删除自己的帖子、评论与删除自己的评论、维护个人信息 |
| 管理员 | 在普通用户权限基础上，管理板块与用户状态、置顶帖子、删除任意帖子与评论、查看统计 |

## 技术栈

| 层次 | 主要技术 |
|------|----------|
| 后端 | Java 21、Spring Boot 3.3.5、Spring Security、JWT、MyBatis Plus 3.5.7、Knife4j |
| Web 前端 | Vue 3、Vite、Element Plus、Pinia、Vue Router、WangEditor、ECharts |
| 移动客户端 | ArkTS / ArkUI、Stage 模型、HarmonyOS 26.0.0（API 26） |
| 数据库 | MySQL 8.x |
| 离线分析 | Spark 4.1.2（独立进程，每日跑批写统计表） |

架构分层、数据模型与接口规范的详细说明见 [文档/技术设计.md](./文档/技术设计.md)。

## 安装步骤

### 环境要求

- **JDK 21**
- **Maven** 3.8+
- **Node.js** 18+
- **MySQL** 8.x
- **IDE**：IntelliJ IDEA 或 VS Code（Spring Boot 插件）

移动客户端另需 DevEco Studio 26.0 及以上与 API 26 SDK，非必需。

### 1. 获取代码

```bash
git clone https://github.com/HTY-Vague/Software-Engineering--The-Imitation-of-Weibo.git
cd Software-Engineering--The-Imitation-of-Weibo
```

### 2. 配置环境变量

项目由环境变量驱动配置，避免把账号、密码与密钥写入仓库。参照 `.env.example` 准备本地配置：

```properties
DB_URL=jdbc:mysql://localhost:3306/software_engineering_forum?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
DB_USERNAME=root
DB_PASSWORD=your_password
JWT_SECRET=replace_with_at_least_32_characters_secret
JWT_EXPIRATION=86400000
SERVER_PORT=8080
```

### 3. 初始化数据库

执行建库建表脚本，按需导入演示数据：

```bash
mysql -u root -p < SoftwareEngineering.sql
mysql -u root -p < demo_data.sql
```

### 4. 构建并启动后端

```bash
mvn clean package
java -jar target/Software-Engineering-0.0.1-SNAPSHOT.jar
```

Windows 环境下也可双击 `run-app.bat`，脚本会用系统 Java 启动已打包的 jar。

启动后后端服务监听 `http://localhost:8080`。

### 5. 启动 Web 前端（开发模式）

接口基址通过 `VITE_API_BASE_URL` 配置，未设置时为同源请求。本地开发时在 `frontend/.env.local` 写入后端地址（后端已开启 CORS）：

```properties
VITE_API_BASE_URL=http://localhost:8080
```

随后启动开发服务器：

```bash
cd frontend
npm install
npm run dev
```

### 6. 一体化部署（可选）

生产环境采用前后端一体打包：`npm run build` 生成 `frontend/dist`，将其内容复制到 `src/main/resources/static/` 后重新打包后端。启动后直接访问 `http://localhost:8080/` 即可打开前端页面，SPA 路由由后端兜底转发。

### 7. 构建移动客户端（可选）

用 DevEco Studio 打开 `Harmony-Version/` 目录，等待 hvigor 同步完成后运行 `entry` 模块。首次运行需在 `File > Project Structure > Signing Configs` 勾选自动生成调试签名。

命令行构建时先把 DevEco Studio 的 `tools/hvigor/bin` 加入 PATH，再执行：

```bash
cd Harmony-Version
hvigorw assembleHap --mode module -p product=default -p buildMode=debug
```

打包签名阶段要求 `java` 在 PATH 中。未配置签名时产出 `entry-default-unsigned.hap`，可构建但无法安装。

客户端启动后需在「服务地址」页填写后端地址（模拟器访问宿主机用 `http://10.0.2.2:8080`，真机用局域网或公网地址）。详细说明见 [Harmony-Version/README.md](./Harmony-Version/README.md)。

## 使用方法

### 演示账号

导入 `demo_data.sql` 后可使用以下账号：

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `Admin_123` |
| 普通用户 | `demo_user` | `User_123` |

### 典型操作路径

普通用户：

1. 首页浏览板块与帖子，支持按板块筛选与关键词搜索
2. 注册或登录后进入发帖页，选择板块并提交标题与正文
3. 进入帖子详情，发表评论或对评论进行楼中楼回复
4. 在个人中心维护资料、修改密码、管理自己的帖子与评论

管理员：

1. 使用 `admin` 账号登录，导航栏出现「管理后台」入口
2. 在用户管理中启用/禁用账号
3. 在板块管理中新增板块、调整排序、启用或禁用
4. 在帖子管理中置顶或删除帖子，在评论管理中删除违规评论

### API 文档

后端集成 Knife4j，启动后可直接调试全部接口：

- Knife4j UI：http://localhost:8080/doc.html
- Swagger UI：http://localhost:8080/swagger-ui.html

完整接口清单与联调说明见 [API接口清单.md](./API接口清单.md)。

## 相关文档

| 文档 | 内容 |
|------|------|
| [文档/需求分析.md](./文档/需求分析.md) | 需求分析与系统设计 |
| [文档/技术设计.md](./文档/技术设计.md) | 架构分层、数据模型、接口规范、目录结构 |
| [文档/数据库设计.md](./文档/数据库设计.md) | ER 设计与建表说明 |
| [文档/项目实验报告.md](./文档/项目实验报告.md) | 课程实验报告（部署、测试与实现分析） |
| [API接口清单.md](./API接口清单.md) | 后端接口清单与联调说明 |
| [PROGRESS.md](./PROGRESS.md) | 开发进度、待办与风险记录 |
| [Harmony-Version/README.md](./Harmony-Version/README.md) | HarmonyOS 客户端运行说明 |
| [验收材料/](./验收材料/) | 数据库脚本、CSV 数据、大数据脚本 |

## License

本项目用于学习与课程设计场景
