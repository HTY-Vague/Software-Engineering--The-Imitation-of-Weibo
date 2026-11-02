import { HttpClient, QueryBuilder } from './HttpClient';
import { ForumComment, CommentCreateRequest } from '../model/ForumComment';

/**
 * 评论接口：对应 services/modules/comment.js
 * create 为 JSON body；list / delete 为 POST + @RequestParam。
 */
export class CommentApi {
  static listComments(postId: number): Promise<Array<ForumComment>> {
    const query: string = new QueryBuilder().add('postId', postId).build();
    return HttpClient.post<Array<ForumComment>>('/comments/list', query);
  }

  /**
   * 发表评论或楼中楼回复。
   * parentCommentId 传 undefined 表示顶级评论（序列化为 null）；
   * 传具体评论 ID 表示回复，此时后端挂到该评论的 children 下。
   */
  static createComment(postId: number, parentCommentId: number | undefined,
    replyUserId: number | undefined, content: string): Promise<ForumComment> {
    const body: CommentCreateRequest = new CommentCreateRequest();
    body.postId = postId;
    body.parentCommentId = parentCommentId === undefined ? null : parentCommentId;
    body.replyUserId = replyUserId === undefined ? null : replyUserId;
    body.content = content;
    return HttpClient.postJson<ForumComment>('/comments/create', body);
  }

  static deleteComment(commentId: number): Promise<Object> {
    const query: string = new QueryBuilder().add('commentId', commentId).build();
    return HttpClient.post<Object>('/comments/delete', query);
  }
}
