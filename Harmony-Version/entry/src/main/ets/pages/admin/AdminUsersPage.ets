import { User, STATUS_ENABLED } from '../../model/User';
import { AdminApi } from '../../api/AdminApi';
import { NavHeader } from '../../components/NavHeader';
import { LoadingView, EmptyView } from '../../components/StateViews';
import { showToast, resolveErrorMessage } from '../../utils/ToastUtil';
import { safeNumber, safeText } from '../../utils/TextUtil';

/**
 * 用户管理，等价 Web 端 pages/admin/UsersPage.vue。
 * 手机端用卡片列表替代 el-table。
 */
@Component
export struct AdminUsersPage {
  @State users: Array<User> = [];
  @State loading: boolean = false;

  aboutToAppear(): void {
    this.loadUsers();
  }

  build() {
    NavDestination() {
      Column() {
        NavHeader()

        Row() {
          Text('用户管理')
            .fontSize(18)
            .fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.text_primary'))
          Blank()
          Button('刷新')
            .fontSize(12)
            .height(32)
            .backgroundColor($r('app.color.accent'))
            .fontColor(Color.White)
            .onClick(() => {
              this.loadUsers();
            })
        }
        .width('100%')
        .padding({ left: 16, right: 16, top: 8, bottom: 8 })

        if (this.loading) {
          LoadingView({ message: '正在加载用户列表...' })
        } else if (this.users.length === 0) {
          EmptyView({ message: '暂无用户' })
        } else {
          Scroll() {
            Column() {
              ForEach(this.users, (user: User) => {
                Column() {
                  Row() {
                    Text(`#${safeNumber(user.userId)}`)
                      .fontSize(11)
                      .fontColor($r('app.color.text_muted'))
                    Blank()
                    this.StatusTag(user.status)
                  }
                  .width('100%')

                  Text(safeText(user.username, '未命名用户'))
                    .fontSize(15)
                    .fontWeight(FontWeight.Bold)
                    .fontColor($r('app.color.text_primary'))
                    .width('100%')
                    .margin({ top: 6 })

                  Text(`昵称：${safeText(user.nickname, '-')} · 角色：${safeText(user.role, '-')}`)
                    .fontSize(12)
                    .fontColor($r('app.color.text_muted'))
                    .width('100%')
                    .margin({ top: 4 })

                  Text(`注册时间：${safeText(user.createTime, '-')}`)
                    .fontSize(11)
                    .fontColor($r('app.color.text_muted'))
                    .width('100%')
                    .margin({ top: 4 })

                  Row() {
                    Blank()
                    Button(this.isEnabled(user.status) ? '禁用' : '启用')
                      .fontSize(12)
                      .height(32)
                      .backgroundColor(this.isEnabled(user.status) ?
                        $r('app.color.accent_soft') : $r('app.color.tag_bg'))
                      .fontColor(this.isEnabled(user.status) ?
                        $r('app.color.danger') : $r('app.color.success'))
                      .onClick(() => {
                        this.handleToggleStatus(user);
                      })
                  }
                  .width('100%')
                  .margin({ top: 10 })
                }
                .width('100%')
                .alignItems(HorizontalAlign.Start)
                .padding(14)
                .margin({ bottom: 12 })
                .borderRadius($r('app.float.card_radius'))
                .backgroundColor($r('app.color.surface'))
                .border({ width: 1, color: $r('app.color.border') })
              }, (user: User) => `${user.userId}`)

              Text(`共 ${this.users.length} 位用户`)
                .fontSize(12)
                .fontColor($r('app.color.text_muted'))
                .width('100%')
                .textAlign(TextAlign.Center)
                .margin({ top: 8, bottom: 8 })
            }
            .width('100%')
            .padding({ left: 16, right: 16, bottom: 24 })
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
    .title('用户管理')
    .backgroundColor($r('app.color.page_bg'))
  }

  private isEnabled(status: string): boolean {
    return safeText(status) === STATUS_ENABLED;
  }

  @Builder
  StatusTag(status: string) {
    Text(this.isEnabled(status) ? '正常' : '禁用')
      .fontSize(11)
      .fontColor(this.isEnabled(status) ? $r('app.color.success') : $r('app.color.danger'))
      .backgroundColor($r('app.color.tag_bg'))
      .padding({ left: 8, right: 8, top: 3, bottom: 3 })
      .borderRadius(10)
  }

  private loadUsers(): void {
    this.loading = true;
    AdminApi.getUsers()
      .then((list: Array<User>) => {
        this.users = list === undefined || list === null ? [] : list;
        this.loading = false;
      })
      .catch((error: Object) => {
        this.users = [];
        this.loading = false;
        showToast(resolveErrorMessage(error, '用户列表加载失败'));
      });
  }

  private handleToggleStatus(user: User): void {
    const nextStatus: number = this.isEnabled(user.status) ? 0 : 1;
    AdminApi.updateUserStatus(safeNumber(user.userId), nextStatus)
      .then(() => {
        showToast('用户状态已更新');
        this.loadUsers();
      })
      .catch((error: Object) => {
        showToast(resolveErrorMessage(error, '更新用户状态失败'));
      });
  }
}
