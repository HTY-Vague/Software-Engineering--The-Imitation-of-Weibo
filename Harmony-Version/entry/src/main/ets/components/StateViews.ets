/**
 * 通用状态视图：加载中 / 空态 / 错误兜底。
 * Web 端这些由各个页面内联 .state 样式承担，此处抽为公共组件。
 */

@Component
export struct LoadingView {
  @Prop message: string = '正在加载...';

  build() {
    Column() {
      LoadingProgress()
        .width(28)
        .height(28)
        .color($r('app.color.accent'))
      Text(this.message)
        .fontSize(13)
        .fontColor($r('app.color.text_muted'))
        .margin({ top: 10 })
    }
    .width('100%')
    .padding(24)
    .justifyContent(FlexAlign.Center)
  }
}

@Component
export struct EmptyView {
  @Prop message: string = '暂无数据';

  build() {
    Column() {
      Text(this.message)
        .fontSize(13)
        .fontColor($r('app.color.text_muted'))
    }
    .width('100%')
    .padding(24)
    .borderRadius($r('app.float.card_radius'))
    .backgroundColor($r('app.color.surface'))
    .border({ width: 1, color: $r('app.color.border') })
  }
}
