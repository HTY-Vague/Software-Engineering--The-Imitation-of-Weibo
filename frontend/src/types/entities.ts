export type UserRole = "USER" | "ADMIN" | string;

export interface User {
  userId: number;
  username: string;
  password?: string | null;
  nickname: string;
  avatar?: string | null;
  signature?: string | null;
  role: UserRole;
  status: string | number;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface ForumBoard {
  boardId: number;
  boardName: string;
  description?: string | null;
  sortNum?: number | null;
  status: string | number;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface ForumPost {
  postId: number;
  boardId: number;
  authorId: number;
  title: string;
  content: string;
  viewCount: number;
  likeCount: number;
  commentCount: number;
  isTop: number;
  status: string | number;
  createTime?: string | null;
  updateTime?: string | null;
}

export interface ForumComment {
  commentId: number;
  postId: number;
  userId: number;
  parentCommentId?: number | null;
  replyUserId?: number | null;
  content: string;
  likeCount: number;
  status: string | number;
  createTime?: string | null;
  updateTime?: string | null;
  children?: ForumComment[];
}

export interface ForumLike {
  likeId: number;
  targetType: number;
  targetId: number;
  userId: number;
  createTime?: string | null;
}

export type LikeTargetType = 1 | 2;
