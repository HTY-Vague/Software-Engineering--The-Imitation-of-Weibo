import { ForumPost } from '../model/ForumPost';
import { AppRouter } from '../router/AppRouter';
import { RouteNames } from '../router/RouteNames';
import { RouteParam } from '../router/RouteParam';
import { formatDateTime } from '../utils/TimeUtil';
import { buildPreview, safeNumber, safeText } from '../utils/TextUtil';

/**
 * 帖子卡片，等价 Web 端 components/post/PostCard.vue。
 */
@Component
export struct PostCard {
  @Prop post: ForumPost = new ForumPost();
  @Prop boardName: string = '';

  build() {
    Column() {
      Row() {
        if (safeNumber(this.post.isTop) === 1) {
          Text('置顶')
            .fontSize(10)
            .fontColor($r('app.color.accent_strong'))
            .backgroundColor($r('app.color.accent_soft'))
            .padding({ left: 8, right: 8, top: 3, bottom: 3 })
            .borderRadius(10)
        }
        if (this.boardName.length > 0) {
          Text(this.boardName)
            .fontSize(10)
            .fontColor($r('app.color.text_primary'))
            .backgroundColor($r('app.color.tag_bg'))
            .padding({ left: 8, right: 8, top: 3, bottom: 3 })
            .borderRadius(10)
        }
        Text(`#${safeNumber(this.post.postId)}`)
          .fontSize(10)
          .fontColor($r('app.color.text_muted'))
          .backgroundColor($r('app.color.tag_bg'))
          .padding({ left: 8, right: 8, top: 3, bottom: 3 })
          .borderRadius(10)
      }
      .width('100%')
      .margin({ bottom: 10 })

      Text(safeText(this.post.title, '无标题'))
        .fontSize(17)
        .fontWeight(FontWeight.Bold)
        .fontColor($r('app.color.text_primary'))
        .width('100%')
        .maxLines(2)
        .textOverflow({ overflow: TextOverflow.Ellipsis })
        .margin({ bottom: 8 })

      Text(buildPreview(this.post.content) === '' ? '暂无正文预览' : buildPreview(this.post.content))
        .fontSize(13)
        .fontColor($r('app.color.text_muted'))
        .width('100%')
        .maxLines(3)
        .textOverflow({ overflow: TextOverflow.Ellipsis })
        .lineHeight(20)
        .margin({ bottom: 12 })

      Row() {
        Text(formatDateTime(this.post.createTime))
          .fontSize(11)
          .fontColor($r('app.color.text_muted'))
        Blank()
        Text(`${safeNumber(this.post.viewCount)} 浏览`)
          .fontSize(11)
          .fontColor($r('app.color.text_muted'))
        Text(`${safeNumber(this.post.commentCount)} 评论`)
          .fontSize(11)
          .fontColor($r('app.color.text_muted'))
          .margin({ left: 10 })
        Text(`${safeNumber(this.post.likeCount)} 点赞`)
          .fontSize(11)
          .fontColor($r('app.color.text_muted'))
          .margin({ left: 10 })
      }
      .width('100%')
    }
    .width('100%')
    .alignItems(HorizontalAlign.Start)
    .padding(16)
    .borderRadius($r('app.float.card_radius'))
    .backgroundColor($r('app.color.surface'))
    .border({ width: 1, color: $r('app.color.border') })
    .onClick(() => {
      const param: RouteParam = new RouteParam();
      param.postId = this.post.postId;
      AppRouter.push(RouteNames.PostDetail, param);
    })
  }
}
