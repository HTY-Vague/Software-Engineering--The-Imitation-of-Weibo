import { ForumComment } from '../model/ForumComment';
import { CommentApi } from '../api/CommentApi';
import { AuthStore } from '../store/AuthStore';
import { showToast, resolveErrorMessage } from '../utils/ToastUtil';
import { safeNumber } from '../utils/TextUtil';
import { CommentItem } from './CommentItem';

/**
 * 评论区，等价 Web 端 components/comment/CommentSection.vue。
 */
@Component
export struct CommentSection {
  @Prop postId: number = 0;
  @State comments: Array<ForumComment> = [];
  @State content: string = '';
  @State loading: boolean = false;
  @State submitting: boolean = false;
  @StorageLink('isLogin') isLogin: boolean = false;
  onChanged: () => void = () => {};

  aboutToAppear(): void {
    this.loadComments();
  }

  build() {
    Column() {
      Row() {
        Text('评论区')
          .fontSize(15)
          .fontWeight(FontWeight.Bold)
          .fontColor($r('app.color.text_primary'))
        Blank()
        Text(`${this.comments.length} 条评论`)
          .fontSize(12)
          .fontColor($r('app.color.text_muted'))
      }
      .width('100%')
      .margin({ bottom: 12 })

      if (this.isLogin) {
        Column() {
          TextArea({ text: $$this.content, placeholder: '写下你的评论...' })
            .width('100%')
            .height(100)
            .fontSize(14)
            .maxLength(500)
            .backgroundColor($r('app.color.surface'))

          Row() {
            Blank()
            Button('发表评论')
              .fontSize(13)
              .height(34)
              .backgroundColor($r('app.color.accent'))
              .fontColor(Color.White)
              .onClick(() => {
                this.handleSubmit();
              })
          }
          .width('100%')
          .margin({ top: 10 })
        }
        .width('100%')
        .padding(12)
        .borderRadius(12)
        .backgroundColor($r('app.color.page_bg'))
        .margin({ bottom: 12 })
      } else {
        Text('登录后才能发表评论')
          .fontSize(12)
          .fontColor($r('app.color.text_muted'))
          .width('100%')
          .padding(12)
          .borderRadius(12)
          .backgroundColor($r('app.color.accent_soft'))
          .margin({ bottom: 12 })
      }

      if (this.loading) {
        Text('正在加载评论...')
          .fontSize(13)
          .fontColor($r('app.color.text_muted'))
          .width('100%')
          .textAlign(TextAlign.Center)
          .padding(16)
      } else if (this.comments.length === 0) {
        Text('暂无评论，来抢沙发吧')
          .fontSize(13)
          .fontColor($r('app.color.text_muted'))
          .width('100%')
          .textAlign(TextAlign.Center)
          .padding(16)
      } else {
        ForEach(this.comments, (comment: ForumComment) => {
          CommentItem({
            comment: comment,
            onReplied: () => {
              this.handleCommentsChanged();
            }
          })
            .margin({ bottom: 10 })
        }, (comment: ForumComment) => `${comment.commentId}`)
      }
    }
    .width('100%')
    .alignItems(HorizontalAlign.Start)
  }

  private loadComments(): void {
    this.loading = true;
    CommentApi.listComments(safeNumber(this.postId))
      .then((list: Array<ForumComment>) => {
        this.comments = list === undefined || list === null ? [] : list;
        this.loading = false;
      })
      .catch((error: Object) => {
        this.comments = [];
        this.loading = false;
        showToast(resolveErrorMessage(error, '评论加载失败'));
      });
  }

  private handleSubmit(): void {
    if (this.content.trim().length === 0) {
      showToast('请输入评论内容');
      return;
    }

    this.submitting = true;
    CommentApi.createComment(safeNumber(this.postId), undefined, undefined, this.content.trim())
      .then(() => {
        this.content = '';
        this.submitting = false;
        showToast('评论成功');
        this.handleCommentsChanged();
      })
      .catch((error: Object) => {
        this.submitting = false;
        showToast(resolveErrorMessage(error, '评论失败'));
      });
  }

  private handleCommentsChanged(): void {
    this.loadComments();
    this.onChanged();
  }
}
