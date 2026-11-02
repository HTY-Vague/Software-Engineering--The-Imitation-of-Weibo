/**
 * 页面路由名常量，对应 Web 端 router/index.js 的路由记录。
 * 首页 Home 作为 Navigation 根页面，不进栈。
 */
export class RouteNames {
  static readonly Home: string = 'Home';
  static readonly Login: string = 'Login';
  static readonly Register: string = 'Register';
  static readonly PostCreate: string = 'PostCreate';
  static readonly PostDetail: string = 'PostDetail';
  static readonly PostEdit: string = 'PostEdit';
  static readonly BoardPosts: string = 'BoardPosts';
  static readonly MeProfile: string = 'MeProfile';
  static readonly MePosts: string = 'MePosts';
  static readonly MeComments: string = 'MeComments';
  static readonly Settings: string = 'Settings';
  static readonly NotFound: string = 'NotFound';
  static readonly AdminUsers: string = 'AdminUsers';
  static readonly AdminForums: string = 'AdminForums';
  static readonly AdminPosts: string = 'AdminPosts';
  static readonly AdminComments: string = 'AdminComments';
}
