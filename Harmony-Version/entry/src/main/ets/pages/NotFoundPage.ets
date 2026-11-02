import { AppRouter } from '../router/AppRouter';

/**
 * 404 兜底页，等价 Web 端 pages/home/HomeNotFound.vue。
 * Web 端用 /:pathMatch(.*)* 捕获，App 端由 Index 的页面栈兜底分支承载。
 */
@Component
export struct NotFoundPage {
  build() {
    NavDestination() {
      Column() {
        Text('Page not found')
          .fontSize(22)
          .fontWeight(FontWeight.Bold)
          .fontColor($r('app.color.text_primary'))
        Text('The page you are looking for does not exist.')
          .fontSize(13)
          .fontColor($r('app.color.text_muted'))
          .margin({ top: 10 })
          .textAlign(TextAlign.Center)
        Button('Back to home')
          .margin({ top: 20 })
          .backgroundColor($r('app.color.accent'))
          .fontColor(Color.White)
          .onClick(() => {
            AppRouter.backToHome();
          })
      }
      .width('100%')
      .height('100%')
      .justifyContent(FlexAlign.Center)
      .padding(24)
    }
    .title('页面不存在')
    .backgroundColor($r('app.color.page_bg'))
  }
}
