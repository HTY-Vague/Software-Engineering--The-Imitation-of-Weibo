import { AuthApi } from '../api/AuthApi';
import { AppRouter } from '../router/AppRouter';
import { RouteNames } from '../router/RouteNames';
import { RouteParam } from '../router/RouteParam';
import { showToast, resolveErrorMessage } from '../utils/ToastUtil';

const MIN_PASSWORD_LENGTH: number = 6;

/**
 * 注册页，等价 Web 端 pages/auth/RegisterPage.vue。
 * 校验规则与 Web 端一致：用户名/昵称必填，密码至少 6 位，两次输入一致。
 */
@Component
export struct RegisterPage {
  @State username: string = '';
  @State nickname: string = '';
  @State password: string = '';
  @State confirmPassword: string = '';
  @State loading: boolean = false;

  build() {
    NavDestination() {
      Scroll() {
        Column() {
          Text('注册一个账号，参与发帖、评论和点赞')
            .fontSize(22)
            .fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.text_primary'))
            .width('100%')
          Text('注册后即可使用个人中心、发帖编辑与评论互动能力。')
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

            Text('昵称')
              .fontSize(13)
              .fontColor($r('app.color.text_muted'))
              .width('100%')
              .margin({ top: 16 })
            TextInput({ text: $$this.nickname, placeholder: '请输入昵称' })
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

            Text('确认密码')
              .fontSize(13)
              .fontColor($r('app.color.text_muted'))
              .width('100%')
              .margin({ top: 16 })
            TextInput({ text: $$this.confirmPassword, placeholder: '再次输入密码' })
              .width('100%')
              .height(44)
              .fontSize(15)
              .type(InputType.Password)
              .margin({ top: 6 })
              .backgroundColor($r('app.color.surface'))

            Button(this.loading ? '提交中...' : '注册')
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
              Text('已有账号？')
                .fontSize(13)
                .fontColor($r('app.color.text_muted'))
              Text('去登录')
                .fontSize(13)
                .fontColor($r('app.color.accent'))
                .margin({ left: 6 })
                .onClick(() => {
                  AppRouter.push(RouteNames.Login);
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
    .title('注册')
    .backgroundColor($r('app.color.page_bg'))
  }

  private handleSubmit(): void {
    if (this.username.trim().length === 0) {
      showToast('请输入用户名');
      return;
    }
    if (this.nickname.trim().length === 0) {
      showToast('请输入昵称');
      return;
    }
    if (this.password.length < MIN_PASSWORD_LENGTH) {
      showToast(`密码长度至少 ${MIN_PASSWORD_LENGTH} 位`);
      return;
    }
    if (this.confirmPassword.length === 0) {
      showToast('请再次输入密码');
      return;
    }
    if (this.password !== this.confirmPassword) {
      showToast('两次输入的密码不一致');
      return;
    }

    this.loading = true;
    AuthApi.register(this.username.trim(), this.nickname.trim(), this.password)
      .then(() => {
        this.loading = false;
        showToast('注册成功，请登录');
        const param: RouteParam = new RouteParam();
        param.username = this.username.trim();
        AppRouter.replace(RouteNames.Login, param);
      })
      .catch((error: Object) => {
        this.loading = false;
        showToast(resolveErrorMessage(error, '注册失败，请检查填写内容'));
      });
  }
}
