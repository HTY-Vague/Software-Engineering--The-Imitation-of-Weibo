import { http, unwrap } from "../http";
import type {
  ApiResponse,
  ForumPost,
  PostCreatePayload,
  PostListQuery,
  PostUpdatePayload,
} from "../../types";

export const postApi = {
  createPost(payload: PostCreatePayload) {
    return http.post<ApiResponse<ForumPost>>("/posts/create", payload).then(unwrap);
  },
  getPostDetail(postId: number) {
    return http
      .post<ApiResponse<ForumPost>>("/posts/detail", null, { params: { postId } })
      .then(unwrap);
  },
  listPosts(query: PostListQuery) {
    return http
      .post<ApiResponse<ForumPost[]>>("/posts/list", null, { params: query })
      .then(unwrap);
  },
  updatePost(payload: PostUpdatePayload) {
    return http.post<ApiResponse<void>>("/posts/update", payload).then(unwrap);
  },
  deletePost(postId: number) {
    return http
      .post<ApiResponse<void>>("/posts/delete", null, { params: { postId } })
      .then(unwrap);
  },
};
