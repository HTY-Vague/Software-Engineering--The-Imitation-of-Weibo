import { AppRouter } from '../router/AppRouter';
import { RouteNames } from '../router/RouteNames';
import { readRouteParam } from '../router/RouteParam';
import { AuthStore } from '../store/AuthStore';
import { SettingsStore } from '../store/SettingsStore';
import { HomePage } from './HomePage';
import { LoginPage } from './LoginPage';
import { RegisterPage } from './RegisterPage';
import { PostCreatePage } from './PostCreatePage';
import { PostDetailPage } from './PostDetailPage';
import { PostEditPage } from './PostEditPage';
import { BoardPostsPage } from './BoardPostsPage';
import { MeProfilePage } from './MeProfilePage';
import { MePostsPage } from './MePostsPage';
import { MeCommentsPage } from './MeCommentsPage';
import { SettingsPage } from './SettingsPage';
import { NotFoundPage } from './NotFoundPage';
import { AdminUsersPage } from './admin/AdminUsersPage';
import { AdminForumsPage } from './admin/AdminForumsPage';
import { AdminPostsPage } from './admin/AdminPostsPage';
import { AdminCommentsPage } from './admin/AdminCommentsPage';
import { setGlobalUIContext } from '../utils/UIContextHolder';

/**
 * 应用入口页面：唯一 @Entry，承载 Navigation 页面栈。
 * 首页 HomePage 作为栈底内容，其余页面通过 NavPathStack 进出。
 * 等价 Web 端 App.vue + router/index.js 的组合职责。
 */
@Entry
@Component
struct Index {
  private stack: NavPathStack = AppRouter.getStack();

  aboutToAppear(): void {
    // 注入全局 UIContext，供 ToastUtil 等非组件作用域使用 UIContext 版本 API
    setGlobalUIContext(this.getUIContext());
    // 双重保险：EntryAbility 已发起，这里再确认一次会话与地址就绪
    AuthStore.hydrate().catch(() => undefined);
    SettingsStore.load().catch(() => undefined);
  }

  build() {
    Navigation(this.stack) {
      HomePage()
    }
    .navDestination(this.pageMap)
    .mode(NavigationMode.Stack)
    .hideTitleBar(true)
    .hideToolBar(true)
    .width('100%')
    .height('100%')
  }

  @Builder
  pageMap(name: string, param?: Object) {
    if (name === RouteNames.Login) {
      LoginPage({ param: readRouteParam(param) })
    } else if (name === RouteNames.Register) {
      RegisterPage()
    } else if (name === RouteNames.PostCreate) {
      PostCreatePage({ param: readRouteParam(param) })
    } else if (name === RouteNames.PostDetail) {
      PostDetailPage({ postId: readRouteParam(param).postId })
    } else if (name === RouteNames.PostEdit) {
      PostEditPage({ postId: readRouteParam(param).postId })
    } else if (name === RouteNames.BoardPosts) {
      BoardPostsPage({ boardId: readRouteParam(param).boardId })
    } else if (name === RouteNames.MeProfile) {
      MeProfilePage()
    } else if (name === RouteNames.MePosts) {
      MePostsPage()
    } else if (name === RouteNames.MeComments) {
      MeCommentsPage()
    } else if (name === RouteNames.Settings) {
      SettingsPage()
    } else if (name === RouteNames.AdminUsers) {
      AdminUsersPage()
    } else if (name === RouteNames.AdminForums) {
      AdminForumsPage()
    } else if (name === RouteNames.AdminPosts) {
      AdminPostsPage()
    } else if (name === RouteNames.AdminComments) {
      AdminCommentsPage()
    } else {
      NotFoundPage()
    }
  }
}
