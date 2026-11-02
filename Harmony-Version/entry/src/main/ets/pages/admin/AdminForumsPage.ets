import { ForumBoard } from '../../model/ForumBoard';
import { AdminApi } from '../../api/AdminApi';
import { NavHeader } from '../../components/NavHeader';
import { LoadingView, EmptyView } from '../../components/StateViews';
import { showToast, resolveErrorMessage } from '../../utils/ToastUtil';
import { safeNumber, safeText } from '../../utils/TextUtil';

/**
 * 板块管理，等价 Web 端 pages/admin/ForumsPage.vue。
 * el-dialog 改为页面内内联编辑区；el-table 改为卡片列表。
 */
@Component
export struct AdminForumsPage {
  @State forums: Array<ForumBoard> = [];
  @State loading: boolean = false;
  @State saving: boolean = false;
  @State editorVisible: boolean = false;
  @State editingBoardId: number = 0;
  @State formName: string = '';
  @State formDesc: string = '';
  @State formSort: string = '0';
  @State formStatus: number = 1;

  aboutToAppear(): void {
    this.loadForums();
  }

  build() {
    NavDestination() {
      Column() {
        NavHeader()

        Row() {
          Text('板块管理')
            .fontSize(18)
            .fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.text_primary'))
          Blank()
          Button('保存排序')
            .fontSize(12)
            .height(32)
            .backgroundColor($r('app.color.tag_bg'))
            .fontColor($r('app.color.text_muted'))
            .onClick(() => {
              this.handleSaveSort();
            })
          Button('新增板块')
            .fontSize(12)
            .height(32)
            .margin({ left: 8 })
            .backgroundColor($r('app.color.accent'))
            .fontColor(Color.White)
            .onClick(() => {
              this.openCreate();
            })
        }
        .width('100%')
        .padding({ left: 16, right: 16, top: 8, bottom: 8 })

        if (this.editorVisible) {
          this.EditorPanel()
        }

        if (this.loading) {
          LoadingView({ message: '正在加载板块列表...' })
        } else if (this.forums.length === 0) {
          EmptyView({ message: '暂无板块' })
        } else {
          Scroll() {
            Column() {
              ForEach(this.forums, (board: ForumBoard, index: number) => {
                Column() {
                  Row() {
                    Text(`#${safeNumber(board.boardId)}`)
                      .fontSize(11)
                      .fontColor($r('app.color.text_muted'))
                    Blank()
                    Text(safeText(board.status) === '1' ? '启用' : '禁用')
                      .fontSize(11)
                      .fontColor(safeText(board.status) === '1' ?
                        $r('app.color.success') : $r('app.color.danger'))
                      .backgroundColor($r('app.color.tag_bg'))
                      .padding({ left: 8, right: 8, top: 3, bottom: 3 })
                      .borderRadius(10)
                  }
                  .width('100%')

                  Text(safeText(board.boardName, '未命名板块'))
                    .fontSize(15)
                    .fontWeight(FontWeight.Bold)
                    .fontColor($r('app.color.text_primary'))
                    .width('100%')
                    .margin({ top: 6 })

                  Text(safeText(board.description, '暂无简介'))
                    .fontSize(12)
                    .fontColor($r('app.color.text_muted'))
                    .width('100%')
                    .margin({ top: 4 })
                    .maxLines(2)
                    .textOverflow({ overflow: TextOverflow.Ellipsis })

                  Text(`排序值：${safeNumber(board.sortNum)}`)
                    .fontSize(11)
                    .fontColor($r('app.color.text_muted'))
                    .width('100%')
                    .margin({ top: 4 })

                  Flex({ wrap: FlexWrap.Wrap }) {
                    Button('上移')
                      .fontSize(11)
                      .height(30)
                      .backgroundColor($r('app.color.tag_bg'))
                      .fontColor($r('app.color.text_muted'))
                      .onClick(() => {
                        this.moveBoard(index, -1);
                      })
                    Button('下移')
                      .fontSize(11)
                      .height(30)
                      .margin({ left: 8 })
                      .backgroundColor($r('app.color.tag_bg'))
                      .fontColor($r('app.color.text_muted'))
                      .onClick(() => {
                        this.moveBoard(index, 1);
                      })
                    Button('编辑')
                      .fontSize(11)
                      .height(30)
                      .margin({ left: 8 })
                      .backgroundColor($r('app.color.tag_bg'))
                      .fontColor($r('app.color.text_muted'))
                      .onClick(() => {
                        this.openEdit(board);
                      })
                    Button(safeText(board.status) === '1' ? '禁用' : '启用')
                      .fontSize(11)
                      .height(30)
                      .margin({ left: 8 })
                      .backgroundColor($r('app.color.tag_bg'))
                      .fontColor($r('app.color.text_muted'))
                      .onClick(() => {
                        this.handleToggleStatus(board);
                      })
                    Button('删除')
                      .fontSize(11)
                      .height(30)
                      .margin({ left: 8 })
                      .backgroundColor($r('app.color.accent_soft'))
                      .fontColor($r('app.color.danger'))
                      .onClick(() => {
                        this.confirmDelete(board);
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
              }, (board: ForumBoard) => `${board.boardId}`)

              Text(`共 ${this.forums.length} 个板块`)
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
    .title('板块管理')
    .backgroundColor($r('app.color.page_bg'))
  }

  @Builder
  EditorPanel() {
    Column() {
      Text(this.editingBoardId > 0 ? '编辑板块' : '新增板块')
        .fontSize(16)
        .fontWeight(FontWeight.Bold)
        .fontColor($r('app.color.text_primary'))
        .width('100%')
        .margin({ bottom: 12 })

      Text('板块名称')
        .fontSize(13)
        .fontColor($r('app.color.text_muted'))
        .width('100%')
      TextInput({ text: $$this.formName, placeholder: '请输入板块名称' })
        .width('100%')
        .height(44)
        .fontSize(15)
        .margin({ top: 6 })
        .backgroundColor($r('app.color.surface'))

      Text('简介')
        .fontSize(13)
        .fontColor($r('app.color.text_muted'))
        .width('100%')
        .margin({ top: 16 })
      TextArea({ text: $$this.formDesc, placeholder: '请输入板块简介' })
        .width('100%')
        .height(90)
        .fontSize(15)
        .margin({ top: 6 })
        .backgroundColor($r('app.color.surface'))

      Text('排序值')
        .fontSize(13)
        .fontColor($r('app.color.text_muted'))
        .width('100%')
        .margin({ top: 16 })
      TextInput({ text: $$this.formSort, placeholder: '0' })
        .width('100%')
        .height(44)
        .fontSize(15)
        .type(InputType.Number)
        .margin({ top: 6 })
        .backgroundColor($r('app.color.surface'))

      Row() {
        Text('状态')
          .fontSize(13)
          .fontColor($r('app.color.text_muted'))
        Radio({ value: 'enabled', group: 'boardStatus' })
          .checked(this.formStatus === 1)
          .onChange((checked: boolean) => {
            if (checked) {
              this.formStatus = 1;
            }
          })
        Text('启用')
          .fontSize(13)
          .fontColor($r('app.color.text_primary'))
          .margin({ right: 16 })
        Radio({ value: 'disabled', group: 'boardStatus' })
          .checked(this.formStatus === 0)
          .onChange((checked: boolean) => {
            if (checked) {
              this.formStatus = 0;
            }
          })
        Text('禁用')
          .fontSize(13)
          .fontColor($r('app.color.text_primary'))
      }
      .width('100%')
      .margin({ top: 16 })

      Row() {
        Button('取消')
          .layoutWeight(1)
          .height(44)
          .fontSize(15)
          .backgroundColor($r('app.color.tag_bg'))
          .fontColor($r('app.color.text_muted'))
          .onClick(() => {
            this.editorVisible = false;
          })
        Button(this.saving ? '保存中...' : '保存')
          .layoutWeight(1)
          .height(44)
          .fontSize(15)
          .margin({ left: 12 })
          .backgroundColor($r('app.color.accent'))
          .fontColor(Color.White)
          .onClick(() => {
            this.handleSave();
          })
      }
      .width('100%')
      .margin({ top: 18 })
    }
    .width('100%')
    .alignItems(HorizontalAlign.Start)
    .padding(16)
    .margin({ left: 16, right: 16, bottom: 12 })
    .borderRadius($r('app.float.card_radius'))
    .backgroundColor($r('app.color.surface'))
    .border({ width: 1, color: $r('app.color.border') })
  }

  private loadForums(): void {
    this.loading = true;
    AdminApi.getForums()
      .then((list: Array<ForumBoard>) => {
        this.forums = list === undefined || list === null ? [] : list;
        this.loading = false;
      })
      .catch((error: Object) => {
        this.forums = [];
        this.loading = false;
        showToast(resolveErrorMessage(error, '板块列表加载失败'));
      });
  }

  private openCreate(): void {
    this.editingBoardId = 0;
    this.formName = '';
    this.formDesc = '';
    this.formSort = '0';
    this.formStatus = 1;
    this.editorVisible = true;
  }

  private openEdit(board: ForumBoard): void {
    this.editingBoardId = safeNumber(board.boardId);
    this.formName = safeText(board.boardName);
    this.formDesc = safeText(board.description);
    this.formSort = `${safeNumber(board.sortNum)}`;
    this.formStatus = safeText(board.status) === '1' ? 1 : 0;
    this.editorVisible = true;
  }

  private handleSave(): void {
    if (this.formName.trim().length === 0) {
      showToast('请输入板块名称');
      return;
    }

    const sortNum: number = parseInt(this.formSort);
    const sortValue: number = isNaN(sortNum) ? 0 : sortNum;
    this.saving = true;

    const task: Promise<Object> = this.editingBoardId > 0 ?
      AdminApi.updateForum(this.editingBoardId, this.formName.trim(), this.formDesc.trim(),
        sortValue, this.formStatus) :
      AdminApi.createForum(this.formName.trim(), this.formDesc.trim(), sortValue, this.formStatus)
        .then((board: ForumBoard): Object => {
          return board;
        });

    task.then(() => {
      this.saving = false;
      this.editorVisible = false;
      showToast(this.editingBoardId > 0 ? '板块已更新' : '板块已新增');
      this.loadForums();
    })
      .catch((error: Object) => {
        this.saving = false;
        showToast(resolveErrorMessage(error, '保存板块失败'));
      });
  }

  private handleToggleStatus(board: ForumBoard): void {
    const nextStatus: number = safeText(board.status) === '1' ? 0 : 1;
    AdminApi.updateForumStatus(safeNumber(board.boardId), nextStatus)
      .then(() => {
        showToast('板块状态已更新');
        this.loadForums();
      })
      .catch((error: Object) => {
        showToast(resolveErrorMessage(error, '更新板块状态失败'));
      });
  }

  private confirmDelete(board: ForumBoard): void {
    AlertDialog.show({
      title: '删除板块',
      message: `确认删除板块「${safeText(board.boardName)}」吗？`,
      primaryButton: {
        value: '取消',
        action: () => {
        }
      },
      secondaryButton: {
        value: '删除',
        fontColor: $r('app.color.danger'),
        action: () => {
          AdminApi.deleteForum(safeNumber(board.boardId))
            .then(() => {
              showToast('板块已删除');
              this.loadForums();
            })
            .catch((error: Object) => {
              showToast(resolveErrorMessage(error, '删除失败'));
            });
        }
      }
    });
  }

  private moveBoard(index: number, delta: number): void {
    const nextIndex: number = index + delta;
    if (nextIndex < 0 || nextIndex >= this.forums.length) {
      return;
    }

    const updated: Array<ForumBoard> = [];
    for (let i: number = 0; i < this.forums.length; i++) {
      updated.push(this.forums[i]);
    }
    const moved: ForumBoard = updated[index];
    updated.splice(index, 1);
    updated.splice(nextIndex, 0, moved);
    this.forums = updated;
  }

  private handleSaveSort(): void {
    const ids: Array<number> = [];
    for (let i: number = 0; i < this.forums.length; i++) {
      ids.push(safeNumber(this.forums[i].boardId));
    }

    AdminApi.sortForums(ids)
      .then(() => {
        showToast('板块顺序已保存');
        this.loadForums();
      })
      .catch((error: Object) => {
        showToast(resolveErrorMessage(error, '保存排序失败'));
      });
  }
}
