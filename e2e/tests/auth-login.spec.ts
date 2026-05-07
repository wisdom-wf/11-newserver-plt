import { test, expect } from '@playwright/test';

const API_BASE = 'https://wisdomdance.cn/jxy/api';

test.describe('登录功能测试', () => {
  test('admin账号登录成功', async ({ request }) => {
    const response = await request.post(`${API_BASE}/auth/login`, {
      data: {
        username: 'admin',
        password: 'admin123'
      }
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data.accessToken).toBeDefined();
    expect(body.data.userInfo.userType).toBe('SYSTEM');
  });

  test('服务商账号登录成功', async ({ request }) => {
    const response = await request.post(`${API_BASE}/auth/login`, {
      data: {
        username: 'FWS1',
        password: 'admin123'
      }
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data.accessToken).toBeDefined();
    expect(body.data.userInfo.userType).toBe('PROVIDER');
  });

  test('错误密码登录失败', async ({ request }) => {
    const response = await request.post(`${API_BASE}/auth/login`, {
      data: {
        username: 'admin',
        password: 'wrongpassword'
      }
    });
    // 后端对认证失败返回 HTTP 401
    expect(response.status()).toBe(401);
  });

  test('不存在的用户登录失败', async ({ request }) => {
    const response = await request.post(`${API_BASE}/auth/login`, {
      data: {
        username: 'nonexistent',
        password: 'anypassword'
      }
    });
    // 后端对认证失败返回 HTTP 401
    expect(response.status()).toBe(401);
  });
});
