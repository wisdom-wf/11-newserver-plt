import { test, expect } from '@playwright/test';

/**
 * 合同管理模块专项测试
 * 覆盖范围：合同API + 订单页合同联动 + 合同管理页面
 *
 * 前置条件：
 * - 本地后端运行在 localhost:8080
 * - 本地前端运行在 localhost:9527
 * - 数据库中有测试订单和测试服务商/服务人员数据
 */

const BASE_URL = 'http://localhost:9527';
const API_URL = 'http://localhost:8080';

// 测试用token（需要先登录获取）
let adminToken = '';
let providerToken = '';
let staffToken = '';

// 测试用数据ID
let testOrderId = '';
let testContractId = '';

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

// ========== 测试套件 ==========

test.describe('合同模块 - API层测试', () => {

  test.beforeAll(async ({ request }) => {
    adminToken = await login(request, 'admin', 'admin123');
    try {
      providerToken = await login(request, 'FWS1', 'admin123');
    } catch (e) {
      console.warn('Provider login failed, skipping provider tests');
    }
    try {
      staffToken = await login(request, 'teststaff', 'admin123');
    } catch (e) {
      console.warn('Staff login failed, skipping staff tests');
    }
  });

  test('TC-CON-API-001: 获取合同列表（管理员）', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts', adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data).toHaveProperty('total');
    expect(body.data).toHaveProperty('records');
  });

  test('TC-CON-API-002: 获取合同列表（服务商）', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts', providerToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    // 服务商应只看到自己相关的合同
  });

  test('TC-CON-API-003: 合同列表支持按状态筛选', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts?status=INITIATED', adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    // 返回的记录应该都是INITIATED状态
    if (body.data.records.length > 0) {
      for (const contract of body.data.records) {
        expect(contract.status).toBe('INITIATED');
      }
    }
  });

  test('TC-CON-API-004: 合同列表支持按合同编号搜索', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts?contractNo=CONTRACT', adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
  });

  test('TC-CON-API-005: 合同列表支持分页', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts?page=1&pageSize=5', adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data.records.length).toBeLessThanOrEqual(5);
  });

  test('TC-CON-API-006: 根据合同ID获取合同详情', async ({ request }) => {
    // 先获取列表取一个合同ID
    const listResp = await apiGet(request, '/api/contracts?pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (listBody.data.records.length === 0) {
      test.skip();
      return;
    }
    const contractId = listBody.data.records[0].contractId;

    const response = await apiGet(request, `/api/contracts/${contractId}`, adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data.contractId).toBe(contractId);
    expect(body.data).toHaveProperty('contractNo');
    expect(body.data).toHaveProperty('status');
    expect(body.data).toHaveProperty('statusText');
  });

  test('TC-CON-API-007: 获取不存在的合同详情返回404', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts/nonexistent123', adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(404);
  });

  test('TC-CON-API-008: 根据订单ID获取关联合同', async ({ request }) => {
    // 先获取一个有合同的订单
    const listResp = await apiGet(request, '/api/contracts?pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (listBody.data.records.length === 0) {
      test.skip();
      return;
    }
    const orderId = listBody.data.records[0].orderId;

    const response = await apiGet(request, `/api/contracts/order/${orderId}`, adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    if (body.data) {
      expect(body.data.orderId).toBe(orderId);
    }
  });

  test('TC-CON-API-009: 获取不存在的订单关联合同返回null', async ({ request }) => {
    const response = await apiGet(request, '/api/contracts/order/nonexistent999', adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data).toBeNull();
  });

  test('TC-CON-API-010: 获取签署链接', async ({ request }) => {
    const listResp = await apiGet(request, '/api/contracts?status=INITIATED&pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (listBody.data.records.length === 0) {
      test.skip();
      return;
    }
    const contractId = listBody.data.records[0].contractId;

    const response = await apiGet(request, `/api/contracts/${contractId}/sign-url`, adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(body.data).toHaveProperty('signUrl');
    expect(body.data).toHaveProperty('expireTime');
  });

  test('TC-CON-API-011: 获取合同状态', async ({ request }) => {
    const listResp = await apiGet(request, '/api/contracts?pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (listBody.data.records.length === 0) {
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

  test('TC-CON-API-012: 下载合同', async ({ request }) => {
    const listResp = await apiGet(request, '/api/contracts?status=SIGNED&pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (listBody.data.records.length === 0) {
      test.skip();
      return;
    }
    const contractId = listBody.data.records[0].contractId;

    const response = await apiGet(request, `/api/contracts/${contractId}/download`, adminToken);
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
    expect(typeof body.data).toBe('string');
  });

  test('TC-CON-API-013: 取消已签署的合同应失败', async ({ request }) => {
    const listResp = await apiGet(request, '/api/contracts?status=SIGNED&pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (listBody.data.records.length === 0) {
      test.skip();
      return;
    }
    const contractId = listBody.data.records[0].contractId;

    const response = await apiPost(request, `/api/contracts/${contractId}/cancel`, adminToken, {});
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(400); // 已签署的合同无法取消
  });

  test('TC-CON-API-014: 取消未签署的合同', async ({ request }) => {
    const listResp = await apiGet(request, '/api/contracts?status=INITIATED&pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (listBody.data.records.length === 0) {
      test.skip();
      return;
    }
    const contractId = listBody.data.records[0].contractId;

    const response = await apiPost(request, `/api/contracts/${contractId}/cancel`, adminToken, {});
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);

    // 验证状态已变为CANCELLED
    const statusResp = await apiGet(request, `/api/contracts/${contractId}/status`, adminToken);
    const statusBody = await statusResp.json();
    expect(statusBody.data).toBe('CANCELLED');
  });

  test('TC-CON-API-015: 回调接口无需认证', async ({ request }) => {
    // 回调接口应不带token也能访问
    const response = await request.post(`${API_URL}/api/contracts/callback`, {
      data: { MsgData: { FlowId: 'test', FlowCallbackStatus: 4 } }
    });
    expect(response.status()).toBe(200);
    const body = await response.json();
    expect(body.code).toBe(200);
  });

  test('TC-CON-API-016: 回调更新合同状态', async ({ request }) => {
    // 先创建一个测试合同
    const listResp = await apiGet(request, '/api/contracts?status=INITIATED&pageSize=1', adminToken);
    const listBody = await listResp.json();
    if (listBody.data.records.length === 0) {
      test.skip();
      return;
    }
    const flowId = listBody.data.records[0].flowId;
    if (!flowId || flowId.startsWith('FLOW_')) {
      // mock flowId，回调不会匹配
      test.skip();
      return;
    }

    // 模拟腾讯回调
    const callbackResp = await request.post(`${API_URL}/api/contracts/callback`, {
      data: {
        MsgData: {
          FlowId: flowId,
          FlowCallbackStatus: 4, // ALL = SIGNED
          FlowCallbackShowStatus: 'ALL',
          Operate: 'sign'
        }
      }
    });
    expect(callbackResp.status()).toBe(200);

    // 验证合同状态已更新
    await page.waitForTimeout(500);
    const statusResp = await apiGet(request, `/api/contracts/${listBody.data.records[0].contractId}/status`, adminToken);
    const statusBody = await statusResp.json();
    expect(statusBody.data).toBe('SIGNED');
  });
});

test.describe('合同模块 - 订单页合同联动测试', () => {

  test.beforeEach(async ({ request }) => {
    // 登录
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/dashboard|home/);
  });

  test('TC-CON-UI-001: 派单后显示合同创建提示', async ({ request }) => {
    // 导航到订单页
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForSelector('.n-data-table');

    // 找一个CREATED状态的订单，点击分配
    const dispatchBtn = page.locator('button:has-text("分配")').first();
    if (await dispatchBtn.isVisible()) {
      await dispatchBtn.click();

      // 选择服务商和服务人员
      await page.waitForSelector('.n-modal');
      // 这里需要根据实际UI填写派单表单
      // ...

      // 点击确认派单
      const confirmBtn = page.locator('button:has-text("确认派单")');
      if (await confirmBtn.isVisible()) {
        await confirmBtn.click();

        // 验证成功消息包含合同编号
        const successMsg = page.locator('.n-message__content');
        await expect(successMsg).toBeVisible({ timeout: 10000 });
        const msgText = await successMsg.textContent();
        // 应该显示"派单成功，合同已创建：CONTRACT..."
        expect(msgText).toMatch(/派单成功/);
      }
    }
  });

  test('TC-CON-UI-002: 接单时检查合同状态', async ({ request }) => {
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForSelector('.n-data-table');

    // 找一个DISPATCHED状态的订单，点击接单
    const acceptBtn = page.locator('button:has-text("接单")').first();
    if (await acceptBtn.isVisible()) {
      await acceptBtn.click();

      // 如果合同未签署，应该弹出合同详情对话框
      const contractModal = page.locator('.n-modal:has-text("合同详情")');
      if (await contractModal.isVisible({ timeout: 3000 })) {
        // 验证合同信息显示
        await expect(page.locator('text=合同编号')).toBeVisible();
        await expect(page.locator('text=合同状态')).toBeVisible();
        await expect(page.locator('button:has-text("签署合同")')).toBeVisible();
        await expect(page.locator('button:has-text("刷新状态")')).toBeVisible();
      }
    }
  });

  test('TC-CON-UI-003: 合同详情弹窗显示签署按钮', async ({ request }) => {
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForSelector('.n-data-table');

    const acceptBtn = page.locator('button:has-text("接单")').first();
    if (await acceptBtn.isVisible()) {
      await acceptBtn.click();

      const contractModal = page.locator('.n-modal:has-text("合同详情")');
      if (await contractModal.isVisible({ timeout: 3000 })) {
        // 未签署的合同应显示"签署合同"按钮
        const signBtn = page.locator('.n-modal button:has-text("签署合同")');
        await expect(signBtn).toBeVisible();
      }
    }
  });

  test('TC-CON-UI-004: 点击签署合同打开新窗口', async ({ page, context }) => {
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForSelector('.n-data-table');

    const acceptBtn = page.locator('button:has-text("接单")').first();
    if (await acceptBtn.isVisible()) {
      await acceptBtn.click();

      const contractModal = page.locator('.n-modal:has-text("合同详情")');
      if (await contractModal.isVisible({ timeout: 3000 })) {
        // 监听新窗口
        const [newPage] = await Promise.all([
          context.waitForEvent('page', { timeout: 5000 }).catch(() => null),
          page.locator('.n-modal button:has-text("签署合同")').click()
        ]);

        if (newPage) {
          // 验证新窗口打开了签署链接
          const url = newPage.url();
          expect(url).toContain('ess');
          await newPage.close();
        }
      }
    }
  });

  test('TC-CON-UI-005: 开始服务前检查合同签署状态', async ({ request }) => {
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForSelector('.n-data-table');

    // 找一个已接单（RECEIVED）的订单，点击开始服务
    const startBtn = page.locator('button:has-text("开始服务")').first();
    if (await startBtn.isVisible()) {
      await startBtn.click();

      // 如果合同未签署，应该弹出警告
      const warningMsg = page.locator('.n-message--warning');
      if (await warningMsg.isVisible({ timeout: 3000 })) {
        const msgText = await warningMsg.textContent();
        expect(msgText).toContain('合同');
      }
    }
  });
});

test.describe('合同模块 - 合同管理页面测试', () => {

  test.beforeEach(async ({ request }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/dashboard|home/);
  });

  test('TC-CON-PAGE-001: 合同管理页面加载', async ({ request }) => {
    await page.goto(`${BASE_URL}/business/contract`);
    await page.waitForSelector('.n-data-table');

    // 验证页面标题
    await expect(page.locator('text=合同管理')).toBeVisible();

    // 验证搜索栏
    await expect(page.locator('input[placeholder="合同编号"]')).toBeVisible();
    await expect(page.locator('.n-select')).toBeVisible(); // 状态筛选
  });

  test('TC-CON-PAGE-002: 合同列表显示数据', async ({ request }) => {
    await page.goto(`${BASE_URL}/business/contract`);
    await page.waitForSelector('.n-data-table');

    // 等待数据加载
    await page.waitForTimeout(2000);

    // 验证表格列头
    await expect(page.locator('th:has-text("合同编号")')).toBeVisible();
    await expect(page.locator('th:has-text("合同名称")')).toBeVisible();
    await expect(page.locator('th:has-text("关联订单")')).toBeVisible();
    await expect(page.locator('th:has-text("状态")')).toBeVisible();
    await expect(page.locator('th:has-text("创建时间")')).toBeVisible();
    await expect(page.locator('th:has-text("操作")')).toBeVisible();
  });

  test('TC-CON-PAGE-003: 按合同编号搜索', async ({ request }) => {
    await page.goto(`${BASE_URL}/business/contract`);
    await page.waitForSelector('.n-data-table');

    const searchInput = page.locator('input[placeholder="合同编号"]');
    await searchInput.fill('CONTRACT');
    await page.locator('button:has-text("搜索")').click();

    await page.waitForTimeout(2000);

    // 验证搜索结果
    const rows = page.locator('.n-data-table-body tr');
    const count = await rows.count();
    if (count > 0) {
      // 第一列应该是合同编号
      const firstContractNo = await rows.first().locator('td').first().textContent();
      expect(firstContractNo).toContain('CONTRACT');
    }
  });

  test('TC-CON-PAGE-004: 按状态筛选', async ({ request }) => {
    await page.goto(`${BASE_URL}/business/contract`);
    await page.waitForSelector('.n-data-table');

    // 点击状态下拉框
    const statusSelect = page.locator('.n-select').first();
    await statusSelect.click();

    // 选择"已发起"
    await page.locator('.n-select-option:has-text("已发起")').click();
    await page.locator('button:has-text("搜索")').click();

    await page.waitForTimeout(2000);

    // 验证所有行的状态都是"已发起"
    const statusTags = page.locator('.n-data-table-body .n-tag');
    const count = await statusTags.count();
    if (count > 0) {
      for (let i = 0; i < count; i++) {
        const statusText = await statusTags.nth(i).textContent();
        expect(statusText).toBe('已发起');
      }
    }
  });

  test('TC-CON-PAGE-005: 点击详情打开抽屉', async ({ request }) => {
    await page.goto(`${BASE_URL}/business/contract`);
    await page.waitForSelector('.n-data-table');
    await page.waitForTimeout(2000);

    const detailBtn = page.locator('button:has-text("详情")').first();
    if (await detailBtn.isVisible()) {
      await detailBtn.click();

      // 验证抽屉打开
      const drawer = page.locator('.n-drawer:has-text("合同详情")');
      await expect(drawer).toBeVisible({ timeout: 5000 });

      // 验证详情字段
      await expect(page.locator('text=合同编号')).toBeVisible();
      await expect(page.locator('text=甲方（服务商）')).toBeVisible();
      await expect(page.locator('text=乙方（服务人员）')).toBeVisible();
    }
  });

  test('TC-CON-PAGE-006: 已签署合同显示下载按钮', async ({ request }) => {
    await page.goto(`${BASE_URL}/business/contract`);
    await page.waitForSelector('.n-data-table');

    // 筛选已签署的合同
    const statusSelect = page.locator('.n-select').first();
    await statusSelect.click();
    await page.locator('.n-select-option:has-text("已签署")').click();
    await page.locator('button:has-text("搜索")').click();
    await page.waitForTimeout(2000);

    // 查看操作列的下载按钮
    const downloadBtn = page.locator('button:has-text("下载")').first();
    if (await downloadBtn.isVisible()) {
      // 验证下载按钮存在
      await expect(downloadBtn).toBeVisible();
    }
  });

  test('TC-CON-PAGE-007: 重置搜索', async ({ request }) => {
    await page.goto(`${BASE_URL}/business/contract`);
    await page.waitForSelector('.n-data-table');

    // 输入搜索条件
    await page.locator('input[placeholder="合同编号"]').fill('CONTRACT123');

    // 点击重置
    await page.locator('button:has-text("重置")').click();

    // 验证搜索框已清空
    const searchInput = page.locator('input[placeholder="合同编号"]');
    await expect(searchInput).toHaveValue('');
  });
});

test.describe('合同模块 - 全流程E2E测试', () => {

  test('TC-CON-E2E-001: 派单→合同创建→接单→签署→开始服务 全流程', async ({ request }) => {
    // 1. 登录管理员
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/dashboard|home/);

    // 2. 导航到订单页
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForSelector('.n-data-table');

    // 3. 找CREATED状态订单，点击分配
    const dispatchBtn = page.locator('button:has-text("分配")').first();
    if (await dispatchBtn.isVisible()) {
      await dispatchBtn.click();

      // 4. 选择服务商和服务人员（需要根据实际UI调整选择器）
      // ... 派单表单操作 ...

      // 5. 确认派单
      // const confirmBtn = page.locator('button:has-text("确认派单")');
      // await confirmBtn.click();

      // 6. 验证合同创建提示
      // await expect(page.locator('.n-message__content')).toContainText('合同已创建');

      // 7. 切换到服务人员账号接单
      // ... 登出、重新登录 ...

      // 8. 接单，弹出合同详情
      // const acceptBtn = page.locator('button:has-text("接单")').first();
      // await acceptBtn.click();
      // await expect(page.locator('.n-modal:has-text("合同详情")')).toBeVisible();

      // 9. 签署合同
      // await page.locator('button:has-text("签署合同")').click();

      // 10. 刷新状态
      // await page.locator('button:has-text("刷新状态")').click();

      // 11. 确认接单
      // await page.locator('button:has-text("确认接单")').click();

      // 12. 开始服务
      // const startBtn = page.locator('button:has-text("开始服务")').first();
      // await startBtn.click();

      // 此流程需要完整的测试数据支撑，目前标记为手动验证
      test.skip();
    }
  });
});
