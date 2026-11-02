import http from '@ohos.net.http';
import { BusinessError } from '@ohos.base';
import { Result, RESULT_SUCCESS_CODE } from '../model/Result';
import { ApiError, ERROR_CODE_NETWORK } from '../model/ApiError';
import { AuthStore } from '../store/AuthStore';
import { SettingsStore } from '../store/SettingsStore';

const CONNECT_TIMEOUT: number = 15000;
const READ_TIMEOUT: number = 15000;

/**
 * 查询串构造器。
 * 后端大量接口为 POST + @RequestParam，参数必须拼在 URL 上而非 JSON body。
 */
export class QueryBuilder {
  private items: Array<string> = [];

  add(key: string, value: string | number | undefined): QueryBuilder {
    if (value === undefined || value === null) {
      return this;
    }
    this.items.push(`${key}=${encodeURIComponent(String(value))}`);
    return this;
  }

  build(): string {
    return this.items.length > 0 ? `?${this.items.join('&')}` : '';
  }
}

/**
 * 统一网络层，等价 Web 端 services/http.js（axios 实例 + 拦截器）。
 * 职责：拼 BASE_URL、注入 Bearer token、解包 Result、处理 401/403。
 */
export class HttpClient {
  static get<T>(path: string, query: string = ''): Promise<T> {
    return HttpClient.send<T>(http.RequestMethod.GET, path, query, undefined, true);
  }

  /** POST 且参数全部走 query（后端 @RequestParam） */
  static post<T>(path: string, query: string = ''): Promise<T> {
    return HttpClient.send<T>(http.RequestMethod.POST, path, query, undefined, true);
  }

  /** POST 且 body 为 JSON 对象（后端 @RequestBody 实体） */
  static postJson<T>(path: string, body: Object, query: string = ''): Promise<T> {
    return HttpClient.send<T>(http.RequestMethod.POST, path, query, JSON.stringify(body), true);
  }

  /** POST 且 body 为裸标量（后端 @RequestBody Integer） */
  static postScalar<T>(path: string, scalar: number, query: string = ''): Promise<T> {
    return HttpClient.send<T>(http.RequestMethod.POST, path, query, `${scalar}`, true);
  }

  static putJson<T>(path: string, body: Object, query: string = ''): Promise<T> {
    return HttpClient.send<T>(http.RequestMethod.PUT, path, query, JSON.stringify(body), true);
  }

  static putScalar<T>(path: string, scalar: number, query: string = ''): Promise<T> {
    return HttpClient.send<T>(http.RequestMethod.PUT, path, query, `${scalar}`, true);
  }

  static delete<T>(path: string, query: string = ''): Promise<T> {
    return HttpClient.send<T>(http.RequestMethod.DELETE, path, query, undefined, true);
  }

  private static send<T>(method: http.RequestMethod, path: string, query: string,
    body: string | undefined, withAuth: boolean): Promise<T> {
    return new Promise<T>((resolve, reject) => {
      const url: string = `${SettingsStore.getBaseUrl()}${path}${query}`;
      const header: Record<string, string> = { 'Content-Type': 'application/json' };

      if (withAuth && AuthStore.isLogin()) {
        header['Authorization'] = `Bearer ${AuthStore.getToken()}`;
      }

      const options: http.HttpRequestOptions = {
        method: method,
        header: header,
        expectDataType: http.HttpDataType.STRING,
        connectTimeout: CONNECT_TIMEOUT,
        readTimeout: READ_TIMEOUT
      };

      if (body !== undefined) {
        options.extraData = body;
      }

      const request: http.HttpRequest = http.createHttp();
      request.request(url, options, (error: BusinessError, response: http.HttpResponse) => {
        request.destroy();

        if (error) {
          reject(new ApiError(ERROR_CODE_NETWORK, `网络请求失败（${error.code}）：请检查服务地址与网络`));
          return;
        }

        const status: number = response.responseCode;

        // Spring Security 直接返回的 401/403，未包 Result
        if (status === 401 || status === 403) {
          AuthStore.notifyUnauthorized();
          reject(new ApiError(status, '登录状态已失效，请重新登录'));
          return;
        }

        const raw: string = response.result === undefined ? '' : response.result as string;

        let payload: Result<T> | undefined = undefined;
        try {
          payload = JSON.parse(raw) as Result<T>;
        } catch (parseError) {
          reject(new ApiError(status, `服务返回无法解析（HTTP ${status}）`));
          return;
        }

        if (payload === undefined || payload === null) {
          reject(new ApiError(status, '服务返回为空'));
          return;
        }

        const code: number = payload.code === undefined ? status : payload.code;

        if (code === RESULT_SUCCESS_CODE) {
          resolve(payload.data as T);
          return;
        }

        if (code === 401 || code === 403) {
          AuthStore.notifyUnauthorized();
        }

        reject(new ApiError(code, payload.message === undefined || payload.message.length === 0 ?
        '请求失败' : payload.message));
      });
    });
  }
}
