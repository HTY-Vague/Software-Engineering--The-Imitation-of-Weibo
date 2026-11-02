import { HttpClient } from './HttpClient';
import { User } from '../model/User';
import { ForumBoard } from '../model/ForumBoard';
import { ForumComment } from '../model/ForumComment';

/**
 * 管理员接口：对应 services/modules/admin.js
 * 注意点：
 * 1. /admin/users/{id}/status、/admin/forums/{id}/status、/admin/posts/{id}/top
 *    的 body 是裸 Integer（@RequestBody Integer），必须发 Content-Type: application/json + 纯数字文本；
 * 2. /admin/forums/sort 的 body 是板块 ID 数组（后端签名 List<Long>）；
 * 3. 统计概览页按迁移方案不纳入 App 端，故此处不封装 /admin/statistics/*。
 */
export class AdminApi {
  static getUsers(): Promise<Array<User>> {
    return HttpClient.get<Array<User>>('/admin/users');
  }

  static updateUserStatus(userId: number, status: number): Promise<Object> {
    return HttpClient.putScalar<Object>(`/admin/users/${userId}/status`, status);
  }

  static getForums(): Promise<Array<ForumBoard>> {
    return HttpClient.get<Array<ForumBoard>>('/admin/forums');
  }

  static createForum(boardName: string, description: string, sortNum: number, status: number): Promise<ForumBoard> {
    const body: ForumBoard = new ForumBoard();
    body.boardName = boardName;
    body.description = description;
    body.sortNum = sortNum;
    body.status = `${status}`;
    return HttpClient.postJson<ForumBoard>('/admin/forums', body);
  }

  static updateForum(forumId: number, boardName: string, description: string, sortNum: number,
    status: number): Promise<Object> {
    const body: ForumBoard = new ForumBoard();
    body.boardName = boardName;
    body.description = description;
    body.sortNum = sortNum;
    body.status = `${status}`;
    return HttpClient.putJson<Object>(`/admin/forums/${forumId}`, body);
  }

  static updateForumStatus(forumId: number, status: number): Promise<Object> {
    return HttpClient.putScalar<Object>(`/admin/forums/${forumId}/status`, status);
  }

  static sortForums(boardIds: Array<number>): Promise<Object> {
    return HttpClient.putJson<Object>('/admin/forums/sort', boardIds);
  }

  static deleteForum(forumId: number): Promise<Object> {
    return HttpClient.delete<Object>(`/admin/forums/${forumId}`);
  }

  static setPostTop(postId: number, isTop: number): Promise<Object> {
    return HttpClient.putScalar<Object>(`/admin/posts/${postId}/top`, isTop);
  }

  static deletePost(postId: number): Promise<Object> {
    return HttpClient.delete<Object>(`/admin/posts/${postId}`);
  }

  static deleteComment(commentId: number): Promise<Object> {
    return HttpClient.delete<Object>(`/admin/comments/${commentId}`);
  }

  static getComments(): Promise<Array<ForumComment>> {
    return HttpClient.get<Array<ForumComment>>('/admin/comments');
  }
}
