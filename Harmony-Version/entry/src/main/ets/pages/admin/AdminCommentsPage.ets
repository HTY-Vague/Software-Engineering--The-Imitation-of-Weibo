import { ForumComment } from '../../model/ForumComment';
import { AdminApi } from '../../api/AdminApi';
import { AppRouter } from '../../router/AppRouter';
import { RouteNames } from '../../router/RouteNames';
import { RouteParam } from '../../router/RouteParam';
import { NavHeader } from '../../components/NavHeader';
import { LoadingView, EmptyView } from '../../components/StateViews';
import { formatDateTime } from '../../utils/TimeUtil';
import { safeNumber, safeText } from '../../utils/TextUtil';
import { showToast, resolveErrorMessage } from '../../utils/ToastUtil';

/**
 * 评论管理，等价 Web 端 pages/admin/CommentsPage.vue。
 */
@Component
export struct AdminCommentsPage {
  @State comments: Array<ForumComment> = [];
  @State loading: boolean = false;

  aboutToAppear(): void {
    this.loadComments();
  }

  build() {
    NavDestination() {
      Column() {
        NavHeader()

        Row() {
          Text('评论管理')
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
              this.loadComments();
            })
        }
        .width('100%')
        .padding({ left: 16, right: 16, top: 8, bottom: 8 })

        if (this.loading) {
          LoadingView({ message: '正在加载评论列表...' })
        } else if (this.comments.length === 0) {
          EmptyView({ message: '暂无评论' })
        } else {
          Scroll() {
            Column() {
              ForEach(this.comments, (comment: ForumComment) => {
                Column() {
                  Row() {
                    Column() {
                      Text(`评论 #${safeNumber(comment.commentId)} · 帖子 #${safeNumber(comment.postId)}`)
                        .fontSize(14)
                        .fontWeight(FontWeight.Bold)
                        .fontColor($r('app.color.text_primary'))
                      Text(`${formatDateTime(comment.createTime)} · 用户 #${safeNumber(comment.userId)}`)
                        .fontSize(11)
                        .fontColor($r('app.color.text_muted'))
                        .margin({ top: 2 })
                    }
                    .alignItems(HorizontalAlign.Start)
                    .layoutWeight(1)

                    Text(`${safeNumber(comment.likeCount)} 赞`)
                      .fontSize(11)
                      .fontColor($r('app.color.text_muted'))
                      .backgroundColor($r('app.color.tag_bg'))
                      .padding({ left: 8, right: 8, top: 3, bottom: 3 })
                      .borderRadius(10)
                  }
                  .width('100%')

                  Text(safeText(comment.content))
                    .fontSize(14)
                    .fontColor($r('app.color.text_primary'))
                    .width('100%')
                    .margin({ top: 10 })
                    .lineHeight(21)

                  Row() {
                    Blank()
                    Button('查看原帖')
                      .fontSize(12)
                      .height(32)
                      .backgroundColor($r('app.color.tag_bg'))
                      .fontColor($r('app.color.text_muted'))
                      .onClick(() => {
                        const param: RouteParam = new RouteParam();
                        param.postId = safeNumber(comment.postId);
                        AppRouter.push(RouteNames.PostDetail, param);
                      })
                    Button('删除')
                      .fontSize(12)
                      .height(32)
                      .margin({ left: 10 })
                      .backgroundColor($r('app.color.accent_soft'))
                      .fontColor($r('app.color.danger'))
                      .onClick(() => {
                        this.confirmDelete(comment);
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
              }, (comment: ForumComment) => `${comment.commentId}`)

              Text(`共 ${this.comments.length} 条评论`)
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
    .title('评论管理')
    .backgroundColor($r('app.color.page_bg'))
  }

  private loadComments(): void {
    this.loading = true;
    AdminApi.getComments()
      .then((list: Array<ForumComment>) => {
        this.comments = list === undefined || list === null ? [] : list;
        this.loading = false;
      })
      .catch((error: Object) => {
        this.comments = [];
        this.loading = false;
        showToast(resolveErrorMessage(error, '评论列表加载失败'));
      });
  }

  private confirmDelete(comment: ForumComment): void {
    this.getUIContext().showAlertDialog({
      title: '删除评论',
      message: `确认删除评论 #${safeNumber(comment.commentId)} 吗？`,
      primaryButton: {
        value: '取消',
        action: () => {
        }
      },
      secondaryButton: {
        value: '删除',
        fontColor: $r('app.color.danger'),
        action: () => {
          AdminApi.deleteComment(safeNumber(comment.commentId))
            .then(() => {
              showToast('评论已删除');
              this.loadComments();
            })
            .catch((error: Object) => {
              showToast(resolveErrorMessage(error, '删除失败'));
            });
        }
      }
    });
  }
}
