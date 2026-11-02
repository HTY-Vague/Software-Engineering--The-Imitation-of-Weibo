import { ForumBoard } from '../model/ForumBoard';
import { ForumPost } from '../model/ForumPost';
import { ForumApi } from '../api/ForumApi';
import { PostApi } from '../api/PostApi';
import { AppRouter } from '../router/AppRouter';
import { RouteNames } from '../router/RouteNames';
import { RouteParam } from '../router/RouteParam';
import { NavHeader } from '../components/NavHeader';
import { LoadingView } from '../components/StateViews';
import { showToast, resolveErrorMessage } from '../utils/ToastUtil';
import { plainTextToHtml, safeNumber, safeText, stripHtml } from '../utils/TextUtil';

const MAX_TITLE_LENGTH: number = 120;

/**
 * 编辑帖子页，等价 Web 端 pages/post/PostEditPage.vue。
 * 详情正文可能是 HTML（Web 端发布），编辑时剥离标签还原为纯文本再编辑。
 */
@Component
export struct PostEditPage {
  @State boards: Array<ForumBoard> = [];
  @State boardOptions: Array<SelectOption> = [];
  @State boardIndex: number = 0;
  @State title: string = '';
  @State content: string = '';
  @State loading: boolean = false;
  @State detailLoading: boolean = false;
  @Prop postId: number = 0;

  aboutToAppear(): void {
    this.loadDetail();
  }

  build() {
    NavDestination() {
      Column() {
        NavHeader()

        if (this.detailLoading) {
          LoadingView({ message: '正在加载帖子信息...' })
        } else {
          Scroll() {
            Column() {
              Text(`帖子 ID：${this.postId}`)
                .fontSize(12)
                .fontColor($r('app.color.text_muted'))
                .width('100%')

              Text('板块')
                .fontSize(13)
                .fontColor($r('app.color.text_muted'))
                .width('100%')
                .margin({ top: 16 })
              Select(this.boardOptions)
                .value(this.boardOptions.length > 0 ? this.boardOptions[this.boardIndex].value : '请选择板块')
                .width('100%')
                .height(44)
                .fontSize(15)
                .margin({ top: 6 })
                .backgroundColor($r('app.color.surface'))
                .onSelect((index: number, value: string) => {
                  this.boardIndex = index;
                })

              Text('标题')
                .fontSize(13)
                .fontColor($r('app.color.text_muted'))
                .width('100%')
                .margin({ top: 16 })
              TextInput({ text: $$this.title, placeholder: '请输入帖子标题' })
                .width('100%')
                .height(44)
                .fontSize(15)
                .maxLength(MAX_TITLE_LENGTH)
                .margin({ top: 6 })
                .backgroundColor($r('app.color.surface'))

              Text('正文')
                .fontSize(13)
                .fontColor($r('app.color.text_muted'))
                .width('100%')
                .margin({ top: 16 })
              TextArea({ text: $$this.content, placeholder: '编辑帖子内容' })
                .width('100%')
                .height(260)
                .fontSize(15)
                .margin({ top: 6 })
                .backgroundColor($r('app.color.surface'))

              Row() {
                Button('取消')
                  .layoutWeight(1)
                  .height(46)
                  .fontSize(16)
                  .backgroundColor($r('app.color.tag_bg'))
                  .fontColor($r('app.color.text_muted'))
                  .onClick(() => {
                    AppRouter.pop();
                  })
                Button(this.loading ? '保存中...' : '保存修改')
                  .layoutWeight(1)
                  .height(46)
                  .fontSize(16)
                  .margin({ left: 12 })
                  .backgroundColor($r('app.color.accent'))
                  .fontColor(Color.White)
                  .onClick(() => {
                    this.handleSubmit();
                  })
              }
              .width('100%')
              .margin({ top: 24 })
            }
            .width('100%')
            .padding(16)
            .alignItems(HorizontalAlign.Start)
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
    .title('编辑帖子')
    .backgroundColor($r('app.color.page_bg'))
  }

  private loadDetail(): void {
    this.detailLoading = true;

    ForumApi.getBoards()
      .then((list: Array<ForumBoard>) => {
        this.boards = list === undefined || list === null ? [] : list;
        this.boardOptions = [];
        for (let i: number = 0; i < this.boards.length; i++) {
          this.boardOptions.push({ value: safeText(this.boards[i].boardName, '未命名板块') });
        }
        return PostApi.getPostDetail(this.postId);
      })
      .then((detail: ForumPost) => {
        this.title = safeText(detail.title);
        this.content = stripHtml(safeText(detail.content));
        for (let i: number = 0; i < this.boards.length; i++) {
          if (this.boards[i].boardId === safeNumber(detail.boardId)) {
            this.boardIndex = i;
            break;
          }
        }
        this.detailLoading = false;
      })
      .catch((error: Object) => {
        this.detailLoading = false;
        showToast(resolveErrorMessage(error, '帖子详情加载失败'));
        AppRouter.backToHome();
      });
  }

  private handleSubmit(): void {
    if (this.boards.length === 0) {
      showToast('请选择板块');
      return;
    }
    if (this.title.trim().length === 0) {
      showToast('请输入标题');
      return;
    }
    if (this.content.trim().length === 0) {
      showToast('请输入正文内容');
      return;
    }

    this.loading = true;
    PostApi.updatePost(this.postId, this.boards[this.boardIndex].boardId, this.title.trim(),
      plainTextToHtml(this.content))
      .then(() => {
        this.loading = false;
        showToast('帖子更新成功');
        const param: RouteParam = new RouteParam();
        param.postId = this.postId;
        AppRouter.replace(RouteNames.PostDetail, param);
      })
      .catch((error: Object) => {
        this.loading = false;
        showToast(resolveErrorMessage(error, '更新失败，请检查填写内容'));
      });
  }
}
