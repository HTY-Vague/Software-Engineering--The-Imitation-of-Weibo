import { http, unwrap } from "../http";
import type {
  ApiResponse,
  ForumComment,
  ForumPost,
  UpdatePasswordPayload,
  UpdateProfilePayload,
  User,
} from "../../types";

export const userApi = {
  getProfile() {
    return http.post<ApiResponse<User>>("/user/profile").then(unwrap);
  },
  updateProfile(payload: UpdateProfilePayload) {
    return http
      .post<ApiResponse<void>>("/user/update/profile", payload)
      .then(unwrap);
  },
  updatePassword(payload: UpdatePasswordPayload) {
    return http
      .post<ApiResponse<void>>("/user/update/password", null, {
        params: payload,
      })
      .then(unwrap);
  },
  getMyPosts() {
    return http.post<ApiResponse<ForumPost[]>>("/user/posts").then(unwrap);
  },
  deleteMyPost(postId: number) {
    return http
      .post<ApiResponse<void>>("/user/delete/post", null, { params: { postId } })
      .then(unwrap);
  },
  getMyComments() {
    return http
      .post<ApiResponse<ForumComment[]>>("/user/comments")
      .then(unwrap);
  },
};
