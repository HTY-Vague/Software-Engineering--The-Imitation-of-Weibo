import { HttpClient, QueryBuilder } from './HttpClient';
import { User } from '../model/User';
import { ForumPost } from '../model/ForumPost';
import { ForumComment } from '../model/ForumComment';

/**
 * 用户接口：对应 services/modules/user.js
 * 注意：updatePassword 与 deleteMyPost 走的都是 POST + @RequestParam（query）。
 */
export class UserApi {
  static getProfile(): Promise<User> {
    return HttpClient.post<User>('/user/profile');
  }

  static updateProfile(username: string, nickname: string, avatar: string, signature: string): Promise<Object> {
    const body: User = new User();
    body.username = username;
    body.nickname = nickname;
    body.avatar = avatar;
    body.signature = signature;
    return HttpClient.postJson<Object>('/user/update/profile', body);
  }

  static updatePassword(oldPassword: string, newPassword: string): Promise<Object> {
    const query: string = new QueryBuilder()
      .add('oldPassword', oldPassword)
      .add('newPassword', newPassword)
      .build();
    return HttpClient.post<Object>('/user/update/password', query);
  }

  static getMyPosts(): Promise<Array<ForumPost>> {
    return HttpClient.post<Array<ForumPost>>('/user/posts');
  }

  static deleteMyPost(postId: number): Promise<Object> {
    const query: string = new QueryBuilder().add('postId', postId).build();
    return HttpClient.post<Object>('/user/delete/post', query);
  }

  static getMyComments(): Promise<Array<ForumComment>> {
    return HttpClient.post<Array<ForumComment>>('/user/comments');
  }
}
