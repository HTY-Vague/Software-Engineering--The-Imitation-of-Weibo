import { ForumBoard } from '../model/ForumBoard';
import { ForumPost } from '../model/ForumPost';
import { ForumApi } from '../api/ForumApi';
import { PostApi } from '../api/PostApi';
import { NavHeader } from '../components/NavHeader';
import { BoardSidebar } from '../components/BoardSidebar';
import { PostCard } from '../components/PostCard';
import { LoadingView, EmptyView } from '../components/StateViews';
import { showToast, resolveErrorMessage } from '../utils/ToastUtil';

const PAGE_SIZE: number = 20;

/**
 * 首页：帖子流 + 板块筛选 + 关键词搜索。
 * 等价 Web 端 pages/home/HomePage.vue，作为 Navigation 的根页面。
 */
@Component
export struct HomePage {
  @State boards: Array<ForumBoard> = [];
  @State posts: Array<ForumPost> = [];
  @State keyword: string = '';
  @State boardId: number = 0;
  @State loading: boolean = false;
  @State boardsLoading: boolean = false;

  aboutToAppear(): void {
    this.loadBoards();
    this.loadPosts();
  }

  build() {
    Column() {
      NavHeader()

      Row() {
        TextInput({ text: $$this.keyword, placeholder: '搜索帖子标题或内容' })
          .layoutWeight(1)
          .height(40)
          .fontSize(14)
          .backgroundColor($r('app.color.surface'))
          .onSubmit(() => {
            this.loadPosts();
          })

        Button('搜索')
          .height(40)
          .fontSize(14)
          .margin({ left: 8 })
          .backgroundColor($r('app.color.accent'))
          .fontColor(Color.White)
          .onClick(() => {
            this.loadPosts();
          })
      }
      .width('100%')
      .padding({ left: 16, right: 16, top: 12, bottom: 8 })

      Scroll() {
        Column() {
          BoardSidebar({
            boards: this.boards,
            activeBoardId: this.boardId,
            loading: this.boardsLoading,
            onSelect: (boardId: number | undefined) => {
              this.boardId = boardId === undefined ? 0 : boardId;
              this.loadPosts();
            }
          })

          Row() {
            Text('帖子列表')
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
            LoadingView({ message: '正在加载帖子...' })
          } else if (this.posts.length === 0) {
            EmptyView({ message: '暂无帖子数据' })
          } else {
            ForEach(this.posts, (post: ForumPost) => {
              PostCard({ post: post, boardName: this.resolveBoardName(post.boardId) })
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
    .backgroundColor($r('app.color.page_bg'))
  }

  private resolveBoardName(boardId: number): string {
    for (let i: number = 0; i < this.boards.length; i++) {
      if (this.boards[i].boardId === boardId) {
        return this.boards[i].boardName;
      }
    }
    return '';
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
    PostApi.listPosts(this.boardId > 0 ? this.boardId : undefined, this.keyword.trim(), 1, PAGE_SIZE)
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
}
