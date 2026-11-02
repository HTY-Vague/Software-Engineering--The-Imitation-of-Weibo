import common from '@ohos.app.ability.common';

/**
 * 全局 UIAbilityContext 持有者。
 * EntryAbility.onCreate 中注入，供 Preferences 等非组件场景使用。
 */
export class AppContext {
  private static context: common.UIAbilityContext | undefined = undefined;

  static set(context: common.UIAbilityContext): void {
    AppContext.context = context;
  }

  static get(): common.UIAbilityContext {
    return AppContext.context as common.UIAbilityContext;
  }
}
