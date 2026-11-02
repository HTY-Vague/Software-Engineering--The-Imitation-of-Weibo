/**
 * 页面间传参载体。
 * 统一走 NavPathStack 的 param，避免 union / any 带来的类型问题。
 */
export class RouteParam {
  postId: number = 0;
  boardId: number = 0;
  keyword: string = '';
  /** 登录后要回跳的目标路由名 */
  redirect: string = '';
  /** 回跳目标需要的帖子 ID */
  redirectPostId: number = 0;
  /** 注册成功后带到登录页的用户名，用于预填 */
  username: string = '';
}

/** 从 NavPathStack 的 param 中安全取值（param 可能为 undefined） */
export function readRouteParam(param: Object | undefined): RouteParam {
  if (param === undefined || param === null) {
    return new RouteParam();
  }
  return param as RouteParam;
}
