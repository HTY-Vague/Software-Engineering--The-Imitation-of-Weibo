import { http, unwrap } from "../http";
import type {
  ApiResponse,
  BoardCreatePayload,
  BoardUpdatePayload,
  ForumBoard,
  ForumPost,
  User,
} from "../../types";

export const adminApi = {
  getUsers() {
    return http.get<ApiResponse<User[]>>("/admin/users").then(unwrap);
  },
  updateUserStatus(userId: number, status: number) {
    return http
      .put<ApiResponse<void>>(`/admin/users/${userId}/status`, status)
      .then(unwrap);
  },
  getBoards() {
    return http.get<ApiResponse<ForumBoard[]>>("/admin/forums").then(unwrap);
  },
  addBoard(payload: BoardCreatePayload) {
    return http.post<ApiResponse<ForumBoard>>("/admin/forums", payload).then(unwrap);
  },
  updateBoard(forumId: number, payload: BoardUpdatePayload) {
    return http.put<ApiResponse<void>>(`/admin/forums/${forumId}`, payload).then(unwrap);
  },
  updateBoardStatus(forumId: number, status: number) {
    return http
      .put<ApiResponse<void>>(`/admin/forums/${forumId}/status`, status)
      .then(unwrap);
  },
  sortBoards(boardIds: number[]) {
    return http.put<ApiResponse<void>>("/admin/forums/sort", boardIds).then(unwrap);
  },
  deleteBoard(forumId: number) {
    return http.delete<ApiResponse<void>>(`/admin/forums/${forumId}`).then(unwrap);
  },
  updatePostTop(postId: number, isTop: number) {
    return http.put<ApiResponse<void>>(`/admin/posts/${postId}/top`, isTop).then(unwrap);
  },
  deletePost(postId: number) {
    return http.delete<ApiResponse<void>>(`/admin/posts/${postId}`).then(unwrap);
  },
  deleteComment(commentId: number) {
    return http.delete<ApiResponse<void>>(`/admin/comments/${commentId}`).then(unwrap);
  },
  getOverview() {
    return http.get<ApiResponse<Record<string, number>>>("/admin/statistics/overview").then(unwrap);
  },
  getHotPosts(limit?: number) {
    return http
      .get<ApiResponse<ForumPost[]>>("/admin/statistics/hot-posts", { params: { limit } })
      .then(unwrap);
  },
};
