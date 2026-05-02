import { request } from '../request';

/**
 * Login
 *
 * @param userName User name
 * @param password Password
 */
export function fetchLogin(userName: string, password: string) {
  return request<Api.Auth.LoginToken>({
    url: '/api/auth/login',
    method: 'post',
    data: {
      username: userName,
      password
    }
  });
}

/**
 * 手机号验证码登录（仅服务人员）
 * @param phone 手机号
 * @param captcha 验证码
 */
export function fetchPhoneLogin(phone: string, captcha: string) {
  return request<Api.Auth.LoginToken>({
    url: '/api/auth/phone-login',
    method: 'post',
    data: {
      phone,
      captcha,
      captchaKey: 'PHONE_LOGIN'
    }
  });
}

/** Get user info */
export function fetchGetUserInfo() {
  return request<Api.Auth.UserInfo>({ url: '/api/auth/userinfo' });
}

/**
 * Refresh token
 *
 * @param refreshToken Refresh token
 */
export function fetchRefreshToken(refreshToken: string) {
  return request<Api.Auth.LoginToken>({
    url: '/api/auth/refreshToken',
    method: 'post',
    data: {
      refreshToken
    }
  });
}

/**
 * return custom backend error
 *
 * @param code error code
 * @param msg error message
 */
export function fetchCustomBackendError(code: string, msg: string) {
  return request({ url: '/api/auth/error', params: { code, msg } });
}

/**
 * 获取用户列表
 */
export function fetchGetUserList(params: Api.Common.PaginatingQueryParams & Api.User.UserQuery) {
  return request<Api.Common.PaginatingQueryRecord<Api.User.User>>({
    url: '/api/system/users',
    method: 'get',
    params
  });
}

/**
 * 获取用户详情
 */
export function fetchGetUser(id: string) {
  return request<Api.User.User>({
    url: `/api/system/users/${id}`,
    method: 'get'
  });
}

/**
 * 创建用户
 */
export function fetchCreateUser(data: Api.User.UserForm) {
  return request({
    url: '/api/system/users',
    method: 'post',
    data
  });
}

/**
 * 更新用户
 */
export function fetchUpdateUser(id: string, data: Api.User.UserForm) {
  return request({
    url: `/api/system/users/${id}`,
    method: 'put',
    data
  });
}

/**
 * 删除用户
 */
export function fetchDeleteUser(id: string) {
  return request({
    url: `/api/system/users/${id}`,
    method: 'delete'
  });
}

/**
 * 修改用户状态
 */
export function fetchUpdateUserStatus(id: string, status: string) {
  return request({
    url: `/api/system/users/${id}/status`,
    method: 'put',
    data: { status }
  });
}

/**
 * 重置密码
 */
export function fetchResetPassword(id: string, password: string) {
  return request({
    url: `/api/system/users/${id}/password`,
    method: 'put',
    data: { password }
  });
}

/**
 * 分配角色
 */
export function fetchAssignRoles(id: string, data: { roleIds: string[] }) {
  return request({
    url: `/api/system/users/${id}/roles`,
    method: 'put',
    data
  });
}
