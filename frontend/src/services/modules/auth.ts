import { http, unwrap } from "../http";
import type { ApiResponse, LoginPayload, RegisterPayload, User } from "../../types";

export const authApi = {
  login(payload: LoginPayload) {
    return http
      .post<ApiResponse<{ token: string; user: User }>>("/auth/login", payload)
      .then(unwrap);
  },
  register(payload: RegisterPayload) {
    return http.post<ApiResponse<User>>("/auth/register", payload).then(unwrap);
  },
  logout() {
    return http.post<ApiResponse<void>>("/auth/logout").then(unwrap);
  },
};
