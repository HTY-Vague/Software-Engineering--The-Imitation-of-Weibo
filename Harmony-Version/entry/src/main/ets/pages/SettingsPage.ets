import { SettingsStore, DEFAULT_BASE_URL } from '../store/SettingsStore';
import { NavHeader } from '../components/NavHeader';
import { showToast } from '../utils/ToastUtil';

/**
 * 后端服务地址设置页。
 * 课程演示环境后端地址可能变化（云服务器 / 局域网 / 本机），
 * 提供运行时切换并持久化到 Preferences。
 */
@Component
export struct SettingsPage {
  @State baseUrl: string = DEFAULT_BASE_URL;
  @State saving: boolean = false;

  aboutToAppear(): void {
    this.baseUrl = SettingsStore.getBaseUrl();
  }

  build() {
    NavDestination() {
      Column() {
        NavHeader()

        Scroll() {
          Column() {
            Text('后端服务地址')
              .fontSize(18)
              .fontWeight(FontWeight.Bold)
              .fontColor($r('app.color.text_primary'))
              .width('100%')

            Text('客户端通过 HTTP 直连 Spring Boot 后端。真机演示请填写手机可访问的局域网或公网地址，模拟器可用 10.0.2.2 访问宿主机。')
              .fontSize(12)
              .fontColor($r('app.color.text_muted'))
              .width('100%')
              .margin({ top: 8 })
              .lineHeight(19)

            Text('服务地址')
              .fontSize(13)
              .fontColor($r('app.color.text_muted'))
              .width('100%')
              .margin({ top: 20 })
            TextInput({ text: $$this.baseUrl, placeholder: DEFAULT_BASE_URL })
              .width('100%')
              .height(44)
              .fontSize(15)
              .margin({ top: 6 })
              .backgroundColor($r('app.color.surface'))

            Button(this.saving ? '保存中...' : '保存')
              .width('100%')
              .height(46)
              .fontSize(16)
              .margin({ top: 20 })
              .backgroundColor($r('app.color.accent'))
              .fontColor(Color.White)
              .onClick(() => {
                this.handleSave();
              })

            Button('恢复默认')
              .width('100%')
              .height(46)
              .fontSize(16)
              .margin({ top: 12 })
              .backgroundColor($r('app.color.tag_bg'))
              .fontColor($r('app.color.text_muted'))
              .onClick(() => {
                this.baseUrl = DEFAULT_BASE_URL;
                this.handleSave();
              })

            Text(`当前生效：${SettingsStore.getBaseUrl()}`)
              .fontSize(11)
              .fontColor($r('app.color.text_muted'))
              .width('100%')
              .margin({ top: 16 })
          }
          .width('100%')
          .padding(16)
          .alignItems(HorizontalAlign.Start)
        }
        .layoutWeight(1)
        .scrollable(ScrollDirection.Vertical)
        .scrollBar(BarState.Off)
        .width('100%')
      }
      .width('100%')
      .height('100%')
    }
    .title('服务地址')
    .backgroundColor($r('app.color.page_bg'))
  }

  private handleSave(): void {
    this.saving = true;
    SettingsStore.save(this.baseUrl)
      .then((saved: string) => {
        this.saving = false;
        this.baseUrl = saved;
        showToast(`已保存：${saved}`);
      })
      .catch(() => {
        this.saving = false;
        showToast('保存失败');
      });
  }
}
