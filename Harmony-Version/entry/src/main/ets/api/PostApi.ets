import { HttpClient, QueryBuilder } from './HttpClient';
import { ForumPost } from '../model/ForumPost';

const DEFAULT_PAGE_SIZE: number = 20;

/**
 * 帖子接口：对应 services/modules/post.js
 * create / update 为 JSON body；detail / list / delete 为 POST + @RequestParam。
 */
export class PostApi {
  static createPost(boardId: number, title: string, content: string): Promise<ForumPost> {
    const body: ForumPost = new ForumPost();
    body.boardId = boardId;
    body.title = title;
    body.content = content;
    return HttpClient.postJson<ForumPost>('/posts/create', body);
  }

  static getPostDetail(postId: number): Promise<ForumPost> {
    const query: string = new QueryBuilder().add('postId', postId).build();
    return HttpClient.post<ForumPost>('/posts/detail', query);
  }

  static listPosts(boardId: number | undefined, keyword: string, page: number = 1,
    size: number = DEFAULT_PAGE_SIZE): Promise<Array<ForumPost>> {
    const query: string = new QueryBuilder()
      .add('boardId', boardId)
      .add('keyword', keyword.length > 0 ? keyword : undefined)
      .add('page', page)
      .add('size', size)
      .build();
    return HttpClient.post<Array<ForumPost>>('/posts/list', query);
  }

  static updatePost(postId: number, boardId: number, title: string, content: string): Promise<Object> {
    const body: ForumPost = new ForumPost();
    body.postId = postId;
    body.boardId = boardId;
    body.title = title;
    body.content = content;
    return HttpClient.postJson<Object>('/posts/update', body);
  }

  static deletePost(postId: number): Promise<Object> {
    const query: string = new QueryBuilder().add('postId', postId).build();
    return HttpClient.post<Object>('/posts/delete', query);
  }
}
