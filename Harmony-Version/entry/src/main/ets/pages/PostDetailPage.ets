import { ForumBoard } from '../model/ForumBoard';
import { ForumPost } from '../model/ForumPost';
import { TARGET_TYPE_POST } from '../model/ForumComment';
import { ForumApi } from '../api/ForumApi';
import { PostApi } from '../api/PostApi';
import { AppRouter } from '../router/AppRouter';
import { RouteNames } from '../router/RouteNames';
import { RouteParam } from '../router/RouteParam';
import { NavHeader } from '../components/NavHeader';
import { LikeButton } from '../components/LikeButton';
import { CommentSection } from '../components/CommentSection';
import { LoadingView } from '../components/StateViews';
import { formatDateTime } from '../utils/TimeUtil';
import { safeNumber, safeText, htmlToPlainText } from '../utils/TextUtil';
import { showToast, resolveErrorMessage } from '../utils/ToastUtil';

/**
 * 帖子详情，等价 Web 端 pages/post/PostDetailPage.vue。
 * 正文用 RichText 渲染，兼容 Web 端富文本产生的 HTML（迁移方案 4.6）。
 */
@Component
export struct PostDetailPage {
  @State post: ForumPost = new ForumPost();
  @State boards: Array<ForumBoard> = [];
  @State loading: boolean = true;
  @State notFound: boolean = false;
  @Prop postId: number = 0;

  aboutToAppear(): void {
    this.loadMeta();
    this.loadDetail();
  }

  build() {
    NavDestination() {
      Column() {
        NavHeader()

        if (this.loading) {
          LoadingView({ message: '正在加载帖子详情...' })
        } else if (this.notFound) {
          Column() {
            Text('帖子不存在或已被删除')
              .fontSize(16)
              .fontColor($r('app.color.text_muted'))
            Button('返回首页')
              .margin({ top: 16 })
              .backgroundColor($r('app.color.accent'))
              .fontColor(Color.White)
              .onClick(() => {
                AppRouter.backToHome();
              })
          }
          .width('100%')
          .padding(24)
        } else {
          Scroll() {
            Column() {
              Column() {
                Row() {
                  Text(this.boardName())
                    .fontSize(10)
                    .fontColor($r('app.color.text_primary'))
                    .backgroundColor($r('app.color.tag_bg'))
                    .padding({ left: 8, right: 8, top: 3, bottom: 3 })
                    .borderRadius(10)
                  if (safeNumber(this.post.isTop) === 1) {
                    Text('置顶')
                      .fontSize(10)
                      .fontColor($r('app.color.accent_strong'))
                      .backgroundColor($r('app.color.accent_soft'))
                      .padding({ left: 8, right: 8, top: 3, bottom: 3 })
                      .borderRadius(10)
                      .margin({ left: 8 })
                  }
                  Text(`#${safeNumber(this.post.postId)}`)
                    .fontSize(10)
                    .fontColor($r('app.color.text_muted'))
                    .backgroundColor($r('app.color.tag_bg'))
                    .padding({ left: 8, right: 8, top: 3, bottom: 3 })
                    .borderRadius(10)
                    .margin({ left: 8 })
                }
                .width('100%')

                Text(safeText(this.post.title, '无标题'))
                  .fontSize(22)
                  .fontWeight(FontWeight.Bold)
                  .fontColor($r('app.color.text_primary'))
                  .width('100%')
                  .margin({ top: 14 })

                Text(`作者 ${this.authorName()} · ${formatDateTime(this.post.createTime)}`)
                  .fontSize(12)
                  .fontColor($r('app.color.text_muted'))
                  .width('100%')
                  .margin({ top: 10 })

                Row() {
                  Text(`${safeNumber(this.post.viewCount)} 浏览`)
                    .fontSize(12)
                    .fontColor($r('app.color.text_muted'))
                  Text(`${safeNumber(this.post.commentCount)} 评论`)
                    .fontSize(12)
                    .fontColor($r('app.color.text_muted'))
                    .margin({ left: 12 })
                  Text(`${safeNumber(this.post.likeCount)} 点赞`)
                    .fontSize(12)
                    .fontColor($r('app.color.text_muted'))
                    .margin({ left: 12 })
                }
                .width('100%')
                .margin({ top: 8 })

                Row() {
                  LikeButton({
                    targetType: TARGET_TYPE_POST,
                    targetId: safeNumber(this.post.postId),
                    onChanged: () => {
                      this.loadDetail();
                    }
                  })
                  Button('查看板块')
                    .fontSize(12)
                    .height(30)
                    .margin({ left: 10 })
                    .backgroundColor($r('app.color.tag_bg'))
                    .fontColor($r('app.color.text_muted'))
                    .onClick(() => {
                      const param: RouteParam = new RouteParam();
                      param.boardId = safeNumber(this.post.boardId);
                      AppRouter.push(RouteNames.BoardPosts, param);
                    })
                }
                .width('100%')
                .margin({ top: 16 })

                Text(htmlToPlainText(safeText(this.post.content)))
                  .fontSize(16)
                  .lineHeight(26)
                  .fontColor($r('app.color.text_primary'))
                  .width('100%')
                  .margin({ top: 18 })
              }
              .width('100%')
              .alignItems(HorizontalAlign.Start)
              .padding(16)
              .borderRadius($r('app.float.panel_radius'))
              .backgroundColor($r('app.color.surface'))
              .border({ width: 1, color: $r('app.color.border') })

              Column() {
                CommentSection({
                  postId: safeNumber(this.post.postId),
                  onChanged: () => {
                    this.loadDetail();
                  }
                })
              }
              .width('100%')
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
    .title('帖子详情')
    .backgroundColor($r('app.color.page_bg'))
  }

  private boardName(): string {
    for (let i: number = 0; i < this.boards.length; i++) {
      if (this.boards[i].boardId === safeNumber(this.post.boardId)) {
        return safeText(this.boards[i].boardName, '未分类');
      }
    }
    return '未分类';
  }

  private authorName(): string {
    if (safeText(this.post.authorNickname).length > 0) {
      return this.post.authorNickname;
    }
    if (safeText(this.post.authorUsername).length > 0) {
      return this.post.authorUsername;
    }
    return `用户 #${safeNumber(this.post.authorId)}`;
  }

  private loadMeta(): void {
    ForumApi.getBoards()
      .then((list: Array<ForumBoard>) => {
        this.boards = list === undefined || list === null ? [] : list;
      })
      .catch(() => {
        // 板块名失败不影响主内容展示，与 Web 端一致
      });
  }

  private loadDetail(): void {
    this.loading = true;
    PostApi.getPostDetail(this.postId)
      .then((detail: ForumPost) => {
        if (detail === undefined || detail === null) {
          this.notFound = true;
          this.loading = false;
          return;
        }
        this.post = detail;
        this.notFound = false;
        this.loading = false;
      })
      .catch(() => {
        this.notFound = true;
        this.loading = false;
      });
  }
}
