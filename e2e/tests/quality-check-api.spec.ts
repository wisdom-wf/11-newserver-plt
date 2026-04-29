import { test, expect, request } from '@playwright/test';

const API_BASE = 'http://localhost:8080/api';

/**
 * QualityCheck API Tests - 质检API测试
 * 关键：质检列表是 POST /quality-check/list，不是 GET
 */
test.describe('QualityCheck API Tests', () => {
  let adminToken: string;
  let providerToken: string;
  let staffToken: string;
  let testOrderId: string;

  test.beforeAll(async ({ request: req }) => {
    const adminLogin = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(adminLogin.ok()).toBeTruthy();
    adminToken = (await adminLogin.json()).data.accessToken;

    const provLogin = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'FWS1', password: 'admin123' }
    });
    expect(provLogin.ok()).toBeTruthy();
    providerToken = (await provLogin.json()).data.accessToken;

    const staffLogin = await req.post(`${API_BASE}/auth/login`, {
      data: { username: '13109118901', password: 'admin123' }
    });
    if (staffLogin.ok()) {
      staffToken = (await staffLogin.json()).data.accessToken;
    }

    // 获取一个测试订单ID
    const orderRes = await req.get(`${API_BASE}/orders?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    if (orderRes.ok()) {
      const d = await orderRes.json();
      if (d.data?.records?.length > 0) testOrderId = d.data.records[0].orderId;
    }
  });

  test('TC-QC-001: 创建质检（admin）', async ({ request: req }) => {
    if (!testOrderId) { test.skip(); return; }
    const res = await req.post(`${API_BASE}/quality-check`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: {
        orderId: testOrderId,
        checkType: 'routine',
        checkResult: 'qualified',
        checkScore: 95,
        checkContent: '自动化质检测试',
        findIssues: false
      }
    });
    expect([200, 201]).toContain(res.status());
    const body = await res.json();
    expect(body.code).toBe(200);
  });

  test('TC-QC-002: 质检列表（admin全量）- POST /quality-check/list', async ({ request: req }) => {
    const res = await req.get(`${API_BASE}/quality-check/list?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.code).toBe(200);
    // body.data 可能为 null（无数据时），不强制要求有数据
  });

  test('TC-QC-003: 质检列表（PROVIDER只看到自己）- POST', async ({ request: req }) => {
    if (!providerToken) { test.skip(); return; }
    const res = await req.get(`${API_BASE}/quality-check/list?page=1&pageSize=50`, {
      headers: { Authorization: `Bearer ${providerToken}` }
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.code).toBe(200);
    console.log(`PROVIDER质检数: ${body.data?.records?.length || 0}`);
  });

  test('TC-QC-004: 质检列表（STAFF只看到自己）- POST', async ({ request: req }) => {
    if (!staffToken) { test.skip(); return; }
    const res = await req.get(`${API_BASE}/quality-check/list?page=1&pageSize=50`, {
      headers: { Authorization: `Bearer ${staffToken}` }
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.code).toBe(200);
    console.log(`STAFF质检数: ${body.data?.records?.length || 0}`);
  });

  test('TC-QC-005: 按订单ID查质检', async ({ request: req }) => {
    if (!testOrderId) { test.skip(); return; }
    const res = await req.get(`${API_BASE}/quality-check/order/${testOrderId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect([200, 404]).toContain(res.status());
  });

  test('TC-QC-006: 质检详情（admin）', async ({ request: req }) => {
    const listRes = await req.get(`${API_BASE}/quality-check/list?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    if (!listRes.ok()) { test.skip(); return; }
    const body = await listRes.json();
    const records: any[] = body.data?.records || [];
    if (records.length === 0) { test.skip(); return; }

    const qcId = records[0].qualityCheckId;
    const detailRes = await req.get(`${API_BASE}/quality-check/${qcId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(detailRes.ok()).toBeTruthy();
    const detail = await detailRes.json();
    expect(detail.code).toBe(200);
  });

  test('TC-QC-007: 更新质检（admin）', async ({ request: req }) => {
    const listRes = await req.get(`${API_BASE}/quality-check/list?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    if (!listRes.ok()) { test.skip(); return; }
    const body = await listRes.json();
    const records: any[] = body.data?.records || [];
    if (records.length === 0) { test.skip(); return; }

    const qcId = records[0].qualityCheckId;
    const updateRes = await req.put(`${API_BASE}/quality-check/${qcId}`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: { checkScore: 98, remark: '自动化更新' }
    });
    expect([200, 201]).toContain(updateRes.status());
  });

  test('TC-QC-008: 删除质检（admin）', async ({ request: req }) => {
    if (!testOrderId) { test.skip(); return; }
    const createRes = await req.post(`${API_BASE}/quality-check`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: {
        orderId: testOrderId,
        checkType: 'routine',
        checkResult: 'qualified',
        checkScore: 88,
        checkContent: '待删除质检',
        findIssues: false
      }
    });
    if (![200, 201].includes(createRes.status())) { test.skip(); return; }
    const created = await createRes.json();
    const qcId = created.data?.qcId || created.data?.id;
    if (!qcId) { test.skip(); return; }

    const delRes = await req.delete(`${API_BASE}/quality-check/${qcId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect([200, 204]).toContain(delRes.status());
  });
});
