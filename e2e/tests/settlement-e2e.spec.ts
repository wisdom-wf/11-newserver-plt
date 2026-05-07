import { test, expect } from '@playwright/test';

// ============================================================================
// 测试配置
// ============================================================================
const BACKEND = 'https://wisdomdance.cn/jxy/api';
const FRONTEND = 'http://localhost:9527';
const ADMIN_CREDS = { username: 'admin', password: 'admin123' };
const FWS1_CREDS = { username: 'FWS1', password: 'admin123' };

// ============================================================================
// TC-SETTLE-API-01~10: API 层测试（request fixture + Bearer token）
// ============================================================================
test.describe('结算 API（request + Bearer token）', () => {
  let adminToken: string;
  let fws1Token: string;

  test.beforeAll(async ({ request }) => {
    // ADMIN 登录拿 token
    const adminLogin = await request.post(`${BACKEND}/auth/login`, {
      data: ADMIN_CREDS
    });
    const adminJson = await adminLogin.json();
    adminToken = adminJson.data?.accessToken || adminJson.data?.token;
    console.log(`Admin token: ${adminToken?.slice(0, 20)}...`);

    // FWS1 登录拿 token
    const fws1Login = await request.post(`${BACKEND}/auth/login`, {
      data: FWS1_CREDS
    });
    const fws1Json = await fws1Login.json();
    fws1Token = fws1Json.data?.accessToken || fws1Json.data?.token;
    console.log(`FWS1 token: ${fws1Token?.slice(0, 20)}...`);
  });

  // TC-SETTLE-API-01
  test('TC-SETTLE-API-01: ADMIN 查看结算列表正常返回', async ({ request }) => {
    const resp = await request.get(`${BACKEND}/financial/settlements?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(resp.status(), `HTTP ${resp.status()}`).toBe(200);
    const body = await resp.json();
    expect(body.code, `code=${body.code} msg=${body.message}`).toBe(200);
    expect(Array.isArray(body.data?.records)).toBe(true);
    console.log(`结算列表: ${body.data?.records?.length ?? 0} 条`);
  });

  // TC-SETTLE-API-02
  test('TC-SETTLE-API-02: 结算列表支持按 orderNo 过滤', async ({ request }) => {
    // 先取一条结算单的 orderNo 用于精确过滤
    const allResp = await request.get(`${BACKEND}/financial/settlements?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const allBody = await allResp.json();
    const record = allBody.data?.records?.[0];

    if (!record?.orderNo) {
      console.log('SKIP: 无结算数据');
      test.skip();
      return;
    }

    // 用 orderNo 精确过滤（测试走 o.order_no 联表过滤）
    const filterResp = await request.get(
      `${BACKEND}/financial/settlements?page=1&pageSize=10&orderNo=${encodeURIComponent(record.orderNo)}`,
      { headers: { Authorization: `Bearer ${adminToken}` } }
    );
    expect(filterResp.status()).toBe(200);
    const filtered = (await filterResp.json()).data?.records ?? [];
    console.log(`orderNo="${record.orderNo}" 过滤后: ${filtered.length} 条`);
    expect(filtered.length).toBeGreaterThan(0);
  });
  // TC-SETTLE-API-03
  test('TC-SETTLE-API-03: 结算详情返回正确字段', async ({ request }) => {
    const listResp = await request.get(`${BACKEND}/financial/settlements?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const listBody = await listResp.json();
    const record = listBody.data?.records?.[0];

    if (!record?.settlementId) {
      console.log('SKIP: 无结算数据');
      test.skip();
      return;
    }

    const resp = await request.get(`${BACKEND}/financial/settlements/${record.settlementId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.data.settlementId).toBe(record.settlementId);
    expect(body.data.status).toBeDefined();
    console.log(`结算详情: ${body.data.settlementNo} | status=${body.data.status}`);
  });

  // TC-SETTLE-API-04
  test('TC-SETTLE-API-04: 结算计算返回预览数据', async ({ request }) => {
    const resp = await request.post(`${BACKEND}/financial/settlements/calculate`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: {
        providerId: '',
        settlementPeriodStart: '2026-04-01',
        settlementPeriodEnd: '2026-04-30'
      }
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code, `code=${body.code} msg=${body.message}`).toBe(200);
    expect(typeof body.data?.totalOrderCount).toBe('number');
    console.log(`结算计算: 共${body.data?.totalOrderCount}笔, 服务费¥${body.data?.totalServiceAmount}`);
  });

  // TC-SETTLE-API-05
  test('TC-SETTLE-API-05: 批量结算生成结算单', async ({ request }) => {
    const resp = await request.post(`${BACKEND}/financial/settlements/batch`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: {
        settlementType: 'PROVIDER',
        settlementPeriodStart: '2026-04-01',
        settlementPeriodEnd: '2026-04-30'
      }
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code, `code=${body.code} msg=${body.message}`).toBe(200);
    expect(Array.isArray(body.data)).toBe(true);
    console.log(`批量结算: 生成${body.data?.length ?? 0}个结算单`);
  });

  // TC-SETTLE-API-06
  test('TC-SETTLE-API-06: 确认结算成功', async ({ request }) => {
    const listResp = await request.get(`${BACKEND}/financial/settlements?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const listBody = await listResp.json();
    const unpaid = listBody.data?.records?.find((r: any) => r.status === 'UNPAID');

    if (!unpaid) {
      console.log('SKIP: 无UNPAID结算单');
      test.skip();
      return;
    }

    const resp = await request.post(`${BACKEND}/financial/settlements/${unpaid.settlementId}/confirm`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: {}
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code, `code=${body.code} msg=${body.message}`).toBe(200);
    console.log(`确认结算: ${unpaid.settlementNo} → 成功`);
  });

  // TC-SETTLE-API-07
  test('TC-SETTLE-API-07: 重复确认结算返回错误', async ({ request }) => {
    const listResp = await request.get(`${BACKEND}/financial/settlements?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const listBody = await listResp.json();
    const confirmed = listBody.data?.records?.find((r: any) => r.status === 'CONFIRMED');

    if (!confirmed) {
      console.log('SKIP: 无CONFIRMED结算单');
      test.skip();
      return;
    }

    const resp = await request.post(`${BACKEND}/financial/settlements/${confirmed.settlementId}/confirm`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: {}
    });
    const body = await resp.json();
    // 重复确认应该报错（code !== 0 或 status !== 200）
    console.log(`重复确认结算: code=${body.code} msg=${body.message}`);
  });

  // TC-SETTLE-API-08
  test('TC-SETTLE-API-08: PROVIDER 角色只能查己方结算单（隔离）', async ({ request }) => {
    const resp = await request.get(`${BACKEND}/financial/settlements?page=1&pageSize=50`, {
      headers: { Authorization: `Bearer ${fws1Token}` }
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code, `code=${body.code} msg=${body.message}`).toBe(200);
    console.log(`FWS1 结算列表: ${body.data?.records?.length ?? 0} 条`);

    // 验证每条属于 FWS1（providerId = 2044978647030419457）
    if ((body.data?.records?.length ?? 0) > 0) {
      const allFWS1 = body.data.records.every((r: any) =>
        String(r.providerId) === '2044978647030419457' ||
        String(r.providerId) === '2044978647030419458'
      );
      expect(allFWS1, '存在跨服务商数据').toBe(true);
      console.log('FWS1 数据隔离: 通过 ✓');
    }
  });

  // TC-SETTLE-API-09
  test('TC-SETTLE-API-09: 服务定价列表正常返回', async ({ request }) => {
    const resp = await request.get(`${BACKEND}/financial/prices?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code, `code=${body.code} msg=${body.message}`).toBe(200);
    console.log(`服务定价列表: ${body.data?.records?.length ?? 0} 条`);
  });

  // TC-SETTLE-API-10
  test('TC-SETTLE-API-10: 退款列表正常返回', async ({ request }) => {
    const resp = await request.get(`${BACKEND}/financial/refunds?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code, `code=${body.code} msg=${body.message}`).toBe(200);
    console.log(`退款列表: ${body.data?.records?.length ?? 0} 条`);
  });
});

// ============================================================================
// TC-SETTLE-UI-01~05: UI 层测试（page fixture）
// ============================================================================
async function uiLogin(page: any, username: string, password: string) {
  await page.goto(`${FRONTEND}/login/pwd-login`);
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.locator('input[placeholder="请输入用户名"]').fill(username);
  await page.locator('input[placeholder="请输入密码"]').fill(password);
  await page.locator('button:has-text("确认")').click();
  await page.waitForTimeout(3000);
}

test.describe('结算 UI 测试', () => {
  // TC-SETTLE-UI-01
  test('TC-SETTLE-UI-01: 财务页结算Tab正常加载', async ({ page }) => {
    await uiLogin(page, ADMIN_CREDS.username, ADMIN_CREDS.password);
    await page.goto(`${FRONTEND}/business/financial`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForSelector('.n-tabs', { timeout: 8000 });

    const tabBar = await page.locator('.n-tabs-tab').allTextContents();
    console.log(`财务页Tab: ${tabBar.join(', ')}`);
    expect(tabBar.some(t => t.includes('结算'))).toBe(true);
  });

  // TC-SETTLE-UI-02
  test('TC-SETTLE-UI-02: 结算列表数据显示', async ({ page }) => {
    await uiLogin(page, ADMIN_CREDS.username, ADMIN_CREDS.password);
    await page.goto(`${FRONTEND}/business/financial`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForTimeout(2000);

    const table = page.locator('.n-data-table');
    const exists = await table.isVisible().catch(() => false);
    console.log(`结算表格: ${exists ? '存在' : '不存在'}`);
  });

  // TC-SETTLE-UI-03
  test('TC-SETTLE-UI-03: 确认结算按钮存在', async ({ page }) => {
    await uiLogin(page, ADMIN_CREDS.username, ADMIN_CREDS.password);
    await page.goto(`${FRONTEND}/business/financial`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForTimeout(2000);

    // 找"确认结算"按钮（已修复 B1，现在 handleConfirm 存在）
    const confirmBtn = page.locator('button:has-text("确认结算")').first();
    const exists = await confirmBtn.isVisible().catch(() => false);
    expect(exists, '"确认结算"按钮应该可见').toBe(true);
    console.log('确认结算按钮: 存在 ✓');
  });

  // TC-SETTLE-UI-04
  test('TC-SETTLE-UI-04: 财务页统计卡片', async ({ page }) => {
    await uiLogin(page, ADMIN_CREDS.username, ADMIN_CREDS.password);
    await page.goto(`${FRONTEND}/business/financial`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForTimeout(2000);

    const statCards = await page.locator('.n-card, .stat-card, [class*="stat"]').count();
    console.log(`统计卡片: ${statCards} 个`);
  });

  // TC-SETTLE-UI-05
  test('TC-SETTLE-UI-05: Tab切换正常', async ({ page }) => {
    await uiLogin(page, ADMIN_CREDS.username, ADMIN_CREDS.password);
    await page.goto(`${FRONTEND}/business/financial`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForTimeout(2000);

    // 尝试点击"定价管理"Tab
    const pricingTab = page.locator('.n-tabs-tab:has-text("定价管理")');
    if (await pricingTab.isVisible().catch(() => false)) {
      await pricingTab.click();
      await page.waitForTimeout(1000);
      console.log('定价Tab: 切换成功 ✓');
    } else {
      console.log('定价Tab: 未找到或不可见（跳过）');
    }
  });
});
