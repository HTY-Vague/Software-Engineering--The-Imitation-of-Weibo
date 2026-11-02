import { AuthApi } from '../api/AuthApi';
import { LoginData } from '../model/User';
import { AuthStore } from '../store/AuthStore';
import { AppRouter } from '../router/AppRouter';
import { RouteNames } from '../router/RouteNames';
import { RouteParam } from '../router/RouteParam';
import { showToast, resolveErrorMessage } from '../utils/ToastUtil';

/**
 * 登录页，等价 Web 端 pages/auth/LoginPage.vue。
 * 支持登录后按 redirect 回跳到被守卫拦截前的目标页面。
 */
@Component
export struct LoginPage {
  @State username: string = '';
  @State password: string = '';
  @State loading: boolean = false;
  @Prop param: RouteParam = new RouteParam();

  build() {
    NavDestination() {
      Scroll() {
        Column() {
          Text('登录后继续浏览、发帖与互动')
            .fontSize(22)
            .fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.text_primary'))
            .width('100%')
          Text('统一接入后端 JWT 认证，登录后即可访问个人中心与管理员后台。')
            .fontSize(13)
            .fontColor($r('app.color.text_muted'))
            .width('100%')
            .margin({ top: 10 })
            .lineHeight(20)

          Column() {
            Text('用户名')
              .fontSize(13)
              .fontColor($r('app.color.text_muted'))
              .width('100%')
            TextInput({ text: $$this.username, placeholder: '请输入用户名' })
              .width('100%')
              .height(44)
              .fontSize(15)
              .margin({ top: 6 })
              .backgroundColor($r('app.color.surface'))

            Text('密码')
              .fontSize(13)
              .fontColor($r('app.color.text_muted'))
              .width('100%')
              .margin({ top: 16 })
            TextInput({ text: $$this.password, placeholder: '请输入密码' })
              .width('100%')
              .height(44)
              .fontSize(15)
              .type(InputType.Password)
              .margin({ top: 6 })
              .backgroundColor($r('app.color.surface'))

            Button(this.loading ? '登录中...' : '登录')
              .width('100%')
              .height(46)
              .fontSize(16)
              .margin({ top: 24 })
              .backgroundColor($r('app.color.accent'))
              .fontColor(Color.White)
              .onClick(() => {
                this.handleSubmit();
              })

            Row() {
              Text('还没有账号？')
                .fontSize(13)
                .fontColor($r('app.color.text_muted'))
              Text('去注册')
                .fontSize(13)
                .fontColor($r('app.color.accent'))
                .margin({ left: 6 })
                .onClick(() => {
                  AppRouter.push(RouteNames.Register);
                })
            }
            .width('100%')
            .justifyContent(FlexAlign.Center)
            .margin({ top: 18 })
          }
          .width('100%')
          .padding(20)
          .margin({ top: 20 })
          .borderRadius($r('app.float.panel_radius'))
          .backgroundColor($r('app.color.surface'))
          .border({ width: 1, color: $r('app.color.border') })
        }
        .width('100%')
        .padding(20)
        .alignItems(HorizontalAlign.Start)
      }
      .scrollable(ScrollDirection.Vertical)
      .scrollBar(BarState.Off)
      .width('100%')
      .layoutWeight(1)
    }
    .title('登录')
    .backgroundColor($r('app.color.page_bg'))
  }

  private handleSubmit(): void {
    if (this.username.trim().length === 0) {
      showToast('请输入用户名');
      return;
    }
    if (this.password.length === 0) {
      showToast('请输入密码');
      return;
    }

    this.loading = true;
    AuthApi.login(this.username.trim(), this.password)
      .then((data: LoginData) => {
        return AuthStore.setAuth(data);
      })
      .then(() => {
        this.loading = false;
        showToast('登录成功');
        this.navigateAfterLogin();
      })
      .catch((error: Object) => {
        this.loading = false;
        showToast(resolveErrorMessage(error, '登录失败，请检查账号密码'));
      });
  }

  aboutToAppear(): void {
    // 注册成功后跳转过来时预填用户名，对齐 Web 端 LoginPage 的 route.query.username
    if (this.param.username.length > 0) {
      this.username = this.param.username;
    }
  }

  private navigateAfterLogin(): void {
    if (this.param.redirect.length > 0) {
      const target: RouteParam = new RouteParam();
      target.postId = this.param.redirectPostId;
      AppRouter.replace(this.param.redirect, target);
      return;
    }
    AppRouter.backToHome();
  }
}
