import { RouteNames } from './RouteNames';
import { RouteParam } from './RouteParam';
import { AuthStore } from '../store/AuthStore';

/** 需要登录的页面，对应 Web 端 meta.requiresAuth */
const AUTH_REQUIRED: Array<string> = [
  RouteNames.PostCreate,
  RouteNames.PostEdit,
  RouteNames.MeProfile,
  RouteNames.MePosts,
  RouteNames.MeComments
];

/** 需要管理员角色的页面，对应 Web 端 meta.requiresAdmin */
const ADMIN_REQUIRED: Array<string> = [
  RouteNames.AdminUsers,
  RouteNames.AdminForums,
  RouteNames.AdminPosts,
  RouteNames.AdminComments
];

/**
 * 页面栈与路由守卫。
 * 单 EntryAbility + Navigation 页面栈，守卫逻辑等价于 Web 端 router.beforeEach。
 */
export class AppRouter {
  private static stack: NavPathStack = new NavPathStack();

  static getStack(): NavPathStack {
    return AppRouter.stack;
  }

  static push(name: string, param: RouteParam = new RouteParam()): void {
    if (!AppRouter.guard(name, param)) {
      return;
    }
    AppRouter.stack.pushPath({ name: name, param: param as Object });
  }

  static replace(name: string, param: RouteParam = new RouteParam()): void {
    if (!AppRouter.guard(name, param)) {
      return;
    }
    AppRouter.stack.replacePath({ name: name, param: param as Object });
  }

  /** 回退一页；空栈时为空操作 */
  static pop(): void {
    AppRouter.stack.pop();
  }

  /** 回到首页：首页是 Navigation 根页面，清空栈即可 */
  static backToHome(): void {
    AppRouter.stack.clear();
  }

  static isAuthRequired(name: string): boolean {
    return AUTH_REQUIRED.indexOf(name) >= 0;
  }

  static isAdminRequired(name: string): boolean {
    return ADMIN_REQUIRED.indexOf(name) >= 0;
  }

  /**
   * 路由守卫：返回 true 表示允许跳转，false 表示已被重定向。
   */
  private static guard(name: string, param: RouteParam): boolean {
    // 已登录用户访问登录/注册页，直接回首页
    if (name === RouteNames.Login || name === RouteNames.Register) {
      if (AuthStore.isLogin()) {
        AppRouter.stack.clear();
        return false;
      }
      return true;
    }

    // 未登录访问受保护页面：跳转登录页并携带回跳目标
    if (AppRouter.isAuthRequired(name) && !AuthStore.isLogin()) {
      const redirect: RouteParam = new RouteParam();
      redirect.redirect = name;
      redirect.redirectPostId = param.postId;
      AppRouter.stack.pushPath({ name: RouteNames.Login, param: redirect as Object });
      return false;
    }

    // 非管理员访问管理页：回首页
    if (AppRouter.isAdminRequired(name) && !AuthStore.isAdmin()) {
      AppRouter.stack.clear();
      return false;
    }

    return true;
  }
}
