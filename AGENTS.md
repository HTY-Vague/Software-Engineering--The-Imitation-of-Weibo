# AGENTS.md

本文件是给后续开发者和 AI Agent 使用的项目工作手册。进入本仓库后，优先阅读本文，再根据任务继续查看 `README.md`、`API接口清单.md` 和 `后端文档/` 下的专题文档。

## 项目概览

本项目是一个面向课程设计和前后端分离实践的极简论坛/微博仿制系统，目标是完成基础社区业务闭环：

```text
注册 -> 登录 -> 浏览板块 -> 查看帖子 -> 发布帖子 -> 评论互动 -> 点赞 -> 管理员管理
```

当前仓库主要是后端 Spring Boot 工程。系统保留用户、板块、帖子、评论、点赞、后台管理和统计等核心能力，不包含关注、私信、复杂推荐、用户自建板块等高复杂度功能。

主要角色：

- 普通用户：注册、登录、浏览板块和帖子、发帖、编辑/删除自己的帖子、评论、删除自己的评论、点赞、维护个人资料和密码。
- 管理员：管理用户状态、管理板块、删除违规帖子和评论、帖子置顶、查看统计概览和热门帖子。

## 技术栈与环境

后端技术栈：

- Java 21
- Spring Boot 3.3.5
- Spring Web
- Spring Security 6.x
- JWT 0.11.5
- MyBatis Plus 3.5.7 + MyBatis XML
- MySQL Connector/J
- BCrypt 密码加密
- Knife4j 4.4.0 / OpenAPI
- Hutool 5.8.27
- Maven
- JUnit 5、Mockito、Spring Boot Test、MockMvc

规划中的前端技术栈：

- Vue 3
- Vite
- Element Plus
- Axios
- Pinia
- Vue Router
- WangEditor

环境要求：

- JDK 21
- Maven 3.8+ 或 Maven Wrapper
- MySQL 8.x
- Node.js 18+（前端开发时需要）

## 目录说明

根目录重点文件：

- `README.md`：项目总览、功能模块、技术栈、启动方式和开发状态。
- `API接口清单.md`：当前后端接口路径、权限要求、请求示例和响应格式。
- `SoftwareEngineering.sql`：建库建表脚本。
- `demo_data.sql`：演示数据脚本，包含管理员和普通用户演示账号。
- `.env.example`：环境变量配置示例。不要把真实数据库密码、JWT 密钥或服务器凭据写入仓库。
- `pom.xml`：Maven 项目配置和依赖版本。
- `后端文档/`：需求分析、数据库设计、开发实施方案、部署说明、测试报告等交付文档。

后端源码结构：

```text
src/main/java/cn/edu/zut/softwareengineering/
├── config/        Spring Security、JWT、MyBatis、OpenAPI 配置
├── controller/    REST Controller 和全局异常处理
├── entity/        数据库实体
├── mapper/        MyBatis Mapper 接口
├── service/       Service 接口
├── service/Impl/  Service 实现
└── utils/         JWT、密码、统一响应、安全上下文工具
```

资源与测试：

- `src/main/resources/application.properties`：应用配置，运行时应优先通过环境变量覆盖。
- `src/main/resources/mapper/*.xml`：Mapper SQL，项目约定 SQL 写在 XML 中。
- `src/test/java/**`：自动化测试，包括 Service、Controller、安全、OpenAPI 和数据库联调入口。
- `target/`：Maven 构建产物，不应手动维护，也不应作为源码依据。
- `spark-stats/`：Spark 离线分析独立工程（独立 pom + Java 21），编译成 jar 在服务器上通过 `spark-submit` 跑批写统计表，不依赖主工程运行时。
- `spark_stats.sql` / `spark_demo_data.sql`：统计表建表脚本和演示数据脚本。

## 常用命令

运行常规测试：

```powershell
mvn test
```

构建 JAR：

```powershell
mvn clean package
```

启动打包后的后端服务：

```powershell
java -jar target/Software-Engineering-0.0.1-SNAPSHOT.jar
```

初始化数据库：

```bash
mysql -u root -p < SoftwareEngineering.sql
mysql -u root -p < demo_data.sql
```

真实 MySQL 联调测试需要显式开启，示例：

```powershell
$env:RUN_DB_TESTS="true"
$env:DB_URL="jdbc:mysql://服务器IP:3306/software_engineering_forum?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="数据库用户名"
$env:DB_PASSWORD="数据库密码"
mvn -Dtest=DatabaseIntegrationIT test
```

本地启动后 API 文档地址：

- Knife4j UI: `http://localhost:8080/doc.html`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## 核心模块状态

截至当前文档整理时，项目状态如下：

| 模块 | 当前状态 | 说明 |
|------|----------|------|
| 项目搭建 | 完成 | Spring Boot + MyBatis Plus + MySQL + Spring Security 基线已建立 |
| 用户认证 | 完成 | 注册、登录、JWT 鉴权、BCrypt 加密已实现 |
| 用户管理 | 基本完成 | 个人资料、密码、我的帖子、我的评论已接入当前登录用户 |
| 板块管理 | 完成 | 板块浏览、CRUD、排序、启用/禁用已具备 |
| 帖子管理 | 完成 | 发布、列表、详情、编辑、删除、置顶、搜索已具备 |
| 评论管理 | 完成 | 一级评论、二级回复、树形展示、删除、计数同步已具备 |
| 点赞管理 | 完成 | 帖子/评论通用点赞、取消点赞、状态查询、计数同步已具备 |
| 后台管理 | 完成 | 用户、板块、帖子、评论管理，统计概览和热门帖子已具备 |
| Spark 分析 | 完成 | 独立进程 `spark-stats` 每日跑批写 6 张统计表，统计接口改读统计表展示 |
| 自动化测试 | 完成 | 常规回归记录为 65 个测试通过，失败 0、错误 0、跳过 0 |
| 数据库联调 | 已有入口 | `DatabaseIntegrationIT` 可验证真实 MySQL 和核心 Mapper SQL |
| Spark 跑批 | 已部署验证 | 服务器上 Spark 4.1.2 + Java 21 跑批通过，cron 每日 03:00 |
| 文档 | 基本完成 | README、API 清单、部署说明、测试报告、数据库设计等已整理 |
| 前端应用 | 待开发 | README 中规划为 Vue 3 + Element Plus，目前仓库以后端为主 |

## API 与数据库

接口前缀约定：

| 模块 | 路径前缀 | 权限说明 |
|------|----------|----------|
| 认证 | `/auth/*` | 公开访问 |
| 用户 | `/user/*` | USER/ADMIN |
| 板块 | `/forum/*` | USER/ADMIN |
| 帖子 | `/posts/*` | USER/ADMIN |
| 评论 | `/comments/*` | USER/ADMIN |
| 点赞 | `/likes/*` | USER/ADMIN |
| 管理员 | `/admin/*` | 仅 ADMIN |

统一响应格式为 `ResultUtil.Result<T>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

核心数据表：

- `sys_user`：用户表
- `forum_board`：板块表
- `forum_post`：帖子表
- `forum_comment`：评论表
- `forum_like`：点赞表，使用 `target_type + target_id + user_id` 的通用点赞模型
- `stat_*`（6 张）：Spark 跑批写入的统计表（`stat_overview`/`stat_hot_post`/`stat_board_active`/`stat_active_user`/`stat_content_quality`/`stat_daily_trend`），由 `StatMapper` 只读，写入由独立 Spark 进程负责

演示数据：

- 导入 `demo_data.sql` 后可使用演示账号。
- 管理员：`admin` / `Admin_123`
- 普通用户：`demo_user` / `User_123`
- Spark 分析演示数据：`spark_demo_data.sql`，包含 3 板块 / 8 帖子 / 75 评论 / 23 点赞，用于验证跑批结果。

## 开发约定

通用规则：

- 修改代码前先读相关 Controller、Service、Mapper XML、测试和文档，避免只改单点。
- 不提交真实数据库密码、JWT 密钥、服务器 IP 的敏感凭据。运行配置应使用环境变量或本地未提交配置。
- 保持现有四层结构：`Controller -> Service -> Mapper -> MySQL`。
- 不随意新增 DTO/VO/BO/DAO/Repository 等层级，除非任务明确要求并同步调整文档和测试。
- Controller 负责接收参数、调用 Service、返回 `ResultUtil.Result<T>`。
- Service 负责业务规则、权限判断、事务控制和多 Mapper 编排。
- Mapper 接口不写注解 SQL，SQL 统一维护在 `src/main/resources/mapper/*.xml`。
- 涉及多表写入、计数同步、删除清理的 Service 方法应使用事务。
- 普通用户只能修改或删除自己的内容；管理员能力必须走 ADMIN 权限校验。
- 不信任请求体中的 `userId`，当前用户应从 Spring Security 上下文或 `SecurityUtil` 获取。
- 修改接口路径、请求字段、响应字段或权限规则时，同步更新 `API接口清单.md` 和相关测试。
- 修改数据库字段、索引或表结构时，同步更新 SQL 脚本和数据库设计文档。

测试规则：

- 普通代码改动至少运行相关测试类。
- 影响公共接口、权限、事务或 Mapper SQL 时，优先运行 `mvn test`。
- 真实数据库验证使用 `DatabaseIntegrationIT`，必须通过环境变量显式配置账号和 `RUN_DB_TESTS=true`。
- 不把 `target/` 下的编译产物当作源码编辑。

文档规则：

- README 负责总览和快速开始。
- `API接口清单.md` 负责前后端联调细节。
- `后端文档/后端测试报告.md` 负责测试范围、结果和遗留风险。
- `后端文档/后端部署说明.md` 负责部署步骤和运行环境说明。
- 本文件负责给后续 Agent 的工作入口、项目状态和协作约束。

## 遗留事项

- 前端 Vue 3 + Element Plus 应用仍待开发。
- Knife4j UI 需要在实际部署启动后人工访问验证。
- 当前测试未覆盖高并发点赞、批量数据、接口性能和长期运行稳定性。
- 真实部署环境必须使用环境变量覆盖数据库连接和 JWT 配置，不应依赖仓库中的示例值。
- 如继续推进课程设计交付，应补充最终验收材料、演示流程、答辩 PPT 或演示视频。

## 推荐工作流

1. 阅读 `AGENTS.md`、`README.md` 和任务相关文档。
2. 用 `git status --short` 确认工作区状态，避免覆盖他人改动。
3. 定位相关 Controller、Service、Mapper XML 和测试。
4. 小范围修改，保持现有风格。
5. 运行相关测试或 `mvn test`。
6. 同步更新接口、数据库、测试或部署文档。
7. 最终说明改动内容、验证结果和未验证风险。
