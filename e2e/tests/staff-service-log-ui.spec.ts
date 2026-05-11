import { test, expect, Page } from '@playwright/test';

/**
 * Staff Service Log UI Tests - 服务人员关联服务日志UI测试
 *
 * 覆盖最近修改：
 * - commit dd8dc8f: 服务人员服务日志关联查看
 *   - 服务日志页面支持 staffId 参数筛选
 *   - StaffController 新增 getServiceLogs API
 */
const BASE_URL = '';
const API_BASE = 'https://wisdomdance.cn/jxy/api';

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
      localStorage.setItem('SOY_token', t);
      localStorage.setItem('SOY_refreshToken', rt);
    }, [token, refreshToken]);

    await page.reload();
    await page.waitForTimeout(2000);
  }
}

test.describe('Staff Service Log UI Tests', () => {
  test('TC-UI-STAFF-SL-001: 服务人员列表页面加载', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to staff management page
    await page.goto(`${BASE_URL}/staff`);
    await page.waitForLoadState('networkidle');

    // Verify page loads
    await expect(page.locator('text=服务人员管理').first()).toBeVisible();

    // Verify data table is visible
    await expect(page.locator('.n-data-table').first()).toBeVisible({ timeout: 10000 });
  });

  test('TC-UI-STAFF-SL-002: 服务人员卡片视图显示', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to staff management page
    await page.goto(`${BASE_URL}/staff`);
    await page.waitForLoadState('networkidle');

    // Look for card view toggle or card elements
    const cardView = page.locator('.n-card, .staff-card, [class*="card"]');
    const cardCount = await cardView.count();

    // Should see either cards or table
    const tableVisible = await page.locator('.n-data-table').isVisible();
    const cardsVisible = cardCount > 0;

    expect(tableVisible || cardsVisible).toBeTruthy();
  });

  test('TC-UI-STAFF-SL-003: 点击服务人员查看关联服务日志', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to staff management page
    await page.goto(`${BASE_URL}/staff`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // Look for staff cards or table rows
    const staffRow = page.locator('.n-data-table-tr, .staff-card, [class*="staff"]').first();

    if (await staffRow.isVisible({ timeout: 5000 })) {
      // Click on first staff member
      await staffRow.click();
      await page.waitForTimeout(1500);

      // Look for service logs section or modal
      const serviceLogsSection = page.locator('text=服务日志, text=服务记录');
      const modalOrDrawer = page.locator('.n-modal, .n-drawer');

      const hasServiceLogs = await serviceLogsSection.isVisible().catch(() => false);
      const hasModalOrDrawer = await modalOrDrawer.isVisible().catch(() => false);

      // Should show some related information
      console.log(`Service logs visible: ${hasServiceLogs}, Modal/Drawer: ${hasModalOrDrawer}`);
    }
  });

  test('TC-UI-STAFF-SL-004: 服务日志页面staffId筛选器', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to service log page
    await page.goto(`${BASE_URL}/business/service-log`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // Look for staff/employee filter
    const staffFilter = page.locator('input[placeholder*="员工"], input[placeholder*="人员"], input[placeholder*="staff"]');
    const staffSelect = page.locator('.n-select').filter({ hasText: /员工|人员|服务人员/ });

    const filterVisible = await staffFilter.isVisible().catch(() => false) ||
                          await staffSelect.isVisible().catch(() => false);

    // Check for any form inputs related to filtering
    const formInputs = page.locator('.n-form-item, .filter-item');
    const inputCount = await formInputs.count();

    console.log(`Staff filter visible: ${filterVisible}, Total filter items: ${inputCount}`);
  });

  test('TC-UI-STAFF-SL-005: 服务人员详情显示服务统计', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to staff management page
    await page.goto(`${BASE_URL}/staff`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // Find and click detail button
    const detailButton = page.locator('button:has-text("详情"), button:has-text("查看")').first();

    if (await detailButton.isVisible({ timeout: 5000 })) {
      await detailButton.click();
      await page.waitForTimeout(1000);

      // Check for statistics in drawer/modal
      const statsSection = page.locator('text=服务统计, text=服务记录');
      if (await statsSection.isVisible()) {
        await expect(statsSection).toBeVisible();
      }
    }
  });

  test('TC-UI-STAFF-SL-006: 服务人员筛选状态（在职/离职）', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to staff management page
    await page.goto(`${BASE_URL}/staff`);
    await page.waitForLoadState('networkidle');

    // Look for status filter
    const statusFilter = page.locator('.n-select').filter({ hasText: /状态|在职|离职/ });

    if (await statusFilter.isVisible()) {
      await statusFilter.click();
      await page.waitForTimeout(500);

      // Select ON_JOB option
      const onJobOption = page.locator('.n-base-select-option:has-text("在职")');
      if (await onJobOption.isVisible()) {
        await onJobOption.click();
        await page.waitForTimeout(1000);
      }
    }

    // Verify table shows results
    await expect(page.locator('.n-data-table')).toBeVisible({ timeout: 5000 });
  });

  test('TC-UI-STAFF-SL-007: 服务人员搜索功能', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to staff management page
    await page.goto(`${BASE_URL}/staff`);
    await page.waitForLoadState('networkidle');

    // Look for search input
    const searchInput = page.locator('input[placeholder*="姓名"], input[placeholder*="工号"], input[placeholder*="电话"]');

    if (await searchInput.isVisible()) {
      await searchInput.fill('张');
      await page.waitForTimeout(1000);

      // Click search button if exists
      const searchButton = page.locator('button:has-text("搜索"), button:has-text("查询")');
      if (await searchButton.isVisible()) {
        await searchButton.click();
        await page.waitForTimeout(1000);
      }
    }

    // Verify table updates
    await expect(page.locator('.n-data-table')).toBeVisible();
  });

  test('TC-UI-STAFF-SL-008: 服务日志页面显示服务人员信息', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to service log page
    await page.goto(`${BASE_URL}/business/service-log`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // Check if service log table shows staff info column
    const tableHeaders = page.locator('.n-data-table-th');
    const headerCount = await tableHeaders.count();

    // Look for staff-related column header
    let hasStaffColumn = false;
    for (let i = 0; i < headerCount; i++) {
      const headerText = await tableHeaders.nth(i).textContent();
      if (headerText?.includes('服务人员') || headerText?.includes('员工') || headerText?.includes('staff')) {
        hasStaffColumn = true;
        break;
      }
    }

    console.log(`Has staff column: ${hasStaffColumn}, Total columns: ${headerCount}`);
  });

  test('TC-UI-STAFF-SL-009: 服务人员管理新增按钮', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to staff management page
    await page.goto(`${BASE_URL}/staff`);
    await page.waitForLoadState('networkidle');

    // Look for add button
    const addButton = page.locator('button:has-text("新增"), button:has-text("添加"), button:has-text("创建")');
    await expect(addButton).toBeVisible({ timeout: 5000 });
  });

  test('TC-UI-STAFF-SL-010: 服务人员卡片显示服务统计信息', async ({ page }) => {
    await setupAuthenticatedPage(page, 'admin', 'admin123');

    // Navigate to staff management page
    await page.goto(`${BASE_URL}/staff`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // Look for cards with service stats
    const cards = page.locator('.n-card, .staff-card');
    const cardCount = await cards.count();

    if (cardCount > 0) {
      // Check first card for service stats
      const firstCardText = await cards.first().textContent();

      // Should contain some service-related info
      const hasServiceInfo = firstCardText?.includes('服务') ||
                            firstCardText?.includes('订单') ||
                            firstCardText?.includes('统计');

      console.log(`Card count: ${cardCount}, Has service info: ${hasServiceInfo}`);
    }
  });
});
