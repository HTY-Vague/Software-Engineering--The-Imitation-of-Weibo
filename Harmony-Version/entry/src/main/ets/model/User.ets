/**
 * 对应用户实体 SYS_USER / User.java
 * 后端 LocalDateTime 序列化为 "yyyy-MM-ddTHH:mm:ss" 字符串。
 */
export class User {
  userId: number = 0;
  username: string = '';
  password: string = '';
  nickname: string = '';
  avatar: string = '';
  signature: string = '';
  role: string = '';
  status: string = '';
  createTime: string = '';
  updateTime: string = '';
}

/** 登录接口 /auth/login 的 data 结构 */
export class LoginData {
  token: string = '';
  user: User = new User();
}

/** 角色常量，与后端保持一致 */
export const ROLE_ADMIN: string = 'ADMIN';
export const ROLE_USER: string = 'USER';

/** 状态常量：后端用字符串 "1"/"0" 表示正常/禁用 */
export const STATUS_ENABLED: string = '1';
export const STATUS_DISABLED: string = '0';
