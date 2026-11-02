import { AppRouter } from '../router/AppRouter';
import { RouteNames } from '../router/RouteNames';
import { AuthStore } from '../store/AuthStore';
import { AuthApi } from '../api/AuthApi';
import { showToast } from '../utils/ToastUtil';
import { ROLE_ADMIN } from '../model/User';

/**
 * 顶部导航，等价 Web 端 components/layout/NavHeader.vue。
 * 差异：管理后台入口从 /admin/statistics 改为 /admin/users（迁移方案 3.4）。
 */
@Component
export struct NavHeader {
  @StorageLink('isLogin') isLogin: boolean = false;
  @StorageLink('isAdmin') isAdmin: boolean = false;
  @StorageLink('userNickname') nickname: string = '';

  build() {
    Column() {
      Row() {
        Image($r('app.media.ic_brand'))
          .width(36)
          .height(36)
          .borderRadius(12)

        Column() {
          Text('简坛论坛')
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.text_primary'))
          Text('HarmonyOS 客户端')
            .fontSize(11)
            .fontColor($r('app.color.text_muted'))
        }
        .alignItems(HorizontalAlign.Start)
        .margin({ left: 10 })

        Blank()

        if (this.isLogin) {
          Column() {
            Text(this.nickname)
              .fontSize(13)
              .fontWeight(FontWeight.Bold)
              .fontColor($r('app.color.text_primary'))
            Text(this.isAdmin ? ROLE_ADMIN : 'USER')
              .fontSize(10)
              .fontColor($r('app.color.text_muted'))
          }
          .alignItems(HorizontalAlign.End)
          .margin({ right: 10 })

          Button('退出')
            .fontSize(12)
            .height(30)
            .backgroundColor($r('app.color.accent_soft'))
            .fontColor($r('app.color.accent_strong'))
            .onClick(() => {
              this.handleLogout();
            })
        } else {
          Button('登录')
            .fontSize(12)
            .height(30)
            .backgroundColor($r('app.color.accent_soft'))
            .fontColor($r('app.color.accent_strong'))
            .onClick(() => {
              AppRouter.push(RouteNames.Login);
            })

          Button('注册')
            .fontSize(12)
            .height(30)
            .margin({ left: 8 })
            .backgroundColor($r('app.color.accent'))
            .fontColor(Color.White)
            .onClick(() => {
              AppRouter.push(RouteNames.Register);
            })
        }
      }
      .width('100%')
      .padding({ left: 16, right: 16, top: 12, bottom: 10 })

      Scroll() {
        Row() {
          this.NavChip('首页', RouteNames.Home)
          if (this.isLogin) {
            this.NavChip('发帖', RouteNames.PostCreate)
            this.NavChip('个人中心', RouteNames.MeProfile)
            this.NavChip('我的帖子', RouteNames.MePosts)
            this.NavChip('我的评论', RouteNames.MeComments)
          }
          if (this.isAdmin) {
            this.NavChip('管理后台', RouteNames.AdminUsers)
          }
          this.NavChip('服务地址', RouteNames.Settings)
        }
      }
      .scrollable(ScrollDirection.Horizontal)
      .scrollBar(BarState.Off)
      .width('100%')
      .padding({ left: 12, right: 12, bottom: 10 })
    }
    .width('100%')
    .backgroundColor($r('app.color.surface'))
    .border({ width: { bottom: 1 }, color: $r('app.color.border') })
  }

  @Builder
  NavChip(label: string, target: string) {
    Text(label)
      .fontSize(12)
      .fontColor($r('app.color.text_muted'))
      .padding({ left: 12, right: 12, top: 6, bottom: 6 })
      .margin({ right: 8 })
      .borderRadius(14)
      .backgroundColor($r('app.color.tag_bg'))
      .onClick(() => {
        if (target === RouteNames.Home) {
          AppRouter.backToHome();
          return;
        }
        AppRouter.push(target);
      })
  }

  private async handleLogout(): Promise<void> {
    try {
      await AuthApi.logout();
    } catch (error) {
      // 无状态 JWT 场景下，服务端失败不影响本地退出
    }
    await AuthStore.clearAuth();
    showToast('已退出登录');
    AppRouter.backToHome();
  }
}
