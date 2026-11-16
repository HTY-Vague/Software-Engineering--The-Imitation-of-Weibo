import { LikeApi } from '../api/LikeApi';
import { AuthStore } from '../store/AuthStore';
import { showToast, resolveErrorMessage } from '../utils/ToastUtil';

/**
 * 点赞按钮，等价 Web 端 components/like/LikeButton.vue。
 * 未登录时提示并阻止操作，与 Web 端行为一致。
 */
@Component
export struct LikeButton {
  @Prop targetType: number = 1;
  @Prop targetId: number = 0;
  @State liked: boolean = false;
  @State count: number = 0;
  @State loading: boolean = false;
  onChanged: () => void = () => {};

  aboutToAppear(): void {
    this.loadState();
  }

  build() {
    Row() {
      if (this.loading) {
        LoadingProgress()
          .width(16)
          .height(16)
          .color($r('app.color.text_muted'))
          .margin({ right: 6 })
      }
      Text(this.liked ? '已点赞' : '点赞')
        .fontSize(12)
        .fontColor(this.liked ? $r('app.color.accent_strong') : $r('app.color.text_muted'))
      Text(` · ${this.count}`)
        .fontSize(12)
        .fontColor(this.liked ? $r('app.color.accent_strong') : $r('app.color.text_muted'))
    }
    .padding({ left: 12, right: 12, top: 6, bottom: 6 })
    .borderRadius(14)
    .backgroundColor(this.liked ? $r('app.color.accent_soft') : $r('app.color.tag_bg'))
    .onClick(() => {
      this.handleToggle();
    })
  }

  private loadState(): void {
    this.loading = true;
    const targetType: number = this.targetType;
    const targetId: number = this.targetId;

    LikeApi.getLikeCount(targetType, targetId)
      .then((total: number) => {
        this.count = total === undefined || total === null ? 0 : total;
        if (!AuthStore.isLogin()) {
          this.liked = false;
          this.loading = false;
          return;
        }
        return LikeApi.getLikeStatus(targetType, targetId)
          .then((status: boolean) => {
            this.liked = status === true;
            this.loading = false;
          })
          .catch(() => {
            this.loading = false;
          });
      })
      .catch(() => {
        this.loading = false;
      });
  }

  private handleToggle(): void {
    if (!AuthStore.isLogin()) {
      showToast('请先登录后再点赞');
      return;
    }

    const targetType: number = this.targetType;
    const targetId: number = this.targetId;

    const task: Promise<Object> = this.liked ?
      LikeApi.unlike(targetType, targetId) :
      LikeApi.like(targetType, targetId);

    task.then(() => {
      this.liked = !this.liked;
      this.count = this.liked ? this.count + 1 : Math.max(0, this.count - 1);
      this.onChanged();
    })
      .catch((error: Object) => {
        showToast(resolveErrorMessage(error, '点赞操作失败'));
      });
  }
}
