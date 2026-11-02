import { HttpClient } from './HttpClient';
import { ForumBoard } from '../model/ForumBoard';
import { ForumPost } from '../model/ForumPost';

/**
 * 板块接口：对应 services/modules/forum.js
 * /forum/boards 无参；/forum/board/posts 为 JSON body 传 boardId。
 */
export class ForumApi {
  static getBoards(): Promise<Array<ForumBoard>> {
    return HttpClient.post<Array<ForumBoard>>('/forum/boards');
  }

  static getBoardPosts(boardId: number): Promise<Array<ForumPost>> {
    const body: ForumBoard = new ForumBoard();
    body.boardId = boardId;
    return HttpClient.postJson<Array<ForumPost>>('/forum/board/posts', body);
  }
}
