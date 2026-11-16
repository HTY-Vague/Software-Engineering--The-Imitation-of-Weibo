import { ForumComment, TARGET_TYPE_COMMENT } from '../model/ForumComment';
import { CommentApi } from '../api/CommentApi';
import { AuthStore } from '../store/AuthStore';
import { formatDateTime } from '../utils/TimeUtil';
import { safeNumber, safeText, trimReplyPrefix } from '../utils/TextUtil';
import { showToast, resolveErrorMessage } from '../utils/ToastUtil';
import { LikeButton } from './LikeButton';

/**
 * 单条评论，等价 Web 端 components/comment/CommentItem.vue。
 * 后端只返回两层结构，children 直接展开一层，不做递归组件。
 */
@Component
export struct CommentItem {
  @Prop comment: ForumComment = new ForumComment();
  @State replying: boolean = false;
  @State replyContent: string = '';
  @State submitting: boolean = false;
  onReplied: () => void = () => {};

  build() {
    Column() {
      Row() {
        Column() {
          Row() {
            Text(this.displayName())
              .fontSize(13)
              .fontWeight(FontWeight.Bold)
              .fontColor($r('app.color.text_primary'))
            if (this.replyTarget().length > 0) {
              Text(`回复 @${this.replyTarget()}`)
                .fontSize(11)
                .fontColor($r('app.color.accent'))
                .margin({ left: 6 })
            }
          }

          Text(formatDateTime(this.comment.createTime))
            .fontSize(10)
            .fontColor($r('app.color.text_muted'))
            .margin({ top: 2 })
        }
        .alignItems(HorizontalAlign.Start)
        .layoutWeight(1)

        LikeButton({ targetType: TARGET_TYPE_COMMENT, targetId: safeNumber(this.comment.commentId) })
      }
      .width('100%')

      Text(safeText(this.comment.content))
        .fontSize(14)
        .fontColor($r('app.color.text_primary'))
        .width('100%')
        .margin({ top: 8 })
        .lineHeight(21)

      Row() {
        Text('回复')
          .fontSize(12)
          .fontColor($r('app.color.accent'))
          .padding({ left: 2, right: 10, top: 4, bottom: 4 })
          .onClick(() => {
            this.handleReply();
          })
      }
      .width('100%')
      .margin({ top: 6 })

      if (this.replying) {
        Column() {
          TextArea({ text: $$this.replyContent, placeholder: `回复 ${this.displayName()}...` })
            .width('100%')
            .height(90)
            .fontSize(14)
            .maxLength(500)
            .backgroundColor($r('app.color.surface'))

          Row() {
            Blank()
            Button('取消')
              .fontSize(12)
              .height(30)
              .backgroundColor($r('app.color.tag_bg'))
              .fontColor($r('app.color.text_muted'))
              .onClick(() => {
                this.cancelReply();
              })
            Button('发送')
              .fontSize(12)
              .height(30)
              .margin({ left: 8 })
              .backgroundColor($r('app.color.accent'))
              .fontColor(Color.White)
              .onClick(() => {
                this.submitReply();
              })
          }
          .width('100%')
          .margin({ top: 8 })
        }
        .width('100%')
        .padding(10)
        .margin({ top: 8 })
        .borderRadius(12)
        .backgroundColor($r('app.color.page_bg'))
      }

      if (this.childrenList().length > 0) {
        Column() {
          ForEach(this.childrenList(), (child: ForumComment) => {
            Column() {
              Row() {
                Text(this.childName(child))
                  .fontSize(12)
                  .fontWeight(FontWeight.Bold)
                  .fontColor($r('app.color.text_primary'))
                if (this.childReplyTarget(child).length > 0) {
                  Text(`回复 @${this.childReplyTarget(child)}`)
                    .fontSize(10)
                    .fontColor($r('app.color.accent'))
                    .margin({ left: 6 })
                }
                Blank()
                Text(formatDateTime(child.createTime))
                  .fontSize(10)
                  .fontColor($r('app.color.text_muted'))
              }
              .width('100%')

              Text(safeText(child.content))
                .fontSize(13)
                .fontColor($r('app.color.text_primary'))
                .width('100%')
                .margin({ top: 4 })
                .lineHeight(20)

              Row() {
                LikeButton({ targetType: TARGET_TYPE_COMMENT, targetId: safeNumber(child.commentId) })
              }
              .width('100%')
              .margin({ top: 4 })
            }
            .width('100%')
            .alignItems(HorizontalAlign.Start)
            .padding(10)
            .margin({ top: 8 })
            .borderRadius(12)
            .backgroundColor($r('app.color.page_bg'))
          }, (child: ForumComment) => `${child.commentId}`)
        }
        .width('100%')
      }
    }
    .width('100%')
    .alignItems(HorizontalAlign.Start)
    .padding(14)
    .borderRadius($r('app.float.card_radius'))
    .backgroundColor($r('app.color.surface'))
    .border({ width: 1, color: $r('app.color.border') })
  }

  /** 后端 children 字段可能缺失，统一兜底为空数组 */
  private childrenList(): Array<ForumComment> {
    if (this.comment.children === undefined || this.comment.children === null) {
      return [];
    }
    return this.comment.children;
  }

  private displayName(): string {
    if (safeText(this.comment.nickname).length > 0) {
      return this.comment.nickname;
    }
    if (safeText(this.comment.username).length > 0) {
      return this.comment.username;
    }
    return `用户 #${safeNumber(this.comment.userId)}`;
  }

  private replyTarget(): string {
    if (safeText(this.comment.replyNickname).length > 0) {
      return this.comment.replyNickname;
    }
    return safeText(this.comment.replyUsername);
  }

  private childName(child: ForumComment): string {
    if (safeText(child.nickname).length > 0) {
      return child.nickname;
    }
    if (safeText(child.username).length > 0) {
      return child.username;
    }
    return `用户 #${safeNumber(child.userId)}`;
  }

  private childReplyTarget(child: ForumComment): string {
    if (safeText(child.replyNickname).length > 0) {
      return child.replyNickname;
    }
    return safeText(child.replyUsername);
  }

  /**
   * 回复挂载点：顶级评论挂自己，子评论挂其父（后端只支持两层）。
   */
  private rootParentId(): number {
    const parentId: number = safeNumber(this.comment.parentCommentId);
    return parentId > 0 ? parentId : safeNumber(this.comment.commentId);
  }

  private handleReply(): void {
    if (!AuthStore.isLogin()) {
      showToast('请先登录后再回复');
      return;
    }
    this.replyContent = `@${this.displayName()} `;
    this.replying = true;
  }

  private cancelReply(): void {
    this.replying = false;
    this.replyContent = '';
  }

  private submitReply(): void {
    const text: string = trimReplyPrefix(this.replyContent);
    if (text.length === 0) {
      showToast('请输入回复内容');
      return;
    }

    this.submitting = true;
    CommentApi.createComment(safeNumber(this.comment.postId), this.rootParentId(),
      safeNumber(this.comment.userId), text)
      .then(() => {
        showToast('回复成功');
        this.cancelReply();
        this.submitting = false;
        this.onReplied();
      })
      .catch((error: Object) => {
        this.submitting = false;
        showToast(resolveErrorMessage(error, '回复失败'));
      });
  }
}
