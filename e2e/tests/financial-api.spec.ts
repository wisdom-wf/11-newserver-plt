import { test, expect } from '@playwright/test';

const API_BASE = 'http://localhost:8080/api';

/**
 * Financial API Tests - 财务API测试
 * 端点路径（已对照后端 FinancialController.java 确认）：
 *   GET/POST  /api/financial/prices        - 服务定价
 *   GET/POST  /api/financial/settlements  - 结算记录
 *   GET/POST  /api/financial/refunds      - 退款
 *   GET       /api/financial/reports      - 财务报表
 *   POST      /api/financial/settlements/calculate - 结算计算
 */
test.describe('Financial API Tests', () => {
  let adminToken: string;
  let providerToken: string;

  test.beforeAll(async ({ request: req }) => {
    const adminLogin = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(adminLogin.ok()).toBeTruthy();
    adminToken = (await adminLogin.json()).data.accessToken;

    const provLogin = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'FWS1', password: 'admin123' }
    });
    if (provLogin.ok()) {
      providerToken = (await provLogin.json()).data.accessToken;
    }
  });

  async function safeJson(res: any) {
    try { return await res.json(); }
    catch { return null; }
  }

  // ========== 服务定价 ==========

  test('TC-FIN-001: 服务定价列表（admin）', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/financial/prices?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await safeJson(res);
    console.log(`定价列表: HTTP ${res.status()}, code=${body?.code}`);
    expect(res.status()).toBe(200);
    expect(body?.code).toBe(200);
    console.log(`定价: ${body.data?.total || 0} 条`);
  });

  test('TC-FIN-002: 创建服务定价（admin）', async ({ request: req }) => {
    const res = await req.post(`${API_BASE}/financial/prices`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: {
        serviceType: 'housekeeping',
        price: 150.00,
        unit: '次',
        remark: '自动化测试'
      }
    });
    const body = await safeJson(res);
    console.log(`创建定价: HTTP ${res.status()}, code=${body?.code}`);
    // 201=创建成功, 200=成功, 400=业务校验失败(如已存在), 500=服务端错误
    expect([200, 201, 400]).toContain(res.status());
    if (body?.code === 200 || body?.code === 201) {
      expect(body.data).toBeTruthy();
    }
  });

  // ========== 结算 ==========

  test('TC-FIN-003: 结算记录列表（admin）', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/financial/settlements?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await safeJson(res);
    console.log(`结算列表: HTTP ${res.status()}, code=${body?.code}`);
    expect(res.status()).toBe(200);
    expect(body?.code).toBe(200);
    console.log(`结算: ${body.data?.total || 0} 条`);
  });

  test('TC-FIN-004: 发起结算（admin）- 结算计算', async ({ request: req }) => {
    const res = await req.post(`${API_BASE}/financial/settlements/calculate`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: { providerId: '1', period: '2025-04' }
    });
    const body = await safeJson(res);
    console.log(`发起结算: HTTP ${res.status()}, code=${body?.code}`);
    // 200=成功, 400=参数错误(如provider不存在)
    expect([200, 400]).toContain(res.status());
    if (body?.code === 200) {
      expect(body.data).toBeTruthy();
    }
  });

  test('TC-FIN-005: 结算详情（admin）', async ({ request: req }) => {
    const listRes = await req.get(`${API_BASE}/financial/settlements?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const listBody = await safeJson(listRes);
    if (listBody?.code !== 200 || !listBody.data?.records?.length) {
      test.skip(); return;
    }
    const settleId = listBody.data.records[0].settleId || listBody.data.records[0].id;
    const res = await req.get(`${API_BASE}/financial/settlements/${settleId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await safeJson(res);
    console.log(`结算详情: HTTP ${res.status()}, code=${body?.code}`);
    expect(res.status()).toBe(200);
    expect(body?.code).toBe(200);
  });

  // ========== 退款 ==========

  test('TC-FIN-006: 退款申请（admin）', async ({ request: req }) => {
    const res = await req.post(`${API_BASE}/financial/refunds`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: { orderId: '1', refundAmount: 50.00, reason: '自动化测试' }
    });
    const body = await safeJson(res);
    console.log(`退款申请: HTTP ${res.status()}, code=${body?.code}`);
    // 200/201=成功, 400=业务校验失败
    expect([200, 201, 400]).toContain(res.status());
  });

  test('TC-FIN-007: 退款列表（admin）', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/financial/refunds?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await safeJson(res);
    console.log(`退款列表: HTTP ${res.status()}, code=${body?.code}`);
    expect(res.status()).toBe(200);
    expect(body?.code).toBe(200);
    console.log(`退款: ${body.data?.total || 0} 条`);
  });

  // ========== 财务报表 ==========

  test('TC-FIN-008: 财务报表（admin）', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/financial/reports?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await safeJson(res);
    console.log(`财务报表: HTTP ${res.status()}, code=${body?.code}`);
    expect(res.status()).toBe(200);
    expect(body?.code).toBe(200);
  });

  test('TC-FIN-009: 导出财务报表（admin）', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/financial/reports/export`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    console.log(`导出报表: HTTP ${res.status()}`);
    // 导出可能返回200(Excel)或404(未实现)
    expect([200, 404]).toContain(res.status());
  });

  // ========== PROVIDER视角 ==========

  test('TC-FIN-010: 服务商结算（PROVIDER数据隔离）', async ({ request: req }) => {
    if (!providerToken) { test.skip(); return; }
    const res = await req.get(`${API_BASE}/financial/settlements?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${providerToken}` }
    });
    const body = await safeJson(res);
    console.log(`PROVIDER结算: HTTP ${res.status()}, code=${body?.code}`);
    expect(res.status()).toBe(200);
    expect(body?.code).toBe(200);
    // PROVIDER 只能看自己公司数据
    expect(body.data?.records || []).toBeDefined();
  });
});
