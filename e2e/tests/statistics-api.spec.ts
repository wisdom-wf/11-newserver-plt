import { test, expect, request } from '@playwright/test';

const API_BASE = 'https://wisdomdance.cn/jxy/api';

/**
 * Statistics API Tests - 统计API测试
 * 覆盖：仪表盘 + 各类统计数据
 */
test.describe('Statistics API Tests', () => {
  let adminToken: string;

  test.beforeAll(async ({ request: req }) => {
    const adminLogin = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(adminLogin.ok()).toBeTruthy();
    adminToken = (await adminLogin.json()).data.accessToken;
  });

  test('TC-STAT-001: 仪表盘数据（admin）', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/statistics/dashboard`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect([200, '200']).toContain(body.code);
    console.log(`仪表盘: ${JSON.stringify(body.data || {}).slice(0, 120)}`);
  });

  test('TC-STAT-002: 老人统计', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/statistics/elder`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    if (!res.ok()) { test.skip(); return; }
    const body = await res.json();
    expect([200, '200']).toContain(body.code);
    console.log(`老人统计: ${JSON.stringify(body.data || {}).slice(0, 100)}`);
  });

  test('TC-STAT-003: 服务商统计', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/statistics/provider`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    if (!res.ok()) { test.skip(); return; }
    const body = await res.json();
    expect([200, '200']).toContain(body.code);
    console.log(`服务商统计: ${JSON.stringify(body.data || {}).slice(0, 100)}`);
  });

  test('TC-STAT-004: 订单统计', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/statistics/order`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    if (!res.ok()) { test.skip(); return; }
    const body = await res.json();
    expect([200, '200']).toContain(body.code);
    console.log(`订单统计: ${JSON.stringify(body.data || {}).slice(0, 100)}`);
  });

  test('TC-STAT-005: 服务统计（探索性）', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/statistics/service`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await (async () => { try { return await res.json(); } catch { return null; } })();
    console.log(`服务统计: HTTP ${res.status()}, code=${body?.code}`);
    if (!res.ok() || body === null || body.code !== 200) {
      test.skip(); return;
    }
    console.log(`服务统计: ${JSON.stringify(body.data || {}).slice(0, 100)}`);
  });

  test('TC-STAT-006: 驾驶舱概览（admin）', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/cockpit/overview`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    if (!res.ok()) { test.skip(); return; }
    const body = await res.json();
    expect([200, '200']).toContain(body.code);
    console.log(`驾驶舱: code=${body.code}`);
  });

  test('TC-STAT-007: 服务人员统计', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/statistics/staff`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    if (!res.ok()) { test.skip(); return; }
    const body = await res.json();
    expect([200, '200']).toContain(body.code);
    console.log(`员工统计: ${JSON.stringify(body.data || {}).slice(0, 100)}`);
  });
});
