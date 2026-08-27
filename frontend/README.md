# 极简基础论坛系统 - 前端

基于 Vue 3 + Element Plus 的论坛前端工程，与后端 REST API（见根目录 [API接口清单.md](../API接口清单.md)）配套。

## 技术栈

- Vue 3（Composition API）+ Vite
- Element Plus（UI 组件库）
- Axios（HTTP 客户端，统一拦截器注入 JWT）
- Pinia（认证状态管理）
- Vue Router（16 条路由 + 登录/管理员守卫）
- WangEditor（富文本编辑器）
- ECharts（统计可视化：饼状图 / 柱状图 / 折线图）

## 目录结构

```text
src/
├── pages/        页面组件（15 个）：认证、首页、板块、帖子、个人中心、管理后台
├── components/   公共组件（8 个）：导航、板块侧栏、帖子卡片、评论区、点赞按钮、富文本编辑器
├── services/     Axios 封装与按模块划分的接口层（auth/user/forum/post/comment/like/admin/statistics）
├── stores/       Pinia 认证状态
└── router/       路由与导航守卫
```

## 本地开发

```bash
npm install
npm run dev
```

接口基址通过环境变量 `VITE_API_BASE_URL` 配置（未设置时为同源请求）。本地联调时在 `frontend/.env.local` 中写入后端地址（后端已开启 CORS）：

```properties
VITE_API_BASE_URL=http://localhost:8080
```

## 构建与一体打包部署

```bash
npm run build
```

构建产物输出到 `frontend/dist`。生产部署采用前后端一体打包：将 `dist` 内容复制到后端 `src/main/resources/static/` 后重新执行 `mvn clean package`，启动后端即可直接访问前端页面，SPA 路由由后端兜底转发。

## 演示账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `Admin_123` |
| 普通用户 | `demo_user` | `User_123` |
