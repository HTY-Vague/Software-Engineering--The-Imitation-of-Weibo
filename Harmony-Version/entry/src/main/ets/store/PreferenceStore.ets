import preferences from '@ohos.data.preferences';
import { AppContext } from './AppContext';

/**
 * Preferences 轻量封装，替代 Web 端的 localStorage。
 * 用于持久化 token、用户信息与后端服务地址。
 */
const STORE_NAME: string = 'jiantan_preferences';

export class PreferenceStore {
  private static instance: preferences.Preferences | undefined = undefined;

  static getInstance(): Promise<preferences.Preferences> {
    return new Promise<preferences.Preferences>((resolve, reject) => {
      if (PreferenceStore.instance !== undefined) {
        resolve(PreferenceStore.instance);
        return;
      }

      preferences.getPreferences(AppContext.get(), STORE_NAME)
        .then((store: preferences.Preferences) => {
          PreferenceStore.instance = store;
          resolve(store);
        })
        .catch((error: Object) => {
          reject(error);
        });
    });
  }

  static getString(key: string, defaultValue: string): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      PreferenceStore.getInstance()
        .then((store: preferences.Preferences) => store.get(key, defaultValue))
        .then((value: preferences.ValueType) => {
          resolve(value as string);
        })
        .catch((error: Object) => {
          reject(error);
        });
    });
  }

  static putString(key: string, value: string): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      PreferenceStore.getInstance()
        .then((store: preferences.Preferences) => {
          return store.put(key, value);
        })
        .then(() => {
          const store: preferences.Preferences = PreferenceStore.instance as preferences.Preferences;
          return store.flush();
        })
        .then(() => {
          resolve();
        })
        .catch((error: Object) => {
          reject(error);
        });
    });
  }

  static remove(key: string): Promise<void> {
    return new Promise<void>((resolve, reject) => {
      PreferenceStore.getInstance()
        .then((store: preferences.Preferences) => {
          return store.delete(key);
        })
        .then(() => {
          const store: preferences.Preferences = PreferenceStore.instance as preferences.Preferences;
          return store.flush();
        })
        .then(() => {
          resolve();
        })
        .catch((error: Object) => {
          reject(error);
        });
    });
  }
}
