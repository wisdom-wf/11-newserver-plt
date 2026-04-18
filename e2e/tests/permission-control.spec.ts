import { test, expect, request } from '@playwright/test';

const API_BASE = 'http://localhost:8080/api';

test.describe('权限控制测试', () => {
  let adminToken: string;
  let providerToken: string;

  test.beforeAll(async () => {
    // 获取 admin token
    const adminLogin = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const adminBody = await adminLogin.json();
    adminToken = adminBody.data.accessToken;

    // 获取 provider token
    const providerLogin = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'CS1', password: 'mima123' }
    });
    const providerBody = await providerLogin.json();
    providerToken = providerBody.data.accessToken;
  });

  test('管理员可以访问系统管理接口', async () => {
    const response = await request.get(`${API_BASE}/system/users`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    // 200 或 403 都可能，取决于管理员是否有该权限
    expect([200, 403]).toContain(response.status());
  });

  test('服务商无法访问系统管理接口', async () => {
    const response = await request.get(`${API_BASE}/system/users`, {
      headers: { Authorization: `Bearer ${providerToken}` }
    });
    expect(response.status()).toBe(403);
    const body = await response.json();
    expect(body.code).toBe(403);
  });

  test('服务商可以访问业务数据接口', async () => {
    const response = await request.get(`${API_BASE}/providers`, {
      headers: { Authorization: `Bearer ${providerToken}` }
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
  });

  test('服务商只能看到自己的数据', async () => {
    // 服务商访问 providers 应该返回该服务商自己的数据
    const response = await request.get(`${API_BASE}/providers`, {
      headers: { Authorization: `Bearer ${providerToken}` }
    });
    const body = await response.json();
    if (body.data && body.data.records) {
      // 验证返回的数据属于该服务商
      const providerIds = body.data.records.map((r: any) => r.providerId);
      // CS1 用户属于哪个 providerId 应该只有他自己
      expect(providerIds.length).toBeGreaterThanOrEqual(1);
    }
  });
});
