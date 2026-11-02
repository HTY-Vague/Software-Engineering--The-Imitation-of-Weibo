import { ForumPost } from '../model/ForumPost';
import { UserApi } from '../api/UserApi';
import { AppRouter } from '../router/AppRouter';
import { RouteNames } from '../router/RouteNames';
import { RouteParam } from '../router/RouteParam';
import { NavHeader } from '../components/NavHeader';
import { PostCard } from '../components/PostCard';
import { LoadingView, EmptyView } from '../components/StateViews';
import { showToast, resolveErrorMessage } from '../utils/ToastUtil';
import { safeNumber } from '../utils/TextUtil';

/**
 * 我的帖子，等价 Web 端 pages/me/MyPostsPage.vue。
 */
@Component
export struct MePostsPage {
  @State posts: Array<ForumPost> = [];
  @State loading: boolean = false;

  aboutToAppear(): void {
    this.loadPosts();
  }

  build() {
    NavDestination() {
      Column() {
        NavHeader()

        Row() {
          Text('我的帖子')
            .fontSize(18)
            .fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.text_primary'))
          Blank()
          Button('发布新帖')
            .fontSize(12)
            .height(32)
            .backgroundColor($r('app.color.accent'))
            .fontColor(Color.White)
            .onClick(() => {
              AppRouter.push(RouteNames.PostCreate);
            })
        }
        .width('100%')
        .padding({ left: 16, right: 16, top: 8, bottom: 8 })

        if (this.loading) {
          LoadingView({ message: '正在加载我的帖子...' })
        } else if (this.posts.length === 0) {
          EmptyView({ message: '你还没有发布过帖子' })
        } else {
          Scroll() {
            Column() {
              ForEach(this.posts, (post: ForumPost) => {
                Column() {
                  PostCard({ post: post })
                  Row() {
                    Blank()
                    Button('编辑')
                      .fontSize(12)
                      .height(32)
                      .backgroundColor($r('app.color.tag_bg'))
                      .fontColor($r('app.color.text_muted'))
                      .onClick(() => {
                        const param: RouteParam = new RouteParam();
                        param.postId = safeNumber(post.postId);
                        AppRouter.push(RouteNames.PostEdit, param);
                      })
                    Button('删除')
                      .fontSize(12)
                      .height(32)
                      .margin({ left: 10 })
                      .backgroundColor($r('app.color.accent_soft'))
                      .fontColor($r('app.color.danger'))
                      .onClick(() => {
                        this.confirmDelete(safeNumber(post.postId));
                      })
                  }
                  .width('100%')
                  .margin({ top: 10 })
                }
                .width('100%')
                .padding(12)
                .margin({ bottom: 12 })
                .borderRadius($r('app.float.card_radius'))
                .backgroundColor($r('app.color.surface'))
                .border({ width: 1, color: $r('app.color.border') })
              }, (post: ForumPost) => `${post.postId}`)

              Text(`共 ${this.posts.length} 篇帖子`)
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
    .title('我的帖子')
    .backgroundColor($r('app.color.page_bg'))
  }

  private loadPosts(): void {
    this.loading = true;
    UserApi.getMyPosts()
      .then((list: Array<ForumPost>) => {
        this.posts = list === undefined || list === null ? [] : list;
        this.loading = false;
      })
      .catch((error: Object) => {
        this.posts = [];
        this.loading = false;
        showToast(resolveErrorMessage(error, '我的帖子加载失败'));
      });
  }

  private confirmDelete(postId: number): void {
    AlertDialog.show({
      title: '删除帖子',
      message: '确认删除这篇帖子吗？删除后无法恢复。',
      primaryButton: {
        value: '取消',
        action: () => {
        }
      },
      secondaryButton: {
        value: '删除',
        fontColor: $r('app.color.danger'),
        action: () => {
          this.doDelete(postId);
        }
      }
    });
  }

  private doDelete(postId: number): void {
    UserApi.deleteMyPost(postId)
      .then(() => {
        showToast('帖子已删除');
        this.loadPosts();
      })
      .catch((error: Object) => {
        showToast(resolveErrorMessage(error, '删除失败'));
      });
  }
}
