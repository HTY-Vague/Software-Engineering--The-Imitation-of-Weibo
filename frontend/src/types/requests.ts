import type { LikeTargetType } from "./entities";

export interface LoginPayload {
  username: string;
  password: string;
}

export interface RegisterPayload {
  username: string;
  password: string;
  nickname?: string;
}

export interface UpdateProfilePayload {
  nickname?: string;
  avatar?: string;
  signature?: string;
}

export interface UpdatePasswordPayload {
  oldPassword: string;
  newPassword: string;
}

export interface PostListQuery {
  boardId?: number;
  keyword?: string;
  page?: number;
  size?: number;
}

export interface PostCreatePayload {
  boardId: number;
  title: string;
  content: string;
}

export interface PostUpdatePayload {
  postId: number;
  boardId?: number;
  title?: string;
  content?: string;
}

export interface BoardPostsPayload {
  boardId: number;
}

export interface BoardCreatePayload {
  boardName: string;
  description?: string;
  sortNum?: number;
  status?: string | number;
}

export interface BoardUpdatePayload extends BoardCreatePayload {
  boardId?: number;
}

export interface CommentCreatePayload {
  postId: number;
  content: string;
  parentCommentId?: number | null;
  replyUserId?: number | null;
}

export interface LikePayload {
  targetType: LikeTargetType;
  targetId: number;
}
