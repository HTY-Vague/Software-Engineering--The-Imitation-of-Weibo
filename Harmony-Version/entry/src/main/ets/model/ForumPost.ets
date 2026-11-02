/**
 * 对应帖子实体 FORUM_POST / ForumPost.java
 * content 在 Web 端为富文本 HTML，App 端发帖降级为纯文本，读取时按富文本渲染。
 */
export class ForumPost {
  postId: number = 0;
  boardId: number = 0;
  authorId: number = 0;
  title: string = '';
  content: string = '';
  viewCount: number = 0;
  likeCount: number = 0;
  commentCount: number = 0;
  isTop: number = 0;
  status: string = '';
  createTime: string = '';
  updateTime: string = '';
  authorUsername: string = '';
  authorNickname: string = '';
}
