import { http, unwrap } from "../http";
import type { ApiResponse, BoardPostsPayload, ForumBoard, ForumPost } from "../../types";

export const boardApi = {
  getBoards() {
    return http.post<ApiResponse<ForumBoard[]>>("/forum/boards").then(unwrap);
  },
  getBoardPosts(payload: BoardPostsPayload) {
    return http
      .post<ApiResponse<ForumPost[]>>("/forum/board/posts", payload)
      .then(unwrap);
  },
};
