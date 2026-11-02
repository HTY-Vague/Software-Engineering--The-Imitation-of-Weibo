/**
 * 后端统一响应结构：{ code, message, data }
 * 对应后端 ResultUtil.Result，成功码为 200。
 */
export interface Result<T> {
  code: number;
  message: string;
  data: T | null;
}

export const RESULT_SUCCESS_CODE: number = 200;
