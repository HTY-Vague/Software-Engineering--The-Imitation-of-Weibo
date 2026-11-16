# 开发进度

记录项目各模块与迁移工作的实施状态。项目介绍、功能说明与运行方式见 [README](./README.md)，技术细节见 [文档/技术设计.md](./文档/技术设计.md)。

- 最后更新：2026-11-16
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
| 后端与 Web 端瘦身 | 移除 Spark 统计链路（迁移方案 P0） | 未开始 |

## 2. HarmonyOS 客户端迁移

依据 [文档/HarmonyOS迁移实现方案.md](./文档/HarmonyOS迁移实现方案.md) 实施，工程位于 `Harmony-Version/`，独立托管于 GitCode 组织 `zuthm` 的 `Forum` 仓库。

### 2.1 分阶段状态

| 阶段 | 内容 | 状态 |
|---|---|---|
| P0 | 后端与 Web 端移除 Spark 统计链路 | 未开始 |
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

- P0 全部未执行：后端 8 个 `/admin/statistics/*` 端点、`StatisticsService`、`SparkStatsService`、`StatMapper`、6 个统计实体仍保留；`spark-stats/` 与两个统计 SQL 脚本仍在工作区；Web 端 `StatisticsPage.vue`、`statistics.js`、`echarts` 依赖、`NavHeader` 统计入口、`PostsPage.vue` 的 `getHotPosts` 数据源均未调整。
- 未配置发布签名：当前产物为 `entry-default-unsigned.hap`，无法安装到真机。
- 真机与模拟器联调未执行，验收清单 9 项无执行记录。
- 富文本编辑为纯文本降级，复杂排版内容无法在 App 端编辑。

### 2.4 版本升级记录（2026-11-16）

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
| 高 | 决策 P0 是否执行；若执行，需同步调整后端统计链路、Web 端统计页、`spark-stats/` 与 SQL 脚本 |
| 中 | 处理 API 26 下的废弃告警：`AlertDialog.show`（5 处）、`promptAction.showToast`（1 处）改用 UIContext 版本 |
| 中 | 详情页 `RichText` 在后续 SDK 版本中的替代方案评估 |
| 低 | 补齐 `ohosTest` 用例目录，或在构建配置中移除未使用的 `ohosTest` target |
| 低 | 服务地址默认值 `127.0.0.1:8080` 对真机不可达，考虑首次启动强制引导至服务地址页 |

## 4. 已知风险

| 风险 | 等级 | 说明 |
|---|---|---|
| 未签名 HAP | 高 | 无法安装到真机，P3 交付物尚不可用 |
| 真机联调缺失 | 高 | 网络、权限、明文 HTTP 放行策略均未在设备侧验证 |
| P0 长期挂起 | 中 | App 不调用统计接口故可运行，但 Web 端仍依赖计划废弃的 `hot-posts` |
| 明文 HTTP 放行 | 中 | `network_config.json` 开放 `cleartextTrafficPermitted`，仅适用于课程演示 |
| 双端并行维护 | 低 | Web 端与 HarmonyOS 端需同步接口变更 |

## 5. 变更记录

| 日期 | 变更 |
|---|---|
| 2026-11-16 | HarmonyOS 基线升级至 API 26 并完成首次构建验证；README 拆分为介绍类文档与本文档 |
| 2026-11-02 | HarmonyOS 客户端工程落地（`Harmony-Version/`），纳入 `Harmony` 分支 |
| 2026-10-26 | HarmonyOS 迁移实现方案 v1.0 定稿 |
| 2026-08-27 | 文档重组，模块三收口，验收材料归档 |
| 2026-06-24 | 前后端一体打包部署，前端静态资源内嵌 |
