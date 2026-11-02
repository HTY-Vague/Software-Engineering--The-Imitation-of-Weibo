# 简坛（论坛系统）HarmonyOS 迁移实现方案

- 文档版本：v1.0（规划稿，未开工）
- 编写日期：2026-10-26
- 文档定位：本项目由 Vue 3 Web 前端 + Spring Boot 后端构成。本文档规划两件事：其一，移除项目中的数据分析（Spark 统计）部分；其二，新增一个 HarmonyOS 手机客户端，复用现有后端 REST API。原有 README、API 接口清单、需求分析、实验报告等文档一律不改动，所有差异与废弃说明均记录在本文档中。

---

## 1. 已确认的决策

| 编号 | 决策项 | 结论 |
|---|---|---|
| D1 | 统计概览处理方式 | 保留，改为对业务表实时 COUNT 聚合，不再依赖 Spark 统计表 |
| D2 | 目标设备与系统基线 | 仅手机；HarmonyOS NEXT，API 12+ |
| D3 | 交付形态 | 本地应用（自行签名 / 调试签名 HAP），不发行、不上架，仅用于课程作业演示 |
| D4 | Web 前端 | 保留并继续可运行，与 HarmonyOS 客户端并行；差异记录于本文档 |
| D5 | 后端形态 | 不做架构变更，仍为 Spring Boot 单体 REST 服务，继续部署在现有云服务器 |

## 2. 总体改造路径

改造分为三条并行且有序的工作流：

```
A. 后端瘦身（P0）     删除 Spark 统计链路，统计概览改实时 COUNT，补管理端帖子列表数据源
B. Web 前端瘦身（P0） 删除统计页面与服务，修复受影响页面，保证 Web 端功能不缺失
C. HarmonyOS 客户端（P1-P3） 新建 ArkTS/ArkUI 工程，重写表现层，复用 REST 契约
```

改造完成后的目标形态：

```
HarmonyOS 手机端（ArkTS/ArkUI，新工程）  ──┐
                                            ├── HTTP/JSON ──> Spring Boot 后端（瘦身版） ──> MySQL（业务表）
Vue 3 Web 前端（保留，并行维护）         ──┘
```

后端是唯一数据与业务权威，两端客户端共享同一份 API 契约与 JWT 鉴权体系。

---

## 3. 数据分析部分移除方案（P0）

### 3.1 受影响范围梳理

统计链路由以下部分构成，均为纯只读旁路，核心业务（发帖、评论、点赞、计数）不依赖它们：

| 层 | 文件 | 处理 |
|---|---|---|
| Controller | `AdminController` 中 `/admin/statistics/*` 全部 8 个端点及 `StatisticsService`、`SparkStatsService` 注入 | 删除端点与注入；保留概览端点（改造，见 3.2） |
| Service | `StatisticsService` / `StatisticsServiceImpl` | 精简为仅含 `getOverview`，实现改为实时 COUNT |
| Service | `SparkStatsService` / `SparkStatsServiceImpl` | 整体删除 |
| Mapper | `StatMapper` / `StatMapper.xml` | 整体删除 |
| Entity | `StatOverview`、`StatHotPost`、`StatBoardActive`、`StatActiveUser`、`StatContentQuality`、`StatDailyTrend` 共 6 个实体 | 整体删除（overview 改用 Map 返回，无需实体） |
| 配置 | `application.properties` 中 `spark.*` 全部配置项 | 删除 |
| 测试 | `StatisticsServiceImplTest` | 删除；为改造后的概览接口补一个新测试 |
| 前端服务 | `services/modules/statistics.js` 及 `services/index.js` 中的导出 | 删除 |
| 前端页面 | `pages/admin/StatisticsPage.vue`、`router/index.js` 中 `/admin/statistics` 路由 | 删除 |
| 前端依赖 | `package.json` 中 `echarts` | 移除（全项目仅统计页使用） |
| 独立工程 | `spark-stats/` 目录（跑批工程、jar、数据生成脚本） | 从工作区移除（建议归档备份后再删） |
| SQL 脚本 | `spark_stats.sql`、`spark_demo_data.sql` | 从工作区移除（归档备份） |
| 验收材料 | Spark 相关脚本与统计 CSV | 保留不删（历史交付物），仅在本文档记录废弃 |
| 数据库 | `stat_*` 六张统计表 | 保留不动（只读无害），不做 DROP，避免误删风险；后续如需清理另行确认 |

`pom.xml` 无需改动：后端本身不依赖 Spark，跑批是服务器上的独立进程。

### 3.2 统计概览改造（保留功能）

现状：`GET /admin/statistics/overview` 读取 Spark 写入的 `stat_overview` 表。

改造方案：

- 保留端点路径与响应结构不变（字段：`userCount`、`activeUserCount`、`enabledBoardCount`、`postCount`、`commentCount`、`likeCount`、`updateTime`），两端客户端零适配成本。
- `StatisticsServiceImpl` 改为注入五个业务 Mapper，实时聚合：
  - `userCount`：用户总数；
  - `activeUserCount`：状态正常（未禁用）的用户数（原语义来自统计表，此处口径弱化为"正常状态用户数"，在本文档记录该差异）；
  - `enabledBoardCount`：启用状态板块数（需带状态条件计数）；
  - `postCount` / `commentCount` / `likeCount`：对应业务表有效记录总数；
  - `updateTime`：改为返回当前时间，表示"实时统计"。
- 计数均走数据库 COUNT 聚合，数据量为课程演示规模（千级），性能无压力。

### 3.3 隐藏依赖修复：管理后台帖子页

梳理时发现一处非显性依赖：`pages/admin/PostsPage.vue` 的帖子列表数据源是 `getHotPosts({ limit: 50 })`（Spark 热门帖子接口），并非业务接口。删除统计后该页会直接报错。

修复方案（推荐前者）：

1. 改用现有业务接口 `POST /posts/list`（已支持 `boardId` + `keyword` + 分页），管理页的板块筛选与搜索能力可完整保留，改动最小；
2. 或新增 `GET /admin/posts` 管理端专用列表接口（仅在方案 1 不满足时采用，如需返回已删除帖子）。

### 3.4 前端受影响入口修复

- `components/layout/NavHeader.vue` 中"管理后台"链接当前指向 `/admin/statistics`，删除统计页后需改指 `/admin/users`，避免 404。
- `services/modules/admin.js` 中被注释的旧统计代码一并清理。

### 3.5 移除后的功能完整性保障

- 删除统计属于移除整条旁路链路，不触及业务写路径，无数据流断裂；
- 前端全量搜索 `statistics` / `echarts` 引用确认无其他页面依赖（已核实：仅 `StatisticsPage.vue` 与 `PostsPage.vue` 两处，后者按 3.3 处理）；
- 后端执行全量测试（除删除的统计测试外全部通过）；Web 端执行构建并手工回归管理后台四个页面与核心业务闭环。

---

## 4. HarmonyOS 工程设计（P1-P3）

### 4.1 工程结构

使用 DevEco Studio 新建 Stage 模型工程，独立于现有 Maven / Vite 工程，互不干扰：

```
JianTan/                              # 根工程（建议置于仓库顶层 JianTan/ 目录）
├── AppScope/
│   └── app.json5                     # 应用名称"简坛"、图标、版本号
├── entry/                            # 主模块（单模块即可，规模不大）
│   ├── src/main/
│   │   ├── ets/
│   │   │   ├── entryability/
│   │   │   │   └── EntryAbility.ets  # 唯一 Ability，承载窗口与页面栈
│   │   │   ├── pages/                # 页面（@Entry 组件）
│   │   │   │   ├── Index.ets         # 首页：帖子流 + 板块入口
│   │   │   │   ├── LoginPage.ets
│   │   │   │   ├── RegisterPage.ets
│   │   │   │   ├── PostCreatePage.ets
│   │   │   │   ├── PostDetailPage.ets
│   │   │   │   ├── PostEditPage.ets
│   │   │   │   ├── BoardPostsPage.ets
│   │   │   │   ├── MeProfilePage.ets / MePostsPage.ets / MeCommentsPage.ets
│   │   │   │   └── admin/ UsersPage / ForumsPage / PostsPage / CommentsPage
│   │   │   ├── components/           # 公共组件：NavHeader、PostCard、CommentItem、LikeButton
│   │   │   ├── api/                  # 网络层（见 4.4）
│   │   │   ├── model/                # 与后端实体对应的 interface 类型定义（先行编写）
│   │   │   └── store/                # 会话与状态封装（基于 AppStorage）
│   │   ├── resources/base/           # media（图标图片）、element（颜色/字符串）
│   │   └── module.json5              # 权限：ohos.permission.INTERNET；页面注册
│   └── build-profile.json5
├── build-profile.json5               # 工程签名配置（调试签名自动生成）
└── hvigor 配置文件
```

### 4.2 概念映射与复用判定

| 现有前端概念 | HarmonyOS 对应物 | 判定 |
|---|---|---|
| `pages/*.vue` 页面 | `@Entry @Component` 页面组件 | 重写（结构可对照翻译） |
| `components/*.vue` 组件 | `@Component` 自定义组件 | 重写 |
| `router/index.js` 路由与守卫 | 单 EntryAbility + `Navigation` / `NavDestination` 页面栈 | 重写（逻辑等价迁移） |
| `services/http.js` 拦截封装 | `api/HttpClient.ets` 统一封装 | 重写（契约不变） |
| `services/modules/*.js` | `api/` 下按模块拆分的接口文件 | 重写（路径与参数逐条对照 API 清单） |
| `stores/`（Pinia） | `AppStorage` + `PersistentStorage` | 重写 |
| token 存 localStorage | `@ohos.data.preferences` | 替换 |
| Element Plus 组件库 | ArkUI 内置组件 | 等价替换 |
| WangEditor 富文本 | 一期降级纯文本 `TextArea`，展示用 `RichText` | 部分降级（见 4.6） |
| ECharts 图表 | 无（统计已删） | 不涉及 |
| 后端全部接口与鉴权 | 原样复用 | 直接复用 |

### 4.3 Ability 与路由设计

- 仅一个 `EntryAbility`，窗口加载 `Index` 页，页面间跳转全部走 `Navigation` 组件栈；
- 不引入 ExtensionAbility（无桌面卡片、后台服务诉求；后续如需要再增 `FormExtensionAbility`）；
- 路由守卫等价实现：将 Vue 路由的 `requiresAuth` / `requiresAdmin` 元信息翻译为跳转前的会话检查——未登录跳登录页并携带返回路径，非管理员访问管理页回首页；
- 页面映射（16 条路由 → NavDestination）：登录、注册、首页、帖子详情、发帖、编辑、板块列表、个人中心三页、管理后台四页，另加 404 兜底并入首页提示。

### 4.4 网络层设计

- 基于 `@ohos.net.http` 封装单一 `HttpClient`：`BASE_URL` 常量（指向云服务器或局域网后端地址，课程演示可在设置页或构建配置中切换）、默认超时、请求头注入 `Authorization: Bearer <token>`；
- 统一响应解包：`Result{code, message, data}`，`code === 200` 返回 `data`，否则抛出携带 `message` 的业务异常；HTTP 401 / 403 时清空会话并跳转登录页；
- 注意传参形态：后端多数接口为 `POST + @RequestParam`（表单/查询参数）而非 JSON body，`api/` 各文件须逐条对照《API接口清单.md》确认传参方式，这是适配期最容易出错的位置；
- CORS 配置对 App 端无效（非浏览器），后端现有 CORS 保留不动。

### 4.5 本地存储与权限

- `@ohos.data.preferences` 存储 `token`、`userId`、`role`、昵称等会话信息，启动 `EntryAbility` 时恢复（等价 Web 端 `hydrate` 逻辑）；
- `module.json5` 申请 `ohos.permission.INTERNET`；
- 明文 HTTP 放行：后端暂为 `http://IP:8080`，HarmonyOS 默认仅允许 HTTPS，需在模块网络配置中允许明文流量（仅限本地课程演示，不发行）。

### 4.6 富文本降级策略（主要体验折损点）

- 编辑：一期用 `TextArea` 纯文本输入，帖子标题+正文纯文本提交，后端字段不变（仍存 HTML 或纯文本，纯文本兼容）；
- 展示：帖子详情页用 ArkUI `RichText` 组件渲染既有 HTML 内容子集，旧数据（含格式的帖子）可显示，复杂样式可能丢失；
- 二期视需要再评估引入第三方富文本方案或简化 Markdown 语法。

### 4.7 本地应用构建与安装（不发行）

1. DevEco Studio 打开工程，使用自动生成的调试证书与 Profile（本地签名，无需上架签名）；
2. `Build > Build HAP(s)/APP(s)` 或命令行 `hvigorw assembleHap` 产出 debug HAP；
3. 真机开启开发者模式 + USB 调试，IDE 直接 Run 安装，或 `hdc install entry-default-signed.hap`；
4. 演示时手机需能访问后端：同一局域网（后端跑在本机/实验室机器）或直接访问云服务器公网地址。

---

## 5. 分阶段实施计划与验证

### P0：后端与 Web 前端瘦身（先行，与 HarmonyOS 无耦合）

| 步骤 | 内容 | 验证方式 |
|---|---|---|
| P0-1 | 删除后端统计链路文件、AdminController 统计端点与 spark 配置 | `mvn compile` 通过 |
| P0-2 | 概览接口改实时 COUNT，补 `getOverview` 新测试 | `mvn test` 全绿；Knife4j 验证概览返回结构与改造前一致 |
| P0-3 | Web 端删 statistics 服务/页面/路由/echarts，修复 NavHeader 入口 | `npm run build` 通过 |
| P0-4 | 管理后台帖子页改用 `POST /posts/list` | 手工回归：板块筛选、搜索、置顶、删除 |
| P0-5 | 归档并移除 `spark-stats/`、`spark_stats.sql`、`spark_demo_data.sql` | 目录确认；Web 全功能手工回归（登录→浏览→发帖→评论→点赞→管理） |

### P1：HarmonyOS 工程骨架

| 步骤 | 内容 | 验证方式 |
|---|---|---|
| P1-1 | 新建 Stage 工程跑通空壳（EntryAbility + Index + INTERNET 权限） | 模拟器运行 |
| P1-2 | 编写 `model/` 全量接口类型定义 | 类型评审对照 API 清单 |
| P1-3 | 实现 HttpClient（BASE_URL、token 头、Result 解包、401 处理） | 单元验证 + 联调登录 |
| P1-4 | 登录/注册页 + preferences 会话持久化 | 真机登录成功、杀进程重启仍保持登录 |

### P2：核心业务页面

首页帖子流与板块侧栏、板块帖子列表、帖子详情（含 RichText 展示）、评论树与楼中楼、点赞按钮、发帖（纯文本）、编辑、个人中心三页。验证：完整业务闭环场景回归 + 与 Web 端同账号互操作（App 发帖 Web 可见）。

### P3：管理端与收尾

管理后台四页（用户/板块/帖子/评论，纯列表与操作，无图表）、深色模式适配（可选）、异常与空态兜底、场景清单终验、导出 debug 签名 HAP 并真机安装验收。

## 6. 风险与对策

| 风险 | 等级 | 对策 |
|---|---|---|
| ArkTS 强类型（禁 any），前端逻辑无法直接平移 | 高 | 类型定义先行（P1-2），页面按"结构翻译 + 逻辑重写"节奏推进，不赶进度 |
| 后端 POST + @RequestParam 传参形态多样 | 中 | api 层逐接口对照清单实现，联调时以 Knife4j 实测为准 |
| 富文本编辑降级为纯文本 | 中 | 课程演示可接受；详情页 RichText 保证旧数据可读 |
| 明文 HTTP 被系统策略拦截 | 中 | 模块配置放行明文，仅本地演示用途；若受阻则临时 HTTPS 反代 |
| 双端并行维护成本 | 低 | Web 端冻结新功能，仅修缺陷；新需求一律进 HarmonyOS 端 |
| 手机与后端网络不互通 | 低 | 演示统一走云服务器公网地址，避免局域网环境差异 |

## 7. 附录

### 附录 A：本次废弃的接口清单（原 API 清单的修订记录）

| 方法与路径 | 原用途 | 处理 |
|---|---|---|
| GET /admin/statistics/overview | 统计概览 | 保留，实现改为业务表实时 COUNT |
| GET /admin/statistics/hot-posts | 热门帖子（Spark） | 废弃；管理帖子页改用 POST /posts/list |
| GET /admin/statistics/board-active | 板块活跃度排行 | 废弃 |
| GET /admin/statistics/active-users | 用户活跃度排行 | 废弃 |
| GET /admin/statistics/content-quality | 内容质量分析 | 废弃 |
| GET /admin/statistics/daily-trend | 每日趋势 | 废弃 |
| POST /admin/statistics/trigger-spark | 手动触发 Spark 跑批 | 废弃 |
| GET /admin/statistics/spark-status | Spark 跑批状态 | 废弃 |

### 附录 B：语义变更记录

- `activeUserCount`：原为 Spark 统计的活跃用户数，现弱化为"正常状态用户数"；
- `updateTime`：原为跑批时间，现为实时统计的当前时间。

### 附录 C：实施状态

- 2026-10-26：方案定稿（v1.0），未开工。P0-P3 各步骤见第 5 节，开工后逐项在此登记完成情况与偏差。
