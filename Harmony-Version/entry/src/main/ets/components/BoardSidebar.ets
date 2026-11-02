import { ForumBoard } from '../model/ForumBoard';
import { safeText } from '../utils/TextUtil';

/**
 * 板块列表，等价 Web 端 components/layout/BoardSidebar.vue。
 * 手机端渲染为可横向滚动的板块卡片行，activeBoardId 为 0 表示"全部板块"。
 */
@Component
export struct BoardSidebar {
  @Prop boards: Array<ForumBoard> = [];
  @Prop activeBoardId: number = 0;
  @Prop loading: boolean = false;
  private onSelect: (boardId: number | undefined) => void = () => {};

  build() {
    Column() {
      Row() {
        Text('板块列表')
          .fontSize(15)
          .fontWeight(FontWeight.Bold)
          .fontColor($r('app.color.text_primary'))
        Blank()
        Text(`${this.boards.length}`)
          .fontSize(12)
          .fontColor($r('app.color.text_muted'))
      }
      .width('100%')
      .margin({ bottom: 10 })

      if (this.loading && this.boards.length === 0) {
        Text('正在加载板块...')
          .fontSize(12)
          .fontColor($r('app.color.text_muted'))
          .width('100%')
          .textAlign(TextAlign.Center)
          .padding(12)
      } else {
        Scroll() {
          Row() {
            this.BoardChip('全部板块', '查看全部帖子', this.activeBoardId === 0, undefined)
            ForEach(this.boards, (board: ForumBoard) => {
              this.BoardChip(safeText(board.boardName, '未命名板块'),
                safeText(board.description, '暂无简介'),
                this.activeBoardId === board.boardId, board.boardId);
            }, (board: ForumBoard) => `${board.boardId}`)
          }
        }
        .scrollable(ScrollDirection.Horizontal)
        .scrollBar(BarState.Off)
        .width('100%')
      }
    }
    .width('100%')
    .padding(14)
    .borderRadius($r('app.float.card_radius'))
    .backgroundColor($r('app.color.surface'))
    .border({ width: 1, color: $r('app.color.border') })
  }

  @Builder
  BoardChip(name: string, desc: string, active: boolean, boardId: number | undefined) {
    Column() {
      Text(name)
        .fontSize(13)
        .fontWeight(FontWeight.Bold)
        .fontColor(active ? Color.White : $r('app.color.text_primary'))
      Text(desc)
        .fontSize(10)
        .margin({ top: 2 })
        .maxLines(1)
        .textOverflow({ overflow: TextOverflow.Ellipsis })
        .fontColor(active ? Color.White : $r('app.color.text_muted'))
    }
    .alignItems(HorizontalAlign.Start)
    .padding({ left: 12, right: 12, top: 8, bottom: 8 })
    .margin({ right: 8 })
    .borderRadius(14)
    .constraintSize({ maxWidth: 160 })
    .backgroundColor(active ? $r('app.color.accent') : $r('app.color.tag_bg'))
    .onClick(() => {
      this.onSelect(boardId);
    })
  }
}
