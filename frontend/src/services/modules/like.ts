import { http, unwrap } from "../http";
import type { ApiResponse, LikePayload } from "../../types";

export const likeApi = {
  like(payload: LikePayload) {
    return http
      .post<ApiResponse<void>>("/likes/like", null, { params: payload })
      .then(unwrap);
  },
  unlike(payload: LikePayload) {
    return http
      .post<ApiResponse<void>>("/likes/unlike", null, { params: payload })
      .then(unwrap);
  },
  status(payload: LikePayload) {
    return http
      .post<ApiResponse<boolean>>("/likes/status", null, { params: payload })
      .then(unwrap);
  },
  count(payload: LikePayload) {
    return http
      .post<ApiResponse<number>>("/likes/count", null, { params: payload })
      .then(unwrap);
  },
};
