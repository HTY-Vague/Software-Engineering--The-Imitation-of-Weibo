import { User, LoginData, ROLE_ADMIN } from '../model/User';
import { PreferenceStore } from './PreferenceStore';

/** Preferences 持久化键，与 Web 端 localStorage 的 token / user 对齐 */
const PREF_TOKEN: string = 'token';
const PREF_USER: string = 'user';

/** AppStorage 键：仅放基础类型，供组件 @StorageLink 响应式刷新 */
export const STORAGE_IS_LOGIN: string = 'isLogin';
export const STORAGE_IS_ADMIN: string = 'isAdmin';
export const STORAGE_NICKNAME: string = 'userNickname';

/**
 * 会话状态仓库，等价于 Web 端 stores/auth.js（Pinia + localStorage）。
 * 落盘用 Preferences，内存用 AppStorage 驱动 UI 刷新。
 */
export class AuthStore {
  private static token: string = '';
  private static user: User = new User();
  private static hydrated: boolean = false;
  private static unauthorizedHandler: (() => void) | undefined = undefined;

  /** 启动时恢复会话，对应 Web 端 hydrate() */
  static hydrate(): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      if (AuthStore.hydrated) {
        resolve();
        return;
      }

      PreferenceStore.getString(PREF_TOKEN, '')
        .then((token: string) => {
          AuthStore.token = token;
          return PreferenceStore.getString(PREF_USER, '');
        })
        .then((rawUser: string) => {
          if (rawUser.length > 0) {
            AuthStore.user = JSON.parse(rawUser) as User;
          }
          AuthStore.hydrated = true;
          AuthStore.syncToStorage();
          resolve();
        })
        .catch((error: Object) => {
          AuthStore.hydrated = true;
          AuthStore.syncToStorage();
          reject(error);
        });
    });
  }

  static isLogin(): boolean {
    return AuthStore.token.length > 0;
  }

  static isAdmin(): boolean {
    return AuthStore.isLogin() && AuthStore.user.role === ROLE_ADMIN;
  }

  static getToken(): string {
    return AuthStore.token;
  }

  static getUser(): User {
    return AuthStore.user;
  }

  /** 登录成功后写入会话；对应 Web 端 setAuth() */
  static setAuth(data: LoginData): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      AuthStore.token = data.token === undefined ? '' : data.token;
      AuthStore.user = data.user === undefined ? new User() : data.user;

      PreferenceStore.putString(PREF_TOKEN, AuthStore.token)
        .then(() => {
          return PreferenceStore.putString(PREF_USER, JSON.stringify(AuthStore.user));
        })
        .then(() => {
          AuthStore.syncToStorage();
          resolve();
        })
        .catch((error: Object) => {
          reject(error);
        });
    });
  }

  /** 个人资料更新后合并到本地会话（不覆盖 token） */
  static updateUser(user: User): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      AuthStore.user = user;
      PreferenceStore.putString(PREF_USER, JSON.stringify(user))
        .then(() => {
          AuthStore.syncToStorage();
          resolve();
        })
        .catch((error: Object) => {
          reject(error);
        });
    });
  }

  /** 退出登录；对应 Web 端 clearAuth() */
  static clearAuth(): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      AuthStore.token = '';
      AuthStore.user = new User();

      PreferenceStore.remove(PREF_TOKEN)
        .then(() => {
          return PreferenceStore.remove(PREF_USER);
        })
        .then(() => {
          AuthStore.syncToStorage();
          resolve();
        })
        .catch((error: Object) => {
          reject(error);
        });
    });
  }

  /** 注册 401/403 的跳转回调，由路由层在启动时注入，避免反向依赖 */
  static registerUnauthorizedHandler(handler: () => void): void {
    AuthStore.unauthorizedHandler = handler;
  }

  static notifyUnauthorized(): void {
    AuthStore.clearAuth()
      .then(() => {
        if (AuthStore.unauthorizedHandler !== undefined) {
          const handler: () => void = AuthStore.unauthorizedHandler as () => void;
          handler();
        }
      })
      .catch(() => {
        // 清理失败不阻断跳转
      });
  }

  private static syncToStorage(): void {
    AppStorage.setOrCreate<boolean>(STORAGE_IS_LOGIN, AuthStore.isLogin());
    AppStorage.setOrCreate<boolean>(STORAGE_IS_ADMIN, AuthStore.isAdmin());
    AppStorage.setOrCreate<string>(STORAGE_NICKNAME, AuthStore.displayName());
  }

  /** 展示名优先级：昵称 > 用户名 > 兜底 */
  static displayName(): string {
    if (AuthStore.user.nickname !== undefined && AuthStore.user.nickname.length > 0) {
      return AuthStore.user.nickname;
    }
    if (AuthStore.user.username !== undefined && AuthStore.user.username.length > 0) {
      return AuthStore.user.username;
    }
    return '用户';
  }
}
