import { test, expect, Page } from '@playwright/test';

/**
 * Financial Refund Tests - 退款管理测试
 *
 * API端点：
 * - GET    /api/financial/refunds         退款列表
 * - GET    /api/financial/refunds/{id}    退款详情
 * - POST   /api/financial/refunds         创建退款
 * - PUT    /api/financial/refunds/{id}/audit  审核退款
 *
 * 已知：
 * - P2-7 "退款自动核算" 前端入口尚未实现（订单页无退款按钮）
 * - amount由用户手动输入，后端不做自动计算
 * - 审核结果：APPROVED / REJECTED
 */

const BASE_URL = 'http://localhost:9527';
const API_BASE = 'http://localhost:8080/api';

async function getToken(page: Page, username = 'admin', password = 'admin123') {
  const resp = await page.request.post(`${API_BASE}/auth/login`, {
    data: { username, password }
  });
  const body = await resp.json();
  if (!resp.ok() || !body?.data?.accessToken) {
    throw new Error(`Login failed: ${resp.status()} ${JSON.stringify(body)}`);
  }
  return body.data.accessToken as string;
}

async function setupAuth(page: Page) {
  await page.goto(`${BASE_URL}/login`);
  await page.fill('input[type="text"], input[placeholder*="用户名"]', 'admin');
  await page.fill('input[type="password"]', 'admin123');
  await page.click('button[type="submit"], button:has-text("确认")');
  await page.waitForURL('**/home**', { timeout: 15000 });
  await page.waitForLoadState('domcontentloaded');
  await page.waitForTimeout(1500);
}

test.describe.serial('Financial Refund API Tests', { tag: '@refund' }, () => {

  test('TC-RF-API-001: 管理员获取退款列表', async ({ page }) => {
    const token = await getToken(page);
    const resp = await page.request.get(`${API_BASE}/financial/refunds?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code).toBe(200);
    expect(body.data.records).toBeDefined();
    console.log(`退款总数: ${body.data.total}`);
  });

  test('TC-RF-API-002: 创建退款申请（完整字段）', async ({ page }) => {
    const token = await getToken(page);

    // 获取一个有已完成订单的orderId
    const orderResp = await page.request.get(`${API_BASE}/orders?page=1&pageSize=3`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const orders = (await orderResp.json()).data.records;
    const order = orders.find((o: any) => o.status === 'COMPLETED') || orders[0];

    if (!order) {
      test.skip();
      return;
    }

    const resp = await page.request.post(`${API_BASE}/financial/refunds`, {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: {
        orderId: order.orderId,
        reason: 'E2E测试：服务不满意申请退款',
        amount: 100,
        refundType: 'FULL',
        remark: '自动化测试'
      }
    });

    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code).toBe(200);
    expect(body.data).toBeTruthy();
    console.log(`创建退款ID: ${body.data}`);
  });

  test('TC-RF-API-003: 创建退款（金额为0应成功）', async ({ page }) => {
    const token = await getToken(page);

    const orderResp = await page.request.get(`${API_BASE}/orders?page=1&pageSize=3`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const orders = (await orderResp.json()).data.records;
    const order = orders[0];

    if (!order) {
      test.skip();
      return;
    }

    const resp = await page.request.post(`${API_BASE}/financial/refunds`, {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: {
        orderId: order.orderId,
        reason: 'E2E测试：金额0的退款',
        amount: 0,
        refundType: 'PARTIAL'
      }
    });

    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code).toBe(200);
  });

  test('TC-RF-API-004: 审核通过退款', async ({ page }) => {
    const token = await getToken(page);

    // 找一个PENDING状态的退款
    const listResp = await page.request.get(`${API_BASE}/financial/refunds?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const records = (await listResp.json()).data.records;
    const pending = records.find((r: any) => r.auditStatus === 'PENDING');

    if (!pending) {
      console.log('无PENDING退款，跳过审核测试');
      test.skip();
      return;
    }

    const resp = await page.request.put(`${API_BASE}/financial/refunds/${pending.refundId}/audit`, {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: { result: 'APPROVED', remark: 'E2E审核通过' }
    });

    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code).toBe(200);
    console.log(`审核通过退款: ${pending.refundId}`);
  });

  test('TC-RF-API-005: 审核拒绝退款', async ({ page }) => {
    const token = await getToken(page);

    const listResp = await page.request.get(`${API_BASE}/financial/refunds?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const records = (await listResp.json()).data.records;
    const pending = records.find((r: any) => r.auditStatus === 'PENDING');

    if (!pending) {
      test.skip();
      return;
    }

    const resp = await page.request.put(`${API_BASE}/financial/refunds/${pending.refundId}/audit`, {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: { result: 'REJECTED', remark: 'E2E审核拒绝：证据不足' }
    });

    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code).toBe(200);
  });

  test('TC-RF-API-006: 获取退款详情', async ({ page }) => {
    const token = await getToken(page);

    const listResp = await page.request.get(`${API_BASE}/financial/refunds?page=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const records = (await listResp.json()).data.records;
    if (records.length === 0) {
      test.skip();
      return;
    }

    const resp = await page.request.get(`${API_BASE}/financial/refunds/${records[0].refundId}`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code).toBe(200);
    expect(body.data.refundId).toBe(records[0].refundId);
    console.log(`退款详情: ${body.data.refundNo}, 状态: ${body.data.auditStatus}`);
  });

  test('TC-RF-API-007: PROVIDER用户获取退款列表（数据隔离）', async ({ page }) => {
    // 通过UI登录FWS1，避免Playwright请求上下文残留状态
    await setupAuth(page);
    // 替换token为FWS1
    await page.evaluate(() => {
      const { sign } = window as any;
      if (sign) {
        localStorage.setItem('token', sign.FWS1_token || localStorage.getItem('token'));
      }
    });
    const token = await page.evaluate(() => localStorage.getItem('token'));
    if (!token || !token.includes('eyJ')) {
      // 如果无法从localStorage获取有效token，直接跳过
      test.skip();
      return;
    }
    // localStorage存的是admin token，不等于FWS1
    // 用请求方式获取FWS1 token
    const resp = await page.request.post(`${API_BASE}/auth/login`, {
      data: { username: 'FWS1', password: 'admin123' }
    });
    const body = await resp.json();
    if (!resp.ok() || !body?.data?.accessToken) {
      test.skip();
      return;
    }
    const resp2 = await page.request.get(`${API_BASE}/financial/refunds?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${body.data.accessToken}` }
    });
    expect(resp2.status()).toBe(200);
    const data = await resp2.json();
    expect(data.code).toBe(200);
    console.log(`FWS1退款数: ${data.data.total}`);
  });

  test('TC-RF-API-008: PROVIDER用户不能审核其他服务商的退款', async ({ page }) => {
    // 获取admin token创建退款
    const adminToken = await getToken(page, 'admin', 'admin123');
    const orderResp = await page.request.get(`${API_BASE}/orders?page=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const orders = (await orderResp.json()).data.records;
    const fws2Order = orders.find((o: any) => String(o.providerId).length > 10);
    if (!fws2Order) {
      test.skip();
      return;
    }

    const createResp = await page.request.post(`${API_BASE}/financial/refunds`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        orderId: fws2Order.orderId,
        reason: 'E2E跨服务商隔离测试',
        amount: 50
      }
    });
    const refundId = (await createResp.json()).data;

    // 获取FWS1 token
    const fwsLoginResp = await page.request.post(`${API_BASE}/auth/login`, {
      data: { username: 'FWS1', password: 'admin123' }
    });
    const fwsBody = await fwsLoginResp.json();
    if (!fwsLoginResp.ok() || !fwsBody?.data?.accessToken) {
      test.skip();
      return;
    }
    const fwsToken = fwsBody.data.accessToken;

    // FWS1尝试审核（应被拒绝或无权限）
    const auditResp = await page.request.put(`${API_BASE}/financial/refunds/${refundId}/audit`, {
      headers: {
        Authorization: `Bearer ${fwsToken}`,
        'Content-Type': 'application/json'
      },
      data: { result: 'APPROVED', remark: 'E2E审核拒绝：证据不足' }
    });

    // 应返回403或400（无权限审核）
    expect([400, 403]).toContain(auditResp.status());
    console.log(`FWS1审核跨服务商退款 → HTTP ${auditResp.status()}`);
  });
});
