import { UIContext } from '@ohos.arkui.UIContext';

/**
 * 全局 UIContext 持有器。
 * 由根页面 Index 在 aboutToAppear 时注入，供 ToastUtil 等非组件作用域的工具函数
 * 调用 UIContext 版本 API（全局 promptAction / AlertDialog 自 API 18 起废弃）。
 */
let globalUIContext: UIContext | undefined = undefined;

export function setGlobalUIContext(context: UIContext): void {
  globalUIContext = context;
}

export function getGlobalUIContext(): UIContext | undefined {
  return globalUIContext;
}
