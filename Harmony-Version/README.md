# 简坛 · HarmonyOS 客户端

仿微博论坛项目的 HarmonyOS 手机端，基于 ArkTS / ArkUI Stage 模型，复用现有 Spring Boot 后端 REST API 与 JWT 鉴权体系。

本目录是一个**独立可运行的完整工程副本**：不引用、不修改、不依赖原项目的 `src/`（Java 后端）与 `frontend/`（Vue Web 前端）。

- 应用名称：简坛
- BundleName：`com.example.jiantan`
- SDK 基线：HarmonyOS NEXT，API 12（`compatibleSdkVersion: 5.0.0(12)`）
- 设备类型：phone
- 交付形态：本地调试签名 HAP，不发行不上架

## 一、运行方式

1. 安装 DevEco Studio（5.0 及以上）并下载 API 12 SDK。
2. 用 DevEco Studio 打开本目录（`Harmony-Version/`），等待 `hvigor` 同步完成。
3. 首次运行使用 IDE 自动生成的调试证书与 Profile：`File > Project Structure > Signing Configs` 勾选自动生成。
4. 后端准备：启动原项目 Spring Boot 服务（默认 `http://<host>:8080`）。
5. 配置服务地址：App 内「服务地址」页可填写并保存；也可改 `entry/src/main/ets/store/SettingsStore.ets` 的 `DEFAULT_BASE_URL`。
   - 模拟器访问宿主机用 `http://10.0.2.2:8080`
   - 真机用局域网地址或云服务器公网地址
6. 运行：`Run > Run 'entry'`，或命令行 `hvigorw assembleHap` 产出 debug HAP 后用 `hdc install` 安装。

> 后端为明文 HTTP，已在 `entry/src/main/resources/base/profile/network_config.json` 放行明文流量（`cleartextTrafficPermitted: true`），并在 `module.json5` 通过 `metadata` 挂载。仅用于课程演示，正式发布前必须改为 HTTPS 并关闭该配置。

## 二、目录结构

```
Harmony-Version/
├── AppScope/                      应用级配置与图标
│   ├── app.json5                  bundleName / 版本 / 图标 / 应用名
│   └── resources/base/
├── entry/                         主模块（单模块覆盖全部页面）
│   ├── build-profile.json5
│   ├── hvigorfile.ts
│   ├── obfuscation-rules.txt
│   ├── oh-package.json5
│   └── src/main/
│       ├── module.json5           权限 INTERNET、明文放行、Ability 注册
│       ├── resources/
│       │   ├── base/element/      color.json / string.json / float.json
│       │   ├── base/media/        图标
│       │   └── base/profile/      main_pages.json、network_config.json
│       └── ets/
│           ├── entryability/      EntryAbility.ets
│           ├── pages/             页面（Index 为唯一 @Entry）
│           │   └── admin/         管理后台四页
│           ├── components/        公共组件
│           ├── api/               网络层与接口模块
│           ├── model/             数据模型
│           ├── store/             会话与配置存储
│           ├── router/            页面栈与路由守卫
│           └── utils/             时间与文本工具
├── build-profile.json5            工程级签名与 SDK 基线
├── hvigorfile.ts
└── oh-package.json5
```

## 三、与原项目文件的对应关系

### 分层对应

| 原 Web 前端 | 本工程 |
|---|---|
| `src/main.js` + `App.vue` | `ets/entryability/EntryAbility.ets` + `ets/pages/Index.ets` |
| `router/index.js` | `ets/router/AppRouter.ets`（含守卫）、`RouteNames.ets`、`RouteParam.ets` |
| `services/http.js` | `ets/api/HttpClient.ets` |
| `services/modules/*.js` | `ets/api/{Auth,User,Forum,Post,Comment,Like,Admin}Api.ets` |
| `stores/*.js`（Pinia） | `ets/store/AuthStore.ets`、`SettingsStore.ets` |
| `types/api.js` | `ets/model/*.ets` |
| `utils/time.js` | `ets/utils/TimeUtil.ets` |
| `src/style.css` 设计变量 | `resources/base/element/color.json`、`float.json` |
| `pages/**/*.vue` | `ets/pages/**/*.ets（含 admin/）` |
| `components/**/*.vue` | `ets/components/*.ets` |

### 页面对应

| 原页面 | 目标页面 |
|---|---|
| `pages/home/HomePage.vue` | `pages/HomePage.ets`（Navigation 根页面） |
| `pages/auth/LoginPage.vue` | `pages/LoginPage.ets` |
| `pages/auth/RegisterPage.vue` | `pages/RegisterPage.ets` |
| `pages/post/PostCreatePage.vue` | `pages/PostCreatePage.ets` |
| `pages/post/PostDetailPage.vue` | `pages/PostDetailPage.ets` |
| `pages/post/PostEditPage.vue` | `pages/PostEditPage.ets` |
| `pages/board/BoardPostsPage.vue` | `pages/BoardPostsPage.ets` |
| `pages/me/ProfilePage.vue` | `pages/MeProfilePage.ets` |
| `pages/me/MyPostsPage.vue` | `pages/MePostsPage.ets` |
| `pages/me/MyCommentsPage.vue` | `pages/MeCommentsPage.ets` |
| `pages/home/HomeNotFound.vue` | `pages/NotFoundPage.ets` |
| `pages/admin/UsersPage.vue` | `pages/admin/AdminUsersPage.ets` |
| `pages/admin/ForumsPage.vue` | `pages/admin/AdminForumsPage.ets` |
| `pages/admin/PostsPage.vue` | `pages/admin/AdminPostsPage.ets` |
| `pages/admin/CommentsPage.vue` | `pages/admin/AdminCommentsPage.ets` |
| 无（新增） | `pages/SettingsPage.ets`（后端服务地址切换） |

### 组件对应

| 原组件 | 目标组件 | 说明 |
|---|---|---|
| `layout/NavHeader.vue` | `components/NavHeader.ets` | 管理后台入口已改为用户管理页 |
| `layout/BoardSidebar.vue` | `components/BoardSidebar.ets` | 竖排列表改为横向滚动卡片 |
| `post/PostCard.vue` | `components/PostCard.ets` | |
| `comment/CommentSection.vue` | `components/CommentSection.ets` | |
| `comment/CommentItem.vue` | `components/CommentItem.ets` | 两层评论，children 直接展开 |
| `like/LikeButton.vue` | `components/LikeButton.ets` | |
| `editor/RichTextEditor.vue` | 无 | **降级**：编辑用 `TextArea` 纯文本，展示用 `RichText` |
| Element Plus `el-table` | 卡片列表 | 管理端表格改卡片 |
| `el-dialog` | 页面内内联编辑区 | 板块新增/编辑 |
| `ElMessageBox.confirm` | `AlertDialog.show` | 删除确认 |
| `ElMessage` | `utils/ToastUtil.ets` | |

## 四、接口传参要点（易错点）

后端接口传参形态不统一，`api/` 各文件已逐条对齐 Controller：

- **JSON Body**：`/auth/login`、`/auth/register`、`/posts/create`、`/posts/update`、`/comments/create`、`/user/update/profile`、`/forum/board/posts`、`/admin/forums`、`/admin/forums/sort`
- **POST + 查询参数**：`/posts/detail`、`/posts/list`、`/posts/delete`、`/comments/list`、`/comments/delete`、`/likes/*`、`/user/delete/post`、`/user/update/password`
- **`@RequestBody` 裸标量**：`/admin/users/{id}/status`、`/admin/forums/{id}/status`、`/admin/posts/{id}/top`，body 为纯数字文本且需 `Content-Type: application/json`
- **评论层级判定**：后端以 `parentCommentId == null` 判定顶级评论，顶级评论必须传 `null`，不能传 `0`（见 `model/ForumComment.ets` 的 `CommentCreateRequest`）

## 五、与原 Web 端的功能差异（迁移方案已确认）

| 项 | 处理 |
|---|---|
| 富文本编辑 | 降级为纯文本输入，提交前把换行转成 `<br/>`；详情页用 `RichText` 渲染既有 HTML |
| 数据统计页 | 不纳入 App 端（方案确认），管理端只有用户/板块/帖子/评论四页 |
| 管理端帖子数据源 | 用 `POST /posts/list`（方案 3.3），不再用 `/admin/statistics/hot-posts` |
| 服务地址 | 新增「服务地址」页，运行时切换并持久化 |
| 表格组件 | `el-table` 改为卡片列表，适配手机屏幕 |
| token 存储 | `localStorage` 改为 `@ohos.data.preferences` |

## 六、依赖关系与前置事项

- 本工程**只包含客户端**，数据来自后端 REST 接口，后端须先启动并可被设备访问。
- 原迁移方案中的 **P0（后端与 Web 端 Spark 统计瘦身）尚未执行**。当前后端仍保留 `/admin/statistics/*` 八个端点与 Spark 相关文件；本客户端不调用它们，因此 P0 是否执行不影响本工程运行。
- 后端执行 P0 后，`/admin/statistics/hot-posts` 将被废弃；本客户端已按方案 3.3 使用 `POST /posts/list`，无需返工。

## 七、验收清单

1. 工程可在 DevEco Studio 打开并完成 `hvigor` 同步。
2. 登录 / 注册成功；杀进程重启后仍保持登录。
3. 首页板块筛选与关键词搜索生效。
4. 帖子详情、评论、楼中楼回复、点赞与取消点赞正常。
5. 发帖与编辑成功（纯文本），Web 端可见同一条帖子。
6. 个人中心资料保存、改密码、我的帖子/评论的删除正常。
7. 管理员账号可见管理后台入口，能启停用户与板块、置顶/删除帖子、删除评论。
8. 非管理员账号访问管理页被拦截回首页；未登录访问受保护页跳登录页并可回跳。
9. 后端不可达或返回错误时，页面有空态/提示且无白屏崩溃。
