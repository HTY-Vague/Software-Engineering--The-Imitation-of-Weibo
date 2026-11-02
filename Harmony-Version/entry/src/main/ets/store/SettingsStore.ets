import { PreferenceStore } from './PreferenceStore';

/**
 * 后端服务地址仓库。
 * 默认指向本机 8080（与后端 server.port 一致），可在"服务地址"页运行时切换并持久化。
 */
const PREF_BASE_URL: string = 'baseUrl';
export const STORAGE_BASE_URL: string = 'baseUrl';

export const DEFAULT_BASE_URL: string = 'http://127.0.0.1:8080';

export class SettingsStore {
  private static baseUrl: string = DEFAULT_BASE_URL;
  private static loaded: boolean = false;

  static load(): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      if (SettingsStore.loaded) {
        resolve(SettingsStore.baseUrl);
        return;
      }

      PreferenceStore.getString(PREF_BASE_URL, DEFAULT_BASE_URL)
        .then((value: string) => {
          SettingsStore.baseUrl = SettingsStore.normalize(value);
          SettingsStore.loaded = true;
          AppStorage.setOrCreate<string>(STORAGE_BASE_URL, SettingsStore.baseUrl);
          resolve(SettingsStore.baseUrl);
        })
        .catch((error: Object) => {
          SettingsStore.baseUrl = DEFAULT_BASE_URL;
          SettingsStore.loaded = true;
          AppStorage.setOrCreate<string>(STORAGE_BASE_URL, SettingsStore.baseUrl);
          reject(error);
        });
    });
  }

  static getBaseUrl(): string {
    return SettingsStore.baseUrl;
  }

  static save(input: string): Promise<string> {
    return new Promise<string>((resolve, reject) => {
      const next: string = SettingsStore.normalize(input);
      PreferenceStore.putString(PREF_BASE_URL, next)
        .then(() => {
          SettingsStore.baseUrl = next;
          SettingsStore.loaded = true;
          AppStorage.setOrCreate<string>(STORAGE_BASE_URL, next);
          resolve(next);
        })
        .catch((error: Object) => {
          reject(error);
        });
    });
  }

  /** 去掉结尾斜杠，补全 http 前缀，避免拼出 //path 或缺失协议 */
  static normalize(input: string): string {
    let value: string = input === undefined || input === null ? '' : input.trim();
    if (value.length === 0) {
      return DEFAULT_BASE_URL;
    }
    if (value.endsWith('/')) {
      value = value.slice(0, value.length - 1);
    }
    if (!value.startsWith('http://') && !value.startsWith('https://')) {
      value = `http://${value}`;
    }
    return value;
  }
}
