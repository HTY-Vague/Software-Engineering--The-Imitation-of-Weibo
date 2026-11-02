import promptAction from '@ohos.promptAction';
import { ApiError } from '../model/ApiError';

/**
 * 轻量提示封装，替代 Web 端的 ElMessage。
 */
export function showToast(message: string): void {
  try {
    promptAction.showToast({
      message: message,
      duration: 2000
    });
  } catch (error) {
    // 非 UI 线程或上下文不可用时静默忽略，不阻断业务流程
  }
}

/** 从任意错误对象中解析可展示的文案（兼容 ApiError 与系统 BusinessError） */
export function resolveErrorMessage(error: Object, fallback: string = '请求失败'): string {
  if (error === undefined || error === null) {
    return fallback;
  }

  const apiError: ApiError = error as ApiError;
  if (apiError.message !== undefined && apiError.message !== null && apiError.message.length > 0) {
    return apiError.message;
  }

  return fallback;
}
