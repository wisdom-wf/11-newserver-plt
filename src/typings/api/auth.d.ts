declare namespace Api {
  /**
   * namespace Auth
   *
   * backend api module: "auth"
   */
  namespace Auth {
    interface LoginToken {
      token: string;
      refreshToken: string;
    }

    interface UserInfo {
      userId: string;
      userName: string;
      realName: string;
      roles: string[];
      buttons: string[];
      /** 用户类型：super_admin/sys_admin/city_admin/district_admin/street_admin/community_admin/provider_admin/staff */
      userType: string;
      /** 区域ID（用于区县及以下管理员） */
      areaId?: string;
      /** 区域编码 */
      areaCode?: string;
      /** 区域名称 */
      areaName?: string;
      /** 服务商ID（用于服务商管理员） */
      providerId?: string;
      /** 服务商名称 */
      providerName?: string;
      /** 手机号 */
      phone?: string;
      /** 邮箱 */
      email?: string;
      /** 头像 */
      avatar?: string;
    }
  }
}
