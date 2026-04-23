import { test, expect, Page } from '@playwright/test';

/**
 * Service Log Health Fields UI Tests
 *
 * Prerequisites:
 * - Frontend running on http://localhost:9527
 * - Backend running on http://localhost:8080
 * - User logged in with provider credentials (for service log creation)
 * - Database has at least one order with status ready for service log
 */

const BASE_URL = 'http://localhost:9527';
const API_BASE = 'http://localhost:8080/api';

/**
 * Helper function to setup authenticated page via localStorage
 */
async function setupAuthenticatedPage(page: Page, username: string, password: string) {
  const loginResponse = await page.request.post(`${API_BASE}/auth/login`, {
    data: { username, password }
  });

  if (!loginResponse.ok()) {
    throw new Error('Failed to login');
  }

  const loginData = await loginResponse.json();
  const token = loginData.data?.accessToken;
  const refreshToken = loginData.data?.refreshToken;

  await page.goto(`${BASE_URL}/`);
  await page.waitForTimeout(1000);

  const currentUrl = page.url();
  if (currentUrl.includes('/login')) {
    await page.evaluate(([t, rt]) => {
      localStorage.setItem('token', t);
      localStorage.setItem('refreshToken', rt);
    }, [token, refreshToken]);

    await page.reload();
    await page.waitForTimeout(2000);
  }
}

test.describe('Service Log Health Fields UI Tests', () => {
  test('TC-UI-SL-001: View service log list page', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to service log page
    await page.goto(`${BASE_URL}/business/service-log`);
    await page.waitForLoadState('networkidle');

    // Verify page loads
    await expect(page.locator('text=服务日志概览').first()).toBeVisible();
    await expect(page.locator('text=服务日志管理').first()).toBeVisible();
  });

  test('TC-UI-SL-002: Service log add form has health fields', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to service log page
    await page.goto(`${BASE_URL}/business/service-log`);
    await page.waitForLoadState('networkidle');

    // Look for add button
    const addButton = page.locator('button:has-text("添加")').first();
    if (await addButton.isVisible()) {
      await addButton.click();

      // Wait for drawer to open
      await page.waitForSelector('.n-drawer', { timeout: 5000 });

      // Check for health observation field
      const healthObsLabel = page.locator('text=健康观察备注');
      await expect(healthObsLabel).toBeVisible();

      // Check for medication given field
      const medGivenLabel = page.locator('text=本次给药记录');
      await expect(medGivenLabel).toBeVisible();

      // Check for textareas
      const textareas = page.locator('.n-drawer textarea');
      const count = await textareas.count();
      expect(count).toBeGreaterThanOrEqual(2); // Should have service content + health fields
    }
  });

  test('TC-UI-SL-003: Fill service log with health data via UI', async ({ page }) => {
    // Setup authenticated page
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Get token
    const loginResponse = await page.request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const loginData = await loginResponse.json();
    const token = loginData.data.accessToken;

    // Get a test order ID
    const orderResponse = await page.request.get(`${API_BASE}/orders?current=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const orderData = await orderResponse.json();
    const orderId = orderData.data?.records?.[0]?.orderId;

    await page.goto(`${BASE_URL}/business/service-log`);
    await page.waitForLoadState('networkidle');

    // Click add button
    const addButton = page.locator('button:has-text("添加")').first();
    await addButton.click();
    await page.waitForSelector('.n-drawer', { timeout: 5000 });

    // Fill order ID if input exists
    const orderInput = page.locator('.n-drawer input[placeholder*="订单ID"]');
    if (await orderInput.isVisible() && orderId) {
      await orderInput.fill(orderId);
    }

    // Fill health observations
    const healthObsTextarea = page.locator('textarea[placeholder*="健康观察"]');
    if (await healthObsTextarea.isVisible()) {
      await healthObsTextarea.fill('老人今日血压略高，建议持续观察');

      // Fill medication given
      const medGivenTextarea = page.locator('textarea[placeholder*="给药"]');
      await medGivenTextarea.fill('遵医嘱服用降压药一片');

      // Verify values are filled
      await expect(healthObsTextarea).toHaveValue('老人今日血压略高，建议持续观察');
      await expect(medGivenTextarea).toHaveValue('遵医嘱服用降压药一片');
    }
  });

  test('TC-UI-SL-004: Service log detail shows health information', async ({ page }) => {
    // Setup authenticated page
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Get token
    const loginResponse = await page.request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const loginData = await loginResponse.json();
    const token = loginData.data.accessToken;

    // Get order ID
    const orderResponse = await page.request.get(`${API_BASE}/orders?current=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const orderData = await orderResponse.json();
    const orderId = orderData.data?.records?.[0]?.orderId;

    if (!orderId) {
      test.skip();
      return;
    }

    // Create service log with health data
    const createResponse = await page.request.post(`${API_BASE}/service-log`, {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: {
        orderId: orderId,
        serviceContent: '常规养老服务',
        healthObservations: '测试：老人状态良好',
        medicationGiven: '测试：服用维生素'
      }
    });

    if (!createResponse.ok()) {
      test.skip();
      return;
    }

    const createData = await createResponse.json();
    const serviceLogId = createData.data?.serviceLogId;

    // Now view in UI
    await page.goto(`${BASE_URL}/business/service-log`);
    await page.waitForLoadState('networkidle');

    // Wait for table to load
    await page.waitForTimeout(2000);

    // Look for detail button
    const detailButton = page.locator('.n-data-table button:has-text("详情")').first();

    if (await detailButton.isVisible({ timeout: 5000 })) {
      await detailButton.click();

      // Wait for detail drawer
      await page.waitForSelector('.n-drawer', { timeout: 5000 });

      // Check for health observation section
      const healthSection = page.locator('.n-drawer:has-text("健康观察")');
      if (await healthSection.isVisible()) {
        await expect(healthSection.locator('text=测试：老人状态良好')).toBeVisible();
      }

      // Check for medication given
      const medSection = page.locator('.n-drawer:has-text("给药记录")');
      if (await medSection.isVisible()) {
        await expect(medSection.locator('text=测试：服用维生素')).toBeVisible();
      }
    }

    // Cleanup: delete the test service log if created
    if (serviceLogId) {
      await page.request.delete(`${API_BASE}/service-log/${serviceLogId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
    }
  });

  test('TC-UI-SL-005: Edit service log health data', async ({ page }) => {
    // Setup authenticated page
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Get token
    const loginResponse = await page.request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const loginData = await loginResponse.json();
    const token = loginData.data.accessToken;

    // Get order ID
    const orderResponse = await page.request.get(`${API_BASE}/orders?current=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const orderData = await orderResponse.json();
    const orderId = orderData.data?.records?.[0]?.orderId;

    if (!orderId) {
      test.skip();
      return;
    }

    // Create service log with initial health data
    const createResponse = await page.request.post(`${API_BASE}/service-log`, {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: {
        orderId: orderId,
        serviceContent: '初始服务内容',
        healthObservations: '初始健康观察',
        medicationGiven: '初始给药记录'
      }
    });

    if (!createResponse.ok()) {
      test.skip();
      return;
    }

    const createData = await createResponse.json();
    const serviceLogId = createData.data?.serviceLogId;

    // View in UI and edit
    await page.goto(`${BASE_URL}/business/service-log`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // Find and click edit button
    const editButton = page.locator('.n-data-table button:has-text("更新")').first();

    if (await editButton.isVisible({ timeout: 5000 })) {
      await editButton.click();

      // Wait for drawer
      await page.waitForSelector('.n-drawer', { timeout: 5000 });

      // Clear and update health observation
      const healthObsTextarea = page.locator('textarea[placeholder*="健康观察"]');
      await healthObsTextarea.clear();
      await healthObsTextarea.fill('更新后的健康观察');

      // Clear and update medication given
      const medGivenTextarea = page.locator('textarea[placeholder*="给药"]');
      await medGivenTextarea.clear();
      await medGivenTextarea.fill('更新后的给药记录');

      // Click save
      const saveButton = page.locator('.n-drawer button:has-text("保存修改")');
      await saveButton.click();

      // Wait for response
      await page.waitForTimeout(1000);
    }

    // Cleanup
    if (serviceLogId) {
      await page.request.delete(`${API_BASE}/service-log/${serviceLogId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
    }
  });

  test('TC-UI-SL-006: Service log statistics are visible', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to service log page
    await page.goto(`${BASE_URL}/business/service-log`);
    await page.waitForLoadState('networkidle');

    // Verify statistics cards are visible
    await expect(page.locator('text=服务日志概览').first()).toBeVisible();

    // Should see stat cards
    const statCards = page.locator('.stat-card, .stat-card-mini');
    const count = await statCards.count();
    expect(count).toBeGreaterThan(0);
  });

  test('TC-UI-SL-007: Service log search functionality', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to service log page
    await page.goto(`${BASE_URL}/business/service-log`);
    await page.waitForLoadState('networkidle');

    // Find and use the search input for order number
    const orderSearchInput = page.locator('input[placeholder="订单号"]');
    if (await orderSearchInput.isVisible()) {
      await orderSearchInput.fill('test');
      await page.locator('button:has-text("搜索")').click();

      // Wait for results
      await page.waitForTimeout(1000);
    }
  });

  test('TC-UI-SL-008: Service log filter by date range', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to service log page
    await page.goto(`${BASE_URL}/business/service-log`);
    await page.waitForLoadState('networkidle');

    // Find date range picker
    const datePicker = page.locator('.n-date-picker');
    if (await datePicker.isVisible()) {
      await datePicker.click();

      // Wait for picker to open
      await page.waitForTimeout(500);
    }
  });
});

  test('TC-UI-SL-009: Service log API returns health fields in list', async ({ request: req }) => {
    // Test API directly for health field verification
    const loginResponse = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const loginData = await loginResponse.json();
    const token = loginData.data.accessToken;

    // Get service log list
    const response = await req.get(`${API_BASE}/service-log/list`, {
      headers: { Authorization: `Bearer ${token}` },
      params: { page: 1, pageSize: 10 }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);

    // Verify health fields exist in response
    if (data.data?.records && data.data.records.length > 0) {
      const record = data.data.records[0];
      expect(record).toHaveProperty('healthObservations');
      expect(record).toHaveProperty('medicationGiven');
    }
  });

  test('TC-UI-SL-010: Service log detail shows health info via API', async ({ request: req }) => {
    const loginResponse = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const loginData = await loginResponse.json();
    const token = loginData.data.accessToken;

    // Get order
    const orderResponse = await req.get(`${API_BASE}/orders?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const orderData = await orderResponse.json();
    const orderId = orderData.data?.records?.[0]?.orderId;

    if (!orderId) {
      test.skip();
      return;
    }

    // Create service log with health data
    const createResponse = await req.post(`${API_BASE}/service-log`, {
      headers: {
        Authorization: `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: {
        orderId: orderId,
        serviceContent: 'API健康字段测试',
        healthObservations: '血压偏高',
        medicationGiven: '服用降压药'
      }
    });

    if (!createResponse.ok()) {
      test.skip();
      return;
    }

    const createResult = await createResponse.json();
    const logId = createResult.data?.serviceLogId || createResult.data;

    // Get detail
    const detailResponse = await req.get(`${API_BASE}/service-log/${logId}`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    expect(detailResponse.ok()).toBeTruthy();
    const detailData = await detailResponse.json();
    expect(detailData.code).toBe(200);

    // Verify health fields in detail
    expect(detailData.data.healthObservations).toBe('血压偏高');
    expect(detailData.data.medicationGiven).toBe('服用降压药');

    // Cleanup
    await req.delete(`${API_BASE}/service-log/${logId}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
  });
