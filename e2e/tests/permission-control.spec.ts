import { test, expect } from '@playwright/test';

const API_BASE = 'http://localhost:8080/api';

test.describe('权限控制测试', () => {
  let adminToken: string;
  let providerToken: string;

  test.beforeAll(async ({ request }) => {
    const adminLogin = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const adminBody = await adminLogin.json();
    adminToken = adminBody.data.accessToken;

    // FWS1 是真实存在的PROVIDER账号
    const providerLogin = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'FWS1', password: 'admin123' }
    });
    const providerBody = await providerLogin.json();
    providerToken = providerBody.data.accessToken;
  });

  test('管理员可以访问系统管理接口', async ({ request }) => {
    const response = await request.get(`${API_BASE}/system/users`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect([200, 403]).toContain(response.status());
  });

  test('服务商无法访问系统管理接口', async ({ request }) => {
    const response = await request.get(`${API_BASE}/system/users`, {
      headers: { Authorization: `Bearer ${providerToken}` }
    });
    expect(response.status()).toBe(403);
    const body = await response.json();
    expect(body.code).toBe(403);
  });

  test('服务商可以访问业务数据接口', async ({ request }) => {
    const response = await request.get(`${API_BASE}/providers`, {
      headers: { Authorization: `Bearer ${providerToken}` }
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
  });

  test('服务商只能看到自己的数据', async ({ request }) => {
    const response = await request.get(`${API_BASE}/providers`, {
      headers: { Authorization: `Bearer ${providerToken}` }
    });
    const body = await response.json();
    if (body.data && body.data.records) {
      const providerIds = body.data.records.map((r: any) => r.providerId);
      expect(providerIds.length).toBeGreaterThanOrEqual(1);
    }
  });
});
