import { HttpClient } from './HttpClient';
import { LoginData, User } from '../model/User';

/**
 * 认证接口：对应 services/modules/auth.js
 * /auth/login、/auth/register 为 JSON body；/auth/logout 无参。
 */
export class AuthApi {
  static login(username: string, password: string): Promise<LoginData> {
    const body: User = new User();
    body.username = username;
    body.password = password;
    return HttpClient.postJson<LoginData>('/auth/login', body);
  }

  static register(username: string, nickname: string, password: string): Promise<User> {
    const body: User = new User();
    body.username = username;
    body.nickname = nickname;
    body.password = password;
    return HttpClient.postJson<User>('/auth/register', body);
  }

  static logout(): Promise<Object> {
    return HttpClient.post<Object>('/auth/logout');
  }
}
