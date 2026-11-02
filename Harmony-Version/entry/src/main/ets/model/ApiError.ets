/**
 * 统一业务/网络错误载体。
 * 不使用 throw（ArkTS 对 throw 表达式有类型约束），统一通过 Promise.reject 抛出。
 */
export class ApiError {
  code: number;
  message: string;

  constructor(code: number, message: string) {
    this.code = code;
    this.message = message;
  }
}

export const ERROR_CODE_UNAUTHORIZED: number = 401;
export const ERROR_CODE_FORBIDDEN: number = 403;
export const ERROR_CODE_NETWORK: number = -1;
