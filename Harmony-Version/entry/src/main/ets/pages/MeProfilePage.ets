import { User } from '../model/User';
import { UserApi } from '../api/UserApi';
import { AuthStore } from '../store/AuthStore';
import { NavHeader } from '../components/NavHeader';
import { LoadingView } from '../components/StateViews';
import { showToast, resolveErrorMessage } from '../utils/ToastUtil';
import { safeText } from '../utils/TextUtil';

const MIN_PASSWORD_LENGTH: number = 6;

/**
 * 个人中心，等价 Web 端 pages/me/ProfilePage.vue。
 * 包含基本资料保存与修改密码两块。
 */
@Component
export struct MeProfilePage {
  @State loading: boolean = false;
  @State savingProfile: boolean = false;
  @State savingPassword: boolean = false;
  @State username: string = '';
  @State nickname: string = '';
  @State avatar: string = '';
  @State signature: string = '';
  @State oldPassword: string = '';
  @State newPassword: string = '';
  @State confirmPassword: string = '';

  aboutToAppear(): void {
    this.loadProfile();
  }

  build() {
    NavDestination() {
      Column() {
        NavHeader()

        if (this.loading) {
          LoadingView({ message: '正在加载个人资料...' })
        } else {
          Scroll() {
            Column() {
              Column() {
                Text('基本资料')
                  .fontSize(16)
                  .fontWeight(FontWeight.Bold)
                  .fontColor($r('app.color.text_primary'))
                  .width('100%')
                  .margin({ bottom: 14 })

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

                Text('头像地址')
                  .fontSize(13)
                  .fontColor($r('app.color.text_muted'))
                  .width('100%')
                  .margin({ top: 16 })
                TextInput({ text: $$this.avatar, placeholder: '请输入头像 URL' })
                  .width('100%')
                  .height(44)
                  .fontSize(15)
                  .margin({ top: 6 })
                  .backgroundColor($r('app.color.surface'))

                Text('个性签名')
                  .fontSize(13)
                  .fontColor($r('app.color.text_muted'))
                  .width('100%')
                  .margin({ top: 16 })
                TextArea({ text: $$this.signature, placeholder: '写一句介绍自己' })
                  .width('100%')
                  .height(90)
                  .fontSize(15)
                  .maxLength(200)
                  .margin({ top: 6 })
                  .backgroundColor($r('app.color.surface'))

                Button(this.savingProfile ? '保存中...' : '保存资料')
                  .width('100%')
                  .height(46)
                  .fontSize(16)
                  .margin({ top: 20 })
                  .backgroundColor($r('app.color.accent'))
                  .fontColor(Color.White)
                  .onClick(() => {
                    this.handleSaveProfile();
                  })
              }
              .width('100%')
              .alignItems(HorizontalAlign.Start)
              .padding(16)
              .borderRadius($r('app.float.panel_radius'))
              .backgroundColor($r('app.color.surface'))
              .border({ width: 1, color: $r('app.color.border') })

              Column() {
                Text('修改密码')
                  .fontSize(16)
                  .fontWeight(FontWeight.Bold)
                  .fontColor($r('app.color.text_primary'))
                  .width('100%')
                  .margin({ bottom: 14 })

                Text('旧密码')
                  .fontSize(13)
                  .fontColor($r('app.color.text_muted'))
                  .width('100%')
                TextInput({ text: $$this.oldPassword, placeholder: '请输入旧密码' })
                  .width('100%')
                  .height(44)
                  .fontSize(15)
                  .type(InputType.Password)
                  .margin({ top: 6 })
                  .backgroundColor($r('app.color.surface'))

                Text('新密码')
                  .fontSize(13)
                  .fontColor($r('app.color.text_muted'))
                  .width('100%')
                  .margin({ top: 16 })
                TextInput({ text: $$this.newPassword, placeholder: '请输入新密码' })
                  .width('100%')
                  .height(44)
                  .fontSize(15)
                  .type(InputType.Password)
                  .margin({ top: 6 })
                  .backgroundColor($r('app.color.surface'))

                Text('确认新密码')
                  .fontSize(13)
                  .fontColor($r('app.color.text_muted'))
                  .width('100%')
                  .margin({ top: 16 })
                TextInput({ text: $$this.confirmPassword, placeholder: '再次输入新密码' })
                  .width('100%')
                  .height(44)
                  .fontSize(15)
                  .type(InputType.Password)
                  .margin({ top: 6 })
                  .backgroundColor($r('app.color.surface'))

                Button(this.savingPassword ? '提交中...' : '修改密码')
                  .width('100%')
                  .height(46)
                  .fontSize(16)
                  .margin({ top: 20 })
                  .backgroundColor($r('app.color.accent'))
                  .fontColor(Color.White)
                  .onClick(() => {
                    this.handleSavePassword();
                  })
              }
              .width('100%')
              .alignItems(HorizontalAlign.Start)
              .padding(16)
              .margin({ top: 14 })
              .borderRadius($r('app.float.panel_radius'))
              .backgroundColor($r('app.color.surface'))
              .border({ width: 1, color: $r('app.color.border') })
            }
            .width('100%')
            .padding(16)
          }
          .layoutWeight(1)
          .scrollable(ScrollDirection.Vertical)
          .scrollBar(BarState.Off)
          .width('100%')
        }
      }
      .width('100%')
      .height('100%')
    }
    .title('个人资料')
    .backgroundColor($r('app.color.page_bg'))
  }

  private loadProfile(): void {
    this.loading = true;
    UserApi.getProfile()
      .then((profile: User) => {
        this.applyProfile(profile);
        this.loading = false;
      })
      .catch((error: Object) => {
        this.loading = false;
        showToast(resolveErrorMessage(error, '个人资料加载失败'));
      });
  }

  private applyProfile(profile: User): void {
    this.username = safeText(profile.username);
    this.nickname = safeText(profile.nickname);
    this.avatar = safeText(profile.avatar);
    this.signature = safeText(profile.signature);
    AuthStore.updateUser(profile).catch(() => undefined);
  }

  private handleSaveProfile(): void {
    if (this.username.trim().length === 0) {
      showToast('请输入用户名');
      return;
    }
    if (this.nickname.trim().length === 0) {
      showToast('请输入昵称');
      return;
    }

    this.savingProfile = true;
    UserApi.updateProfile(this.username.trim(), this.nickname.trim(), this.avatar.trim(),
      this.signature.trim())
      .then(() => {
        this.savingProfile = false;
        showToast('个人资料已更新');
        this.loadProfile();
      })
      .catch((error: Object) => {
        this.savingProfile = false;
        showToast(resolveErrorMessage(error, '更新资料失败'));
      });
  }

  private handleSavePassword(): void {
    if (this.oldPassword.length === 0) {
      showToast('请输入旧密码');
      return;
    }
    if (this.newPassword.length < MIN_PASSWORD_LENGTH) {
      showToast(`新密码长度至少 ${MIN_PASSWORD_LENGTH} 位`);
      return;
    }
    if (this.confirmPassword.length === 0) {
      showToast('请再次输入新密码');
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      showToast('两次输入的新密码不一致');
      return;
    }

    this.savingPassword = true;
    UserApi.updatePassword(this.oldPassword, this.newPassword)
      .then(() => {
        this.savingPassword = false;
        this.oldPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
        showToast('密码修改成功，请重新登录');
      })
      .catch((error: Object) => {
        this.savingPassword = false;
        showToast(resolveErrorMessage(error, '修改密码失败'));
      });
  }
}
