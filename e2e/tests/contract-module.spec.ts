import { test, expect } from '@playwright/test';

/**
 * 合同管理模块专项测试
 * 覆盖范围：合同API + 订单页合同联动 + 合同管理页面
 */

const BASE_URL = '';  // uses playwright baseURL
const API_URL = 'https://wisdomdance.cn/jxy/api';

let adminToken = '';

// ========== 辅助函数 ==========

async function login(request: any, username: string, password: string): Promise<string> {
  const response = await request.post(`${API_URL}/api/auth/login`, {
    data: { username, password }
  });
  const data = await response.json();
  if (!data.data || !data.data.accessToken) {
    throw new Error(`Login failed for ${username}: ${JSON.stringify(data)}`);
  }
  return data.data.accessToken;
}

async function apiGet(request: any, path: string, token: string) {
  return request.get(`${API_URL}${path}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
}

async function apiPost(request: any, path: string, token: string, data: any) {
  return request.post(`${API_URL}${path}`, {
    headers: { Authorization: `Bearer ${token}` },
    data
  });
}

// ========== API层测试 ==========

test.describe('合同模块 - API层测试', () => {

  test.beforeAll(async ({ request }) => {
    adminToken = await login(request, 'admin', 'admin123');
  });

  test('TC-CON-API-001: 获取合同列表（管理员）', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts', adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data).toHaveProperty('total');
    expect(body.data).toHaveProperty('records');
  });

  test('TC-CON-API-002: 合同列表支持按状态筛选', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts?status=INITIATED', adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    if (body.data.records && body.data.records.length > 0) {
      for (const contract of body.data.records) {
        expect(contract.status).toBe('INITIATED');
      }
    }
  });

  test('TC-CON-API-003: 合同列表支持按合同编号搜索', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts?contractNo=CONTRACT', adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
  });

  test('TC-CON-API-004: 合同列表支持分页', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts?page=1&pageSize=2', adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data.records.length).toBeLessThanOrEqual(2);
  });

  test('TC-CON-API-005: 根据合同ID获取合同详情', async ({ request }) => {
    const listResp = await apiGet(request, '/api/contracts?pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (!listBody.data?.records?.length) {
      test.skip();
      return;
    }
    const contractId = listBody.data.records[0].contractId;
    const response = await apiGet(request, `/api/contracts/${contractId}`, adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data.contractId).toBe(contractId);
  });

  test('TC-CON-API-006: 获取不存在的合同详情返回404', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts/nonexistent123', adminToken);
    const body = await response.json();
    expect(body.code).toBe(404);
  });

  test('TC-CON-API-007: 根据订单ID获取关联合同', async ({ request }) => {
    const listResp = await apiGet(request, '/api/contracts?pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (!listBody.data?.records?.length) {
      test.skip();
      return;
    }
    const orderId = listBody.data.records[0].orderId;
    const response = await apiGet(request, `/api/contracts/order/${orderId}`, adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
  });

  test('TC-CON-API-008: 获取签署链接', async ({ request }) => {
    const listResp = await apiGet(request, '/api/contracts?status=INITIATED&pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (!listBody.data?.records?.length) {
      test.skip();
      return;
    }
    const contractId = listBody.data.records[0].contractId;
    const response = await apiGet(request, `/api/contracts/${contractId}/sign-url`, adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data).toHaveProperty('signUrl');
  });

  test('TC-CON-API-009: 获取合同状态', async ({ request }) => {
    const listResp = await apiGet(request, '/api/contracts?pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (!listBody.data?.records?.length) {
      test.skip();
      return;
    }
    const contractId = listBody.data.records[0].contractId;
    const response = await apiGet(request, `/api/contracts/${contractId}/status`, adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(typeof body.data).toBe('string');
  });

  test('TC-CON-API-010: 下载合同', async ({ request }) => {
    const listResp = await apiGet(request, '/api/contracts?status=SIGNED&pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (!listBody.data?.records?.length) {
      test.skip();
      return;
    }
    const contractId = listBody.data.records[0].contractId;
    const response = await apiGet(request, `/api/contracts/${contractId}/download`, adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
  });

  test('TC-CON-API-011: 取消已签署的合同应失败', async ({ request }) => {
    const listResp = await apiGet(request, '/api/contracts?status=SIGNED&pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (!listBody.data?.records?.length) {
      test.skip();
      return;
    }
    const contractId = listBody.data.records[0].contractId;
    const response = await apiPost(request, `/api/contracts/${contractId}/cancel`, adminToken, {});
    const body = await response.json();
    expect(body.code).toBe(400);
  });

  test('TC-CON-API-012: 回调接口无需认证', async ({ request }) => {
    const response = await request.post(`${API_URL}/api/contracts/callback`, {
      data: { MsgData: { FlowId: 'test', FlowCallbackStatus: 4 } }
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
  });
});

// ========== 订单页合同联动测试 ==========

test.describe('合同模块 - 订单页合同联动测试', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/dashboard|home/);
  });

  test('TC-CON-UI-001: 接单时检查合同状态', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForSelector('.n-data-table');

    const acceptBtn = page.locator('button:has-text("接单")').first();
    if (await acceptBtn.isVisible()) {
      await acceptBtn.click();
      const contractModal = page.locator('.n-modal:has-text("合同详情")');
      if (await contractModal.isVisible({ timeout: 3000 })) {
        await expect(page.locator('text=合同编号')).toBeVisible();
        await expect(page.locator('text=合同状态')).toBeVisible();
      }
    }
  });

  test('TC-CON-UI-002: 合同详情弹窗显示签署按钮', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForSelector('.n-data-table');

    const acceptBtn = page.locator('button:has-text("接单")').first();
    if (await acceptBtn.isVisible()) {
      await acceptBtn.click();
      const contractModal = page.locator('.n-modal:has-text("合同详情")');
      if (await contractModal.isVisible({ timeout: 3000 })) {
        await expect(page.locator('.n-modal button:has-text("签署合同")')).toBeVisible();
        await expect(page.locator('.n-modal button:has-text("刷新状态")')).toBeVisible();
      }
    }
  });

  test('TC-CON-UI-003: 开始服务前检查合同签署状态', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForSelector('.n-data-table');

    const startBtn = page.locator('button:has-text("开始服务")').first();
    if (await startBtn.isVisible()) {
      await startBtn.click();
      const warningMsg = page.locator('.n-message--warning');
      if (await warningMsg.isVisible({ timeout: 3000 })) {
        const msgText = await warningMsg.textContent();
        expect(msgText).toContain('合同');
      }
    }
  });
});

// ========== 合同管理页面测试 ==========

test.describe('合同模块 - 合同管理页面测试', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/dashboard|home/);
  });

  test('TC-CON-PAGE-001: 合同管理页面加载', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/contract`);
    await page.waitForSelector('.n-data-table');
    await expect(page.locator('text=合同管理')).toBeVisible();
    await expect(page.locator('input[placeholder="合同编号"]')).toBeVisible();
  });

  test('TC-CON-PAGE-002: 合同列表显示数据', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/contract`);
    await page.waitForSelector('.n-data-table');
    await page.waitForTimeout(2000);
    await expect(page.locator('th:has-text("合同编号")')).toBeVisible();
    await expect(page.locator('th:has-text("状态")')).toBeVisible();
    await expect(page.locator('th:has-text("操作")')).toBeVisible();
  });

  test('TC-CON-PAGE-003: 点击详情打开抽屉', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/contract`);
    await page.waitForSelector('.n-data-table');
    await page.waitForTimeout(2000);

    const detailBtn = page.locator('button:has-text("详情")').first();
    if (await detailBtn.isVisible()) {
      await detailBtn.click();
      const drawer = page.locator('.n-drawer:has-text("合同详情")');
      await expect(drawer).toBeVisible({ timeout: 5000 });
    }
  });

  test('TC-CON-PAGE-004: 重置搜索', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/contract`);
    await page.waitForSelector('.n-data-table');

    await page.locator('input[placeholder="合同编号"]').fill('CONTRACT123');
    await page.locator('button:has-text("重置")').click();

    const searchInput = page.locator('input[placeholder="合同编号"]');
    await expect(searchInput).toHaveValue('');
  });
});
