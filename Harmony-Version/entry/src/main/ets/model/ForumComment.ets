/**
 * 对应评论实体 FORUM_COMMENT / ForumComment.java
 * children 为后端组装的第二级回复（楼中楼），仅两层。
 */
export class ForumComment {
  commentId: number = 0;
  postId: number = 0;
  userId: number = 0;
  parentCommentId: number = 0;
  replyUserId: number = 0;
  content: string = '';
  likeCount: number = 0;
  status: string = '';
  createTime: string = '';
  updateTime: string = '';
  children: Array<ForumComment> = [];
  username: string = '';
  nickname: string = '';
  replyUsername: string = '';
  replyNickname: string = '';
}

/**
 * 发表评论的请求体。
 * 后端以 parentCommentId == null 判定顶级评论（ForumCommentServiceImpl.getComments），
 * 因此顶级评论必须显式传 null，不能传 0。
 */
export class CommentCreateRequest {
  postId: number = 0;
  parentCommentId: number | null = null;
  replyUserId: number | null = null;
  content: string = '';
}

/** 点赞目标类型，与后端 /likes/* 约定一致 */
export const TARGET_TYPE_POST: number = 1;
export const TARGET_TYPE_COMMENT: number = 2;
