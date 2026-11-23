# 开发进度

记录项目各模块与迁移工作的实施状态。项目介绍、功能说明与运行方式见 [README](./README.md)，技术细节见 [文档/技术设计.md](./文档/技术设计.md)。

- 最后更新：2026-11-23
- 状态口径：`已完成` 表示功能实现且通过验证；`进行中` 表示代码已落地但验证未完成；`未开始` 表示尚未实施；`已废弃` 表示已确认不再推进

## 1. 总览

| 阶段 | 内容 | 状态 |
|------|------|------|
| 项目搭建 | Spring Boot + MyBatis + MySQL + Security | 已完成 |
| 用户认证 | 注册、登录、JWT 鉴权、BCrypt 加密 | 已完成 |
| 板块管理 | 浏览、CRUD、排序、启用/禁用 | 已完成 |
| 帖子管理 | 发布、列表、详情、编辑、删除、置顶、搜索 | 已完成 |
| 评论管理 | 发表、二级回复、树形展示、删除、计数同步 | 已完成 |
| 点赞管理 | 帖子/评论通用点赞、切换、状态、计数 | 已完成 |
| 后台管理 | 用户/板块/内容管理 + 统计概览 + 热门帖子 | 已完成 |
| Spark 分析 | 离线跑批写统计表：热门/板块活跃度/用户活跃度/内容质量/每日趋势 | 已完成 |
| 大数据工具 | `datagen.ps1` 生成业务表 CSV、`BulkDataLoader` Spark 批量导入 | 已完成 |
| 测试体系 | 67 个自动化测试，覆盖 Service/Controller/鉴权/统计 | 已完成 |
| 文档收口 | README、需求分析、数据库设计、实验报告、API 清单 | 已完成 |
| 数据库联调 | 云服务器 MySQL 真实环境联调通过 | 已完成 |
| 前端应用 | Vue 3 + Element Plus + ECharts（15 个页面、8 个公共组件） | 已完成 |
| 一体打包部署 | 前端构建产物内嵌后端静态资源 + SPA 路由兜底 | 已完成 |
| HarmonyOS 客户端 | ArkTS/ArkUI Stage 工程，复用后端 REST API | 进行中（代码完成，待真机验收） |
| 后端与 Web 端瘦身 | 移除 Spark 统计链路（迁移方案 P0） | 已完成（代码与构建验证，运行时回归待部署决策后执行） |

## 2. HarmonyOS 客户端迁移

依据 [文档/HarmonyOS迁移实现方案.md](./文档/HarmonyOS迁移实现方案.md) 实施，工程位于 `Harmony-Version/`，独立托管于 GitCode 组织 `zuthm` 的 `Forum` 仓库。

### 2.1 分阶段状态

| 阶段 | 内容 | 状态 |
|---|---|---|
| P0 | 后端与 Web 端移除 Spark 统计链路 | 已完成（2026-11-17，代码与构建验证通过） |
| P1 | 工程骨架、数据模型、网络层、登录会话 | 已完成（API 26 构建通过） |
| P2 | 核心业务页面与组件 | 已完成（未真机联调） |
| P3 | 管理端四页、异常兜底、HAP 产出 | 部分完成（HAP 已构建，未签名安装） |

### 2.2 已完成

- Stage 工程：单 `entry` 模块，`AppScope` 应用配置，49 个 `.ets` 源文件。
- 路由：`Index.ets` 唯一 `@Entry`，`Navigation` + `NavPathStack` 承载 14 个 `NavDestination` 与 `NotFound` 兜底；`AppRouter` 按 `AUTH_REQUIRED` / `ADMIN_REQUIRED` 实现等价路由守卫。
- 网络层：`HttpClient` 支持 JSON Body、POST + 查询参数、裸标量 Body 三种传参形态；统一解包 `Result`，401/403 清会话并跳转登录。
- 接口封装：27 条路径，覆盖后端 7 个 Controller 的全部非统计端点，管理端 12 个端点全量封装。
- 会话与配置：`PreferenceStore` 持久化 token 与用户信息，`SettingsStore` 支持运行时切换后端地址。
- 页面：首页、板块列表、帖子详情、发帖、编辑、个人中心三页、登录、注册、服务地址、404，以及管理端用户/板块/帖子/评论四页。
- 组件：`NavHeader`、`BoardSidebar`、`PostCard`、`CommentSection`、`CommentItem`、`LikeButton`、`StateViews`。
- 富文本降级：编辑使用 `TextArea` 纯文本，详情使用 `RichText` 渲染既有 HTML。

### 2.3 未完成

- 未配置发布签名：当前产物为 `entry-default-unsigned.hap`，无法安装到真机。
- 真机与模拟器联调未执行，验收清单 9 项无执行记录。
- 富文本编辑为纯文本降级，复杂排版内容无法在 App 端编辑。
- P0 运行时验证未执行：概览接口 Knife4j 人工核对、Web 端全功能手工回归（登录→浏览→发帖→评论→点赞→管理）需启动应用与数据库后进行，已随数据/服务部署位置决策一并暂缓（该决策留到最后通过配置文件解决，见待办）。

### 2.4 P0 后端与 Web 端瘦身实施记录（2026-11-17）

后端：

- 删除 6 个统计实体（`StatOverview` 等）、`StatMapper` 接口与 XML、`SparkStatsService` 接口与实现、旧 `StatisticsServiceImplTest`。
- `AdminController` 保留 `/admin/statistics/overview`，删除其余 7 个统计端点及 `SparkStatsService` 注入。
- `StatisticsService` 精简为仅含 `getOverview`；`StatisticsServiceImpl` 注入 5 个业务 Mapper（复用既有 `countUsers` / `countActiveUsers` / `countEnabledBoards` / `countPosts` / `countComments` / `countLikes`），实时 COUNT 聚合，`updateTime` 返回当前时间（语义变更见迁移方案附录 B）。
- `application.properties` 删除 `spark.*` 配置项；部署相关参数（`DB_URL` / `DB_USERNAME` / `DB_PASSWORD` 等）未改动，留待部署决策时通过配置调整。
- 新增 `StatisticsServiceImplTest` 2 个用例（实时聚合、null 兜底）。
- 验证：`mvn clean test` 全绿（58 个测试；原 67 个中删除 11 个 Spark 统计用例、新增 2 个）；`mvn clean package` 成功。

Web 端：

- 删除 `services/modules/statistics.js`、`pages/admin/StatisticsPage.vue`、`/admin/statistics` 路由、`package.json` 的 `echarts` 依赖、`admin.js` 中注释的旧统计代码。
- `NavHeader.vue` 管理后台链接改指 `/admin/users`。
- `PostsPage.vue` 数据源由 `getHotPosts({ limit: 50 })` 改为 `listPosts({ page: 1, size: 50 })`（方案 3.3 修复项）；该页原本无板块筛选/搜索 UI，与 Harmony 端 `AdminPostsPage` 的纯列表实现保持一致。
- `frontend/dist` 重新构建并覆盖 `src/main/resources/static`，内嵌资源已不含统计页面 chunk。
- 验证：`npm run build` 通过；全量搜索 `statistics` / `echarts` / `getHotPosts` 无残留引用。

独立工程归档：

- `spark-stats/`、`spark_stats.sql`、`spark_demo_data.sql` 已备份至 `%TEMP%\wb-archive-p0-spark-20261117`（19 个文件，含 target 产物与数据 CSV）后从工作区移除；git 历史可随时找回。
- 验收材料中 Spark 相关交付物保留未动；数据库 `stat_*` 六张表保留不 DROP（方案 3.1 约定）。

### 2.5 版本升级记录（2026-11-16）

SDK 基线由 HarmonyOS NEXT API 12 升级至 API 26（HarmonyOS 26.0.0），配置调整如下：

| 文件 | 变更 |
|---|---|
| `build-profile.json5` | `compileSdkVersion` / `compatibleSdkVersion` / `targetSdkVersion` 统一为 `26.0.0` |
| `oh-package.json5` | `modelVersion` 由 `5.0.0` 调整为 `6.0.0` |
| `hvigor/hvigor-config.json5` | 新增，`modelVersion` 为 `6.0.0`，补齐原工程缺失的 hvigor 构建配置 |

代码层适配：

- `PostCreatePage.ets` / `PostEditPage.ets`：`Select` 组件的 `.fontSize()` 改为 `.font({ size })`，原写法在 API 26 下无对应属性。
- `BoardSidebar.ets` / `CommentItem.ets` / `CommentSection.ets` / `LikeButton.ets`：回调成员移除 `private` 修饰，否则父组件无法通过构造函数注入，回调静默失效。

构建验证结果：

- 工具链：DevEco Studio 26.0.0.851，hvigor 6.26.8，SDK `apiVersion 26`（`platformVersion 26.0.0`），JDK 25（DevEco 内置 JBR）。
- 命令：`hvigorw assembleHap --mode module -p product=default -p buildMode=debug`
- 结果：`BUILD SUCCESSFUL`，耗时约 21 秒，产出 `entry/build/default/outputs/default/entry-default-unsigned.hap`（753 KB）。
- 包内校验：`compileSdkVersion` 为 `26.0.0.105`，`targetAPIVersion` 与 `minAPIVersion` 均为 `260000026`。

## 3. 待办

| 优先级 | 事项 |
|---|---|
| 高 | 配置调试签名，产出可安装 HAP 并在真机跑通验收清单 9 项 |
| 高 | 数据/服务部署位置决策（本地或云端）——已暂缓，留到迁移收尾阶段通过修改配置文件解决，不做代码层改动 |
| 中 | P0 运行时验证：启动应用后核对概览接口返回结构（Knife4j）、Web 端全功能手工回归，依赖上一项部署决策 |
| 中 | 处理 API 26 下的废弃告警：`AlertDialog.show`（5 处）、`promptAction.showToast`（1 处）改用 UIContext 版本 | 已解决：2026-11-23 改用 `this.getUIContext().showAlertDialog` 与 `UIContext.getPromptAction().showToast`（新增 `utils/UIContextHolder.ets`，由 `Index` 注入），clean 全量构建通过，deprecated 告警清零 |
| 中 | 详情页 `RichText` 在后续 SDK 版本中的替代方案评估 |
| 低 | 补齐 `ohosTest` 用例目录，或在构建配置中移除未使用的 `ohosTest` target | 已解决：2026-11-17 移除 `entry/build-profile.json5` 的 ohosTest target（工程无测试用例，保留悬空声明无意义） |
| 低 | 服务地址默认值 `127.0.0.1:8080` 对真机不可达，考虑首次启动强制引导至服务地址页 |

## 4. 已知风险

| 风险 | 等级 | 说明 |
|---|---|---|
| 未签名 HAP | 高 | 无法安装到真机，P3 交付物尚不可用 |
| 真机联调缺失 | 高 | 网络、权限、明文 HTTP 放行策略均未在设备侧验证 |
| 部署位置未定 | 中 | 数据/服务运行在本地或云端尚未决策，P0 运行时验证与双端联调被阻塞；决策后仅需改配置，无需代码改动 |
| 明文 HTTP 放行 | 中 | `network_config.json` 开放 `cleartextTrafficPermitted`，仅适用于课程演示 |
| 双端并行维护 | 低 | Web 端与 HarmonyOS 端需同步接口变更 |

## 5. 变更记录

| 日期 | 变更 |
|---|---|
| 2026-11-23 | 工程路径去空格完成（`Java Project` → `JavaProject`，用户手动执行），CLI 构建在新路径验证通过，DevEco 打开工程的阻塞解除；处理 API 26 废弃告警 6 处：5 处 `AlertDialog.show` 改 `this.getUIContext().showAlertDialog`，1 处 `promptAction.showToast` 改 `UIContext.getPromptAction().showToast`（新增 `utils/UIContextHolder.ets` 全局持有器，由根页面 `Index` 注入），clean 全量构建 BUILD SUCCESSFUL，deprecated 告警清零（剩余 `app_name` 资源重复声明告警 1 条与未签名提示 1 条，属既有状态未动） |
| 2026-11-17 | 修复 Harmony 工程配置与模板不一致项：移除悬空的 `signingConfig: "default"` 引用与 `ohosTest` target，DevEco Studio 26 可直接打开构建（`hvigorw assembleHap` 验证通过，BUILD SUCCESSFUL）；GitCode 交付形态定稿为单一 `master` 分支承载完整项目（覆盖原子树内容并删除远端 `Harmony` 分支，subtree 同步流程废止），今后 Harmony 分支提交双推 `origin/Harmony`（GitHub）与 `gitcode master` |
| 2026-11-17 | 执行迁移方案 P0：删除后端与 Web 端 Spark 统计链路，概览改实时 COUNT，管理帖子页改用 POST /posts/list，归档移除 spark-stats 独立工程；测试 58 个全绿，前后端构建通过 |
| 2026-11-16 | HarmonyOS 基线升级至 API 26 并完成首次构建验证；README 拆分为介绍类文档与本文档 |
| 2026-11-02 | HarmonyOS 客户端工程落地（`Harmony-Version/`），纳入 `Harmony` 分支 |
| 2026-10-26 | HarmonyOS 迁移实现方案 v1.0 定稿 |
| 2026-08-27 | 文档重组，模块三收口，验收材料归档 |
| 2026-06-24 | 前后端一体打包部署，前端静态资源内嵌 |
