import UIAbility from '@ohos.app.ability.UIAbility';
import AbilityConstant from '@ohos.app.ability.AbilityConstant';
import Want from '@ohos.app.ability.Want';
import window from '@ohos.window';
import { BusinessError } from '@ohos.base';
import hilog from '@ohos.hilog';
import { AppContext } from '../store/AppContext';
import { AuthStore } from '../store/AuthStore';
import { SettingsStore } from '../store/SettingsStore';
import { AppRouter } from '../router/AppRouter';
import { RouteNames } from '../router/RouteNames';

const TAG: string = 'JianTanAbility';
const DOMAIN: number = 0xFF00;

/**
 * 唯一 Ability：注入全局 Context、恢复会话与后端地址、加载页面栈。
 * 对应 Web 端 main.js 的初始化与 router 的 hydrate 时机。
 */
export default class EntryAbility extends UIAbility {
  onCreate(want: Want, launchParam: AbilityConstant.LaunchParam): void {
    AppContext.set(this.context);

    // 401/403 统一处理：清会话并跳登录页
    AuthStore.registerUnauthorizedHandler(() => {
      AppRouter.push(RouteNames.Login);
    });

    AuthStore.hydrate().catch((error: Object) => {
      hilog.error(DOMAIN, TAG, `hydrate failed: ${JSON.stringify(error)}`);
    });

    SettingsStore.load().catch((error: Object) => {
      hilog.error(DOMAIN, TAG, `baseUrl load failed: ${JSON.stringify(error)}`);
    });
  }

  onWindowStageCreate(windowStage: window.WindowStage): void {
    windowStage.loadContent('pages/Index', (error: BusinessError) => {
      if (error.code) {
        hilog.error(DOMAIN, TAG, `loadContent failed: ${JSON.stringify(error)}`);
        return;
      }
      hilog.info(DOMAIN, TAG, 'loadContent succeeded');
    });
  }

  onDestroy(): void {
    hilog.info(DOMAIN, TAG, 'onDestroy');
  }

  onWindowStageDestroy(): void {
    hilog.info(DOMAIN, TAG, 'onWindowStageDestroy');
  }

  onForeground(): void {
    hilog.info(DOMAIN, TAG, 'onForeground');
  }

  onBackground(): void {
    hilog.info(DOMAIN, TAG, 'onBackground');
  }
}
