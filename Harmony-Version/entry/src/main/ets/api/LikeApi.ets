import { HttpClient, QueryBuilder } from './HttpClient';

/**
 * 点赞接口：对应 services/modules/like.js
 * 四个接口全部为 POST + @RequestParam。
 */
export class LikeApi {
  static like(targetType: number, targetId: number): Promise<Object> {
    return HttpClient.post<Object>('/likes/like', LikeApi.buildQuery(targetType, targetId));
  }

  static unlike(targetType: number, targetId: number): Promise<Object> {
    return HttpClient.post<Object>('/likes/unlike', LikeApi.buildQuery(targetType, targetId));
  }

  static getLikeStatus(targetType: number, targetId: number): Promise<boolean> {
    return HttpClient.post<boolean>('/likes/status', LikeApi.buildQuery(targetType, targetId));
  }

  static getLikeCount(targetType: number, targetId: number): Promise<number> {
    return HttpClient.post<number>('/likes/count', LikeApi.buildQuery(targetType, targetId));
  }

  private static buildQuery(targetType: number, targetId: number): string {
    return new QueryBuilder()
      .add('targetType', targetType)
      .add('targetId', targetId)
      .build();
  }
}
