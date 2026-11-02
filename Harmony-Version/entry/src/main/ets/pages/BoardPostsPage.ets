import { ForumBoard } from '../model/ForumBoard';
import { ForumPost } from '../model/ForumPost';
import { ForumApi } from '../api/ForumApi';
import { AppRouter } from '../router/AppRouter';
import { NavHeader } from '../components/NavHeader';
import { BoardSidebar } from '../components/BoardSidebar';
import { PostCard } from '../components/PostCard';
import { LoadingView, EmptyView } from '../components/StateViews';
import { showToast, resolveErrorMessage } from '../utils/ToastUtil';

/**
 * 板块帖子列表，等价 Web 端 pages/board/BoardPostsPage.vue。
 */
@Component
export struct BoardPostsPage {
  @State boards: Array<ForumBoard> = [];
  @State posts: Array<ForumPost> = [];
  @State loading: boolean = false;
  @State boardsLoading: boolean = false;
  @Prop boardId: number = 0;

  aboutToAppear(): void {
    this.loadBoards();
    this.loadPosts();
  }

  build() {
    NavDestination() {
      Column() {
        NavHeader()

        Scroll() {
          Column() {
            BoardSidebar({
              boards: this.boards,
              activeBoardId: this.boardId,
              loading: this.boardsLoading,
              onSelect: (boardId: number | undefined) => {
                if (boardId === undefined) {
                  AppRouter.backToHome();
                  return;
                }
                this.boardId = boardId;
                this.loadPosts();
              }
            })

            Row() {
              Text('板块内帖子')
                .fontSize(16)
                .fontWeight(FontWeight.Bold)
                .fontColor($r('app.color.text_primary'))
              Blank()
              Text(`${this.posts.length} 条`)
                .fontSize(12)
                .fontColor($r('app.color.text_muted'))
            }
            .width('100%')
            .padding({ left: 4, right: 4 })
            .margin({ top: 16, bottom: 10 })

            if (this.loading) {
              LoadingView({ message: '正在加载板块帖子...' })
            } else if (this.posts.length === 0) {
              EmptyView({ message: '当前板块暂无帖子' })
            } else {
              ForEach(this.posts, (post: ForumPost) => {
                PostCard({ post: post })
                  .margin({ bottom: 12 })
              }, (post: ForumPost) => `${post.postId}`)
            }
          }
          .width('100%')
          .padding({ left: 16, right: 16, bottom: 24 })
        }
        .layoutWeight(1)
        .scrollable(ScrollDirection.Vertical)
        .scrollBar(BarState.Off)
        .width('100%')
      }
      .width('100%')
      .height('100%')
    }
    .title('板块帖子')
    .backgroundColor($r('app.color.page_bg'))
  }

  private loadBoards(): void {
    this.boardsLoading = true;
    ForumApi.getBoards()
      .then((list: Array<ForumBoard>) => {
        this.boards = list === undefined || list === null ? [] : list;
        this.boardsLoading = false;
      })
      .catch((error: Object) => {
        this.boards = [];
        this.boardsLoading = false;
        showToast(resolveErrorMessage(error, '板块列表加载失败'));
      });
  }

  private loadPosts(): void {
    this.loading = true;
    ForumApi.getBoardPosts(this.boardId)
      .then((list: Array<ForumPost>) => {
        this.posts = list === undefined || list === null ? [] : list;
        this.loading = false;
      })
      .catch((error: Object) => {
        this.posts = [];
        this.loading = false;
        showToast(resolveErrorMessage(error, '板块帖子加载失败'));
      });
  }
}
