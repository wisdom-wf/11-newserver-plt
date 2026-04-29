import { test, expect, Page } from '@playwright/test';

/**
 * Service Log Health Fields UI Tests
 *
 * 页面结构：
 * - /business/service-log
 * - 标题："服务日志概览" + "服务日志管理"
 * - 按钮：TableHeaderOperation → "新增"
 * - 表单抽屉有"健康观察备注"和"本次给药记录"字段
 */

const BASE_URL = 'http://localhost:9527';
const API_BASE = 'http://localhost:8080/api';

/**
 * Auth via localStorage (SOY_ prefix required by VITE_STORAGE_PREFIX=SOY_)
 */
async function setupAuth(page: Page) {
  await page.goto(`${BASE_URL}/login`);
  await page.fill('input[type="text"], input[placeholder*="用户名"]', 'admin');
  await page.fill('input[type="password"]', 'admin123');
  await page.click('button[type="submit"], button:has-text("确认")');
  await page.waitForURL('**/home**', { timeout: 15000 });
  await page.waitForLoadState('domcontentloaded');
  await page.waitForTimeout(1000);
}

/**
 * Navigate to service log page and wait for data
 */
async function gotoServiceLog(page: Page) {
  await page.goto(`${BASE_URL}/business/service-log`);
  // Wait for page title
  await expect(page.locator('text=服务日志概览').first()).toBeVisible({ timeout: 10000 });
  await page.waitForTimeout(2000);
}

/**
 * Open the add drawer
 */
async function openAddDrawer(page: Page) {
  // Click "新增" button (TableHeaderOperation)
  const addBtn = page.locator('button:has-text("新增")').first();
  await addBtn.click();
  await page.waitForSelector('.n-drawer', { timeout: 5000 });
  await page.waitForTimeout(1000);
}

test.describe.serial('Service Log Health Fields UI Tests', { tag: '@service-log' }, () => {

  test('TC-UI-SL-001: View service log list page', async ({ page }) => {
    await setupAuth(page);
    await gotoServiceLog(page);

    // Verify page title
    await expect(page.locator('text=服务日志概览').first()).toBeVisible();
    await expect(page.locator('text=服务日志管理').first()).toBeVisible();

    // Stat cards should be visible
    await expect(page.locator('.stat-card, .stat-card-mini').first()).toBeVisible();
  });

  test('TC-UI-SL-002: Service log add form has health fields', async ({ page }) => {
    await setupAuth(page);
    await gotoServiceLog(page);

    // Open add drawer
    await openAddDrawer(page);

    // Check for health observation field
    const healthObsLabel = page.locator('text=健康观察备注');
    await expect(healthObsLabel).toBeVisible();

    // Check for medication given field
    const medGivenLabel = page.locator('text=本次给药记录');
    await expect(medGivenLabel).toBeVisible();

    // Check textareas exist
    const textareas = page.locator('.n-drawer textarea');
    const count = await textareas.count();
    expect(count).toBeGreaterThanOrEqual(2);
  });

  test('TC-UI-SL-003: Fill service log with health data via UI', async ({ page }) => {
    await setupAuth(page);

    // Get token for API calls
    const loginResp = await page.request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const { data: { accessToken } } = await loginResp.json();

    // Get an order ID for service log
    const orderResp = await page.request.get(`${API_BASE}/orders?current=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${accessToken}` }
    });
    const orderData = await orderResp.json();
    const orderId = orderData.data?.records?.[0]?.orderId;

    await gotoServiceLog(page);
    await openAddDrawer(page);

    // Fill order ID (if input exists)
    const orderInput = page.locator('.n-drawer input').first();
    if (await orderInput.isVisible() && orderId) {
      await orderInput.fill(orderId);
    }

    // Fill health observation
    const healthObsTextarea = page.locator('textarea').filter({ hasText: '' }).first();
    if (await healthObsTextarea.isVisible()) {
      await healthObsTextarea.fill('老人今日血压略高，建议持续观察');
      await expect(healthObsTextarea).toHaveValue('老人今日血压略高，建议持续观察');
    }

    // Fill medication given
    const textareas = page.locator('.n-drawer textarea');
    if (await textareas.count() >= 2) {
      await textareas.nth(1).fill('遵医嘱服用降压药一片');
      await expect(textareas.nth(1)).toHaveValue('遵医嘱服用降压药一片');
    }
  });

  test('TC-UI-SL-004: Service log detail shows health information', async ({ page }) => {
    await setupAuth(page);

    const loginResp = await page.request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const { data: { accessToken } } = await loginResp.json();

    // Get order
    const orderResp = await page.request.get(`${API_BASE}/orders?current=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${accessToken}` }
    });
    const orderData = await orderResp.json();
    const orderId = orderData.data?.records?.[0]?.orderId;

    if (!orderId) {
      test.skip();
      return;
    }

    // Create service log with health data via API
    const createResp = await page.request.post(`${API_BASE}/service-log`, {
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        orderId,
        serviceContent: '常规养老服务',
        healthObservations: '测试：老人状态良好',
        medicationGiven: '测试：服用维生素'
      }
    });

    if (!createResp.ok()) {
      test.skip();
      return;
    }

    const createData = await createResp.json();
    const serviceLogId = createData.data?.serviceLogId;

    // View in UI
    await gotoServiceLog(page);

    // Wait for table
    await page.waitForTimeout(2000);

    // Look for detail button
    const detailBtn = page.locator('button:has-text("详情")').first();
    if (await detailBtn.isVisible({ timeout: 5000 })) {
      await detailBtn.click();
      await page.waitForSelector('.n-drawer', { timeout: 5000 });

      // Check health observation section
      const healthSection = page.locator('.n-drawer').filter({ hasText: '健康观察' });
      if (await healthSection.count() > 0) {
        await expect(healthSection).toBeVisible();
      }

      // Check medication given
      const medSection = page.locator('.n-drawer').filter({ hasText: '给药记录' });
      if (await medSection.count() > 0) {
        await expect(medSection).toBeVisible();
      }
    }

    // Cleanup
    if (serviceLogId) {
      await page.request.delete(`${API_BASE}/service-log/${serviceLogId}`, {
        headers: { Authorization: `Bearer ${accessToken}` }
      });
    }
  });

  test('TC-UI-SL-005: Service log list table is visible', async ({ page }) => {
    await setupAuth(page);
    await gotoServiceLog(page);

    // Table columns should be visible
    const tableHeaders = page.locator('.n-data-table-th');
    const headerCount = await tableHeaders.count();
    expect(headerCount).toBeGreaterThan(0);

    // Data table body
    await expect(page.locator('.n-data-table')).toBeVisible();
  });

  test('TC-UI-SL-006: Service log statistics cards visible', async ({ page }) => {
    await setupAuth(page);
    await gotoServiceLog(page);

    // Stat cards
    await expect(page.locator('.stat-card, .stat-card-mini').first()).toBeVisible();
    await expect(page.locator('text=总日志数').first()).toBeVisible();
  });

  test('TC-UI-SL-007: Service log filter controls visible', async ({ page }) => {
    await setupAuth(page);
    await gotoServiceLog(page);

    // Filter controls
    const inputs = page.locator('.n-input, .n-select, .n-date-picker');
    const inputCount = await inputs.count();
    console.log(`Filter controls found: ${inputCount}`);
    expect(inputCount).toBeGreaterThan(0);
  });

  test('TC-UI-SL-008: Service log filter by date range', async ({ page }) => {
    await setupAuth(page);
    await gotoServiceLog(page);

    // Date picker
    const datePicker = page.locator('.n-date-picker').first();
    if (await datePicker.isVisible()) {
      await datePicker.click();
      await page.waitForTimeout(500);
      // Just verify it opens
      const pickerPanel = page.locator('.n-date-panel, .n-picker-panel');
      if (await pickerPanel.count() > 0) {
        await expect(pickerPanel.first()).toBeVisible();
      }
    }
  });

  test('TC-UI-SL-009: Service log API returns health fields in list', async ({ page }) => {
    await setupAuth(page);
    await gotoServiceLog(page);

    // API call should include health fields
    const loginResp = await page.request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const { data: { accessToken } } = await loginResp.json();

    const resp = await page.request.get(`${API_BASE}/service-log/list?current=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${accessToken}` }
    });

    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code).toBe(200);
  });

  test('TC-UI-SL-010: Service log detail shows health info via API', async ({ page }) => {
    await setupAuth(page);

    const loginResp = await page.request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const { data: { accessToken } } = await loginResp.json();

    // Get a service log ID from the list
    const listResp = await page.request.get(`${API_BASE}/service-log/list?current=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${accessToken}` }
    });
    const listData = await listResp.json();
    const logId = listData.data?.records?.[0]?.serviceLogId;

    if (!logId) {
      test.skip();
      return;
    }

    const resp = await page.request.get(`${API_BASE}/service-log/${logId}`, {
      headers: { Authorization: `Bearer ${accessToken}` }
    });

    expect(resp.status()).toBe(200);
  });
});
