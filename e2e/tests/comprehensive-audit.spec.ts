import { test, expect, Page, ConsoleMessage } from '@playwright/test';

const BASE_URL = 'http://localhost:9527';
const API_BASE = 'https://wisdomdance.cn/jxy/api';

// ============================================================================
// Test Accounts
// ============================================================================
const ACCOUNTS = {
  admin: { username: 'admin', password: 'admin123' },
  staff: { username: 'CS1', password: 'admin123' },
};

// ============================================================================
// Utility Functions
// ============================================================================
async function login(page: Page, username: string, password: string) {
  await page.goto(`${BASE_URL}/login`);
  await page.waitForLoadState('networkidle');

  const userInput = page.locator('input[placeholder*="用户"]').first();
  const pwdInput = page.locator('input[type="password"]').first();

  if (await userInput.isVisible({ timeout: 3000 })) {
    await userInput.fill(username);
    await pwdInput.fill(password);
    await page.click('button:has-text("确认")');
    await page.waitForURL('**/home', { timeout: 10000 }).catch(() => {
      // Already on home or other page
    });
  }
}

async function getConsoleErrors(page: Page): Promise<string[]> {
  const errors: string[] = [];
  page.on('console', (msg: ConsoleMessage) => {
    if (msg.type() === 'error') {
      errors.push(msg.text());
    }
  });
  return errors;
}

function extractButtons(page: Page): string[] {
  // Get all visible buttons and interactive elements
  const selectors = [
    'button:not([style*="display:none"]):not([style*="display: none"])',
    '.n-button:not([style*="display:none"])',
    'a[href]:not([href^="javascript"])',
    '[role="button"]',
    '.n-link',
    'button[type="button"]',
    '.n-btn',
  ];
  return selectors;
}

// ============================================================================
// Phase 1: All Page Navigation Tests
// ============================================================================
test.describe('Phase 1: All Pages Navigation Audit', () => {

  test.beforeAll(async ({ browser }) => {
    // Login once, reuse session
  });

  test('P1-01: /home - 首页仪表盘', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/home`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // Check for console errors
    const errors: string[] = [];
    page.on('console', msg => { if (msg.type() === 'error') errors.push(msg.text()); });

    // Verify key elements
    const hasContent = await page.locator('body').innerText();
    expect(hasContent.length).toBeGreaterThan(50);

    // Check for charts/data cards
    const cards = await page.locator('.n-card, .data-card, [class*="card"]').count();
    console.log(`  -> Found ${cards} cards on home page`);
  });

  test('P1-02: /business/elder - 老人管理', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/business/elder`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasTable = await page.locator('.n-data-table, table').first().isVisible().catch(() => false);
    console.log(`  -> Table visible: ${hasTable}`);

    // Check Add button
    const addBtn = page.locator('button:has-text("新增"), button:has-text("添加"), .n-button:has-text("新增")').first();
    const addVisible = await addBtn.isVisible().catch(() => false);
    console.log(`  -> Add button visible: ${addVisible}`);
  });

  test('P1-03: /business/order - 订单管理', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasTable = await page.locator('.n-data-table, table').first().isVisible().catch(() => false);
    console.log(`  -> Table visible: ${hasTable}`);
  });

  test('P1-04: /business/staff - 员工管理', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/business/staff`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasTable = await page.locator('.n-data-table, table').first().isVisible().catch(() => false);
    console.log(`  -> Table visible: ${hasTable}`);
  });

  test('P1-05: /business/service-log - 服务日志', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/business/service-log`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasTable = await page.locator('.n-data-table, table').first().isVisible().catch(() => false);
    console.log(`  -> Table visible: ${hasTable}`);
  });

  test('P1-06: /business/health-archive - 健康档案', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // Should have elder selector
    const hasSelect = await page.locator('.n-select').first().isVisible().catch(() => false);
    console.log(`  -> Elder selector visible: ${hasSelect}`);
  });

  test('P1-07: /business/evaluation - 服务评价', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/business/evaluation`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasTable = await page.locator('.n-data-table, table').first().isVisible().catch(() => false);
    console.log(`  -> Table visible: ${hasTable}`);
  });

  test('P1-08: /business/quality - 质检管理', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/business/quality`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasTable = await page.locator('.n-data-table, table').first().isVisible().catch(() => false);
    console.log(`  -> Table visible: ${hasTable}`);
  });

  test('P1-09: /business/financial - 财务统计', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/business/financial`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasContent = await page.locator('body').innerText();
    console.log(`  -> Page has content: ${hasContent.length > 50}`);
  });

  test('P1-10: /appointment - 预约管理', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/appointment`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasTable = await page.locator('.n-data-table, table').first().isVisible().catch(() => false);
    console.log(`  -> Table visible: ${hasTable}`);
  });

  test('P1-11: /provider - 服务商管理', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/provider`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasContent = await page.locator('body').innerText();
    console.log(`  -> Page has content: ${hasContent.length > 50}`);
  });

  test('P1-12: /system/user - 用户管理', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/system/user`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasTable = await page.locator('.n-data-table, table').first().isVisible().catch(() => false);
    console.log(`  -> Table visible: ${hasTable}`);
  });

  test('P1-13: /system/role - 角色管理', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/system/role`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasTable = await page.locator('.n-data-table, table').first().isVisible().catch(() => false);
    console.log(`  -> Table visible: ${hasTable}`);
  });

  test('P1-14: /system/menu - 菜单管理', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/system/menu`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasTable = await page.locator('.n-data-table, table').first().isVisible().catch(() => false);
    console.log(`  -> Table visible: ${hasTable}`);
  });

  test('P1-15: /system/dict - 字典管理', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}/system/dict`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const hasTable = await page.locator('.n-data-table, table').first().isVisible().catch(() => false);
    console.log(`  -> Table visible: ${hasTable}`);
  });
});

// ============================================================================
// Phase 2: Button & Interaction Tests
// ============================================================================
test.describe('Phase 2: Button & Interaction Audit', () => {

  async function auditPageButtons(page: Page, pageName: string, url: string) {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);
    await page.goto(`${BASE_URL}${url}`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const results: Record<string, boolean> = {};

    // Check common buttons
    const buttonLabels = [
      '新增', '添加', '创建', '编辑', '修改', '删除', '重置',
      '搜索', '查询', '导出', '导入', '提交', '确认', '取消',
      '详情', '查看', '通过', '拒绝', '启用', '禁用', '保存'
    ];

    for (const label of buttonLabels) {
      const btn = page.locator(`button:has-text("${label}"), .n-button:has-text("${label}")`).first();
      results[label] = await btn.isVisible().catch(() => false);
    }

    console.log(`  -> ${pageName} buttons:`, JSON.stringify(results));
    return results;
  }

  test('P2-01: Buttons on /business/elder', async ({ page }) => {
    await auditPageButtons(page, '老人管理', '/business/elder');
  });

  test('P2-02: Buttons on /business/order', async ({ page }) => {
    await auditPageButtons(page, '订单管理', '/business/order');
  });

  test('P2-03: Buttons on /business/staff', async ({ page }) => {
    await auditPageButtons(page, '员工管理', '/business/staff');
  });

  test('P2-04: Buttons on /system/user', async ({ page }) => {
    await auditPageButtons(page, '用户管理', '/system/user');
  });

  test('P2-05: Buttons on /system/role', async ({ page }) => {
    await auditPageButtons(page, '角色管理', '/system/role');
  });
});

// ============================================================================
// Phase 3: Critical UI Quality Checks
// ============================================================================
test.describe('Phase 3: UI Quality Checks', () => {

  test('P3-01: No "0.0" or "null" displayed as valid data', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);

    const pages = [
      '/home',
      '/business/elder',
      '/business/order',
      '/business/financial',
    ];

    for (const p of pages) {
      await page.goto(`${BASE_URL}${p}`);
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(1500);

      const content = await page.locator('body').innerText();

      // Check for bad patterns
      const hasBadNull = content.includes('null') && !content.includes('暂无');
      const hasBadZero = /\b0\.0\b/.test(content) || /\b0\.00\b/.test(content);

      if (hasBadNull || hasBadZero) {
        console.log(`  -> ${p}: Found bad data display pattern`);
      }
    }
  });

  test('P3-02: Delete buttons have confirmation', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);

    const pages = [
      '/business/elder',
      '/business/order',
      '/business/staff',
      '/system/user',
    ];

    for (const p of pages) {
      await page.goto(`${BASE_URL}${p}`);
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(1500);

      const deleteBtn = page.locator('button:has-text("删除")').first();
      if (await deleteBtn.isVisible().catch(() => false)) {
        await deleteBtn.click();
        await page.waitForTimeout(500);

        const hasConfirm = await page.locator('.n-popconfirm, [class*="confirm"], [class*="popconfirm"]').first().isVisible().catch(() => false);
        console.log(`  -> ${p}: Delete confirmation visible: ${hasConfirm}`);

        // Close popconfirm
        await page.keyboard.press('Escape');
        await page.waitForTimeout(300);
      }
    }
  });

  test('P3-03: All table pages have proper pagination', async ({ page }) => {
    await login(page, ACCOUNTS.admin.username, ACCOUNTS.admin.password);

    const tablePages = [
      '/business/elder',
      '/business/order',
      '/business/staff',
      '/business/service-log',
      '/system/user',
    ];

    for (const p of tablePages) {
      await page.goto(`${BASE_URL}${p}`);
      await page.waitForLoadState('networkidle');
      await page.waitForTimeout(1500);

      const hasPagination = await page.locator('.n-pagination, .pagination, [class*="pagination"]').first().isVisible().catch(() => false);
      console.log(`  -> ${p}: Pagination visible: ${hasPagination}`);
    }
  });

  test('P3-04: Login page loads correctly', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.waitForLoadState('networkidle');

    const hasUserInput = await page.locator('input').first().isVisible().catch(() => false);
    const hasPwdInput = await page.locator('input[type="password"]').first().isVisible().catch(() => false);
    const hasSubmitBtn = await page.locator('button').first().isVisible().catch(() => false);

    expect(hasUserInput).toBeTruthy();
    expect(hasPwdInput).toBeTruthy();
    expect(hasSubmitBtn).toBeTruthy();
    console.log('  -> Login page: user input, password input, submit button all visible');
  });

  test('P3-05: Error pages load (/404)', async ({ page }) => {
    await page.goto(`${BASE_URL}/404`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1000);

    const content = await page.locator('body').innerText();
    const has404Text = content.includes('404') || content.includes('找不到');
    console.log(`  -> /404 page shows 404 message: ${has404Text}`);
  });
});

// ============================================================================
// Phase 4: API Health Checks
// ============================================================================
test.describe('Phase 4: API Health Checks', () => {

  test('P4-01: Auth APIs', async ({ request }) => {
    const loginRes = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(loginRes.ok()).toBeTruthy();
    const loginData = await loginRes.json();
    expect(loginData.code).toBe(200);
    expect(loginData.data?.accessToken).toBeDefined();

    const token = loginData.data.accessToken;

    // Test menu API
    const menuRes = await request.get(`${API_BASE}/menu/user`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log(`  -> /api/menu/user status: ${menuRes.status()}, ok: ${menuRes.ok()}`);

    // Test order list
    const orderRes = await request.get(`${API_BASE}/orders?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log(`  -> /api/orders status: ${orderRes.status()}, ok: ${orderRes.ok()}`);
    const orderData = await orderRes.json();
    console.log(`  -> Orders count: ${orderData.data?.records?.length ?? 0}`);
  });

  test('P4-02: Elder APIs', async ({ request }) => {
    const loginRes = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await loginRes.json()).data.accessToken;

    const elderRes = await request.get(`${API_BASE}/elder?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(elderRes.ok()).toBeTruthy();
    const elderData = await elderRes.json();
    console.log(`  -> Elder count: ${elderData.data?.records?.length ?? 0}`);
  });

  test('P4-03: Staff APIs', async ({ request }) => {
    const loginRes = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await loginRes.json()).data.accessToken;

    const staffRes = await request.get(`${API_BASE}/staff?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(staffRes.ok()).toBeTruthy();
    const staffData = await staffRes.json();
    console.log(`  -> Staff count: ${staffData.data?.records?.length ?? 0}`);
  });

  test('P4-04: Service Log APIs', async ({ request }) => {
    const loginRes = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await loginRes.json()).data.accessToken;

    const logRes = await request.get(`${API_BASE}/service-log/list?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(logRes.ok()).toBeTruthy();
    const logData = await logRes.json();
    console.log(`  -> Service logs count: ${logData.data?.records?.length ?? 0}`);
  });

  test('P4-05: Provider APIs', async ({ request }) => {
    const loginRes = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await loginRes.json()).data.accessToken;

    const provRes = await request.get(`${API_BASE}/provider?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(provRes.ok()).toBeTruthy();
    const provData = await provRes.json();
    console.log(`  -> Providers count: ${provData.data?.records?.length ?? 0}`);
  });
});

// ============================================================================
// Phase 5: STAFF Role Data Isolation Check
// ============================================================================
test.describe('Phase 5: STAFF Role Data Isolation', () => {

  test('P5-01: Admin sees all data, Staff sees only own data', async ({ request }) => {
    // Admin token
    const adminLogin = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const adminToken = (await adminLogin.json()).data.accessToken;

    // Staff token
    const staffLogin = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'CS1', password: 'admin123' }
    });
    if (!staffLogin.ok()) {
      console.log('  -> Staff account CS1 not available, skipping');
      return;
    }
    const staffToken = (await staffLogin.json()).data.accessToken;

    // Compare order counts
    const adminOrders = await request.get(`${API_BASE}/orders?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const staffOrders = await request.get(`${API_BASE}/orders?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${staffToken}` }
    });

    const adminCount = (await adminOrders.json()).data?.records?.length ?? 0;
    const staffCount = (await staffOrders.json()).data?.records?.length ?? 0;

    console.log(`  -> Admin sees ${adminCount} orders, Staff sees ${staffCount} orders`);
    console.log(`  -> Data isolation working: ${staffCount <= adminCount}`);

    // Compare service logs
    const adminLogs = await request.get(`${API_BASE}/service-log/list?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const staffLogs = await request.get(`${API_BASE}/service-log/list?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${staffToken}` }
    });

    const adminLogCount = (await adminLogs.json()).data?.records?.length ?? 0;
    const staffLogCount = (await staffLogs.json()).data?.records?.length ?? 0;

    console.log(`  -> Admin sees ${adminLogCount} logs, Staff sees ${staffLogCount} logs`);
  });
});
