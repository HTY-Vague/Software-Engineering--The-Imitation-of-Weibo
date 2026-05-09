import { http, unwrap } from "../http";
import type { ApiResponse, CommentCreatePayload, ForumComment } from "../../types";

export const commentApi = {
  createComment(payload: CommentCreatePayload) {
    return http
      .post<ApiResponse<ForumComment>>("/comments/create", payload)
      .then(unwrap);
  },
  listComments(postId: number) {
    return http
      .post<ApiResponse<ForumComment[]>>("/comments/list", null, {
        params: { postId },
      })
      .then(unwrap);
  },
  deleteComment(commentId: number) {
    return http
      .post<ApiResponse<void>>("/comments/delete", null, {
        params: { commentId },
      })
      .then(unwrap);
  },
};
