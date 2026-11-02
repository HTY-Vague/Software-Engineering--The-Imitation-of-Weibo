import { ForumBoard } from '../../model/ForumBoard';
import { ForumPost } from '../../model/ForumPost';
import { AdminApi } from '../../api/AdminApi';
import { ForumApi } from '../../api/ForumApi';
import { PostApi } from '../../api/PostApi';
import { AppRouter } from '../../router/AppRouter';
import { RouteNames } from '../../router/RouteNames';
import { RouteParam } from '../../router/RouteParam';
import { NavHeader } from '../../components/NavHeader';
import { PostCard } from '../../components/PostCard';
import { LoadingView, EmptyView } from '../../components/StateViews';
import { showToast, resolveErrorMessage } from '../../utils/ToastUtil';
import { safeNumber, safeText } from '../../utils/TextUtil';

const ADMIN_PAGE_SIZE: number = 50;

/**
 * 内容管理，等价 Web 端 pages/admin/PostsPage.vue。
 * 数据源按迁移方案 3.3 由 /admin/statistics/hot-posts 改为 POST /posts/list。
 */
@Component
export struct AdminPostsPage {
  @State posts: Array<ForumPost> = [];
  @State boards: Array<ForumBoard> = [];
  @State loading: boolean = false;

  aboutToAppear(): void {
    this.loadBoards();
    this.loadPosts();
  }

  build() {
    NavDestination() {
      Column() {
        NavHeader()

        Row() {
          Text('内容管理')
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
              this.loadPosts();
            })
        }
        .width('100%')
        .padding({ left: 16, right: 16, top: 8, bottom: 8 })

        if (this.loading) {
          LoadingView({ message: '正在加载内容列表...' })
        } else if (this.posts.length === 0) {
          EmptyView({ message: '暂无帖子' })
        } else {
          Scroll() {
            Column() {
              ForEach(this.posts, (post: ForumPost) => {
                Column() {
                  PostCard({ post: post, boardName: this.resolveBoardName(safeNumber(post.boardId)) })

                  Flex({ wrap: FlexWrap.Wrap }) {
                    Button('查看')
                      .fontSize(11)
                      .height(30)
                      .backgroundColor($r('app.color.tag_bg'))
                      .fontColor($r('app.color.text_muted'))
                      .onClick(() => {
                        const param: RouteParam = new RouteParam();
                        param.postId = safeNumber(post.postId);
                        AppRouter.push(RouteNames.PostDetail, param);
                      })
                    Button('编辑')
                      .fontSize(11)
                      .height(30)
                      .margin({ left: 8 })
                      .backgroundColor($r('app.color.tag_bg'))
                      .fontColor($r('app.color.text_muted'))
                      .onClick(() => {
                        const param: RouteParam = new RouteParam();
                        param.postId = safeNumber(post.postId);
                        AppRouter.push(RouteNames.PostEdit, param);
                      })
                    Button(safeNumber(post.isTop) === 1 ? '取消置顶' : '置顶')
                      .fontSize(11)
                      .height(30)
                      .margin({ left: 8 })
                      .backgroundColor($r('app.color.tag_bg'))
                      .fontColor($r('app.color.text_muted'))
                      .onClick(() => {
                        this.handleToggleTop(post);
                      })
                    Button('删除')
                      .fontSize(11)
                      .height(30)
                      .margin({ left: 8 })
                      .backgroundColor($r('app.color.accent_soft'))
                      .fontColor($r('app.color.danger'))
                      .onClick(() => {
                        this.confirmDelete(post);
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
    .title('内容管理')
    .backgroundColor($r('app.color.page_bg'))
  }

  private resolveBoardName(boardId: number): string {
    for (let i: number = 0; i < this.boards.length; i++) {
      if (this.boards[i].boardId === boardId) {
        return safeText(this.boards[i].boardName);
      }
    }
    return '';
  }

  private loadBoards(): void {
    ForumApi.getBoards()
      .then((list: Array<ForumBoard>) => {
        this.boards = list === undefined || list === null ? [] : list;
      })
      .catch(() => {
        this.boards = [];
      });
  }

  private loadPosts(): void {
    this.loading = true;
    PostApi.listPosts(undefined, '', 1, ADMIN_PAGE_SIZE)
      .then((list: Array<ForumPost>) => {
        this.posts = list === undefined || list === null ? [] : list;
        this.loading = false;
      })
      .catch((error: Object) => {
        this.posts = [];
        this.loading = false;
        showToast(resolveErrorMessage(error, '帖子列表加载失败'));
      });
  }

  private handleToggleTop(post: ForumPost): void {
    const nextTop: number = safeNumber(post.isTop) === 1 ? 0 : 1;
    AdminApi.setPostTop(safeNumber(post.postId), nextTop)
      .then(() => {
        showToast('置顶状态已更新');
        this.loadPosts();
      })
      .catch((error: Object) => {
        showToast(resolveErrorMessage(error, '更新置顶失败'));
      });
  }

  private confirmDelete(post: ForumPost): void {
    AlertDialog.show({
      title: '删除帖子',
      message: `确认删除帖子「${safeText(post.title)}」吗？`,
      primaryButton: {
        value: '取消',
        action: () => {
        }
      },
      secondaryButton: {
        value: '删除',
        fontColor: $r('app.color.danger'),
        action: () => {
          AdminApi.deletePost(safeNumber(post.postId))
            .then(() => {
              showToast('帖子已删除');
              this.loadPosts();
            })
            .catch((error: Object) => {
              showToast(resolveErrorMessage(error, '删除失败'));
            });
        }
      }
    });
  }
}
