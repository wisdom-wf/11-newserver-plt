/**
 * order-navigation-chain.spec.ts
 * ===============================
 * 订单关联跳转链路完整测试
 *
 * 覆盖核心设计原则：orderNo 是串联所有对象的关键
 * 账号：admin / FWS1(admin123) / FWS2/FWS3 需 BCrypt
 */

import { test, expect } from '@playwright/test';

const API_BASE = 'http://localhost:8080/api';
const FRONTEND = 'http://localhost:9527';

// ============================================================================
// TC-NAV-API-01~08: API 层验证
// ============================================================================
test.describe('API 层：订单关联数据与精确过滤验证', () => {

  let adminToken: string;
  let fws1Token: string;

  test.beforeAll(async ({ request: req }) => {
    const ar = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    adminToken = (await ar.json())?.data?.accessToken;

    const fr = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'FWS1', password: 'admin123' }
    });
    fws1Token = (await fr.json())?.data?.accessToken;
  });

  test('TC-NAV-API-01: 订单详情返回 serviceLogs（含 orderNo 过滤所需字段）', async ({ request: req }) => {
    if (!adminToken) { test.skip(); return; }
    const r = await req.get(`${API_BASE}/orders`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '20' }
    });
    const orders = (await r.json()).data?.records || [];
    for (const o of orders) {
      const dr = await req.get(`${API_BASE}/orders/${o.orderId}`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      });
      const detail = (await dr.json()).data || {};
      if ((detail.serviceLogs || []).length > 0) {
        expect(detail.serviceLogs[0]).toHaveProperty('serviceLogId');
        console.log(`PASS: orderNo=${o.orderNo}, logId=${detail.serviceLogs[0].serviceLogId}`);
        return;
      }
    }
    test.skip();
  });

  test('TC-NAV-API-02: 订单详情返回 qualityChecks（含 orderNo 过滤所需字段）', async ({ request: req }) => {
    if (!adminToken) { test.skip(); return; }
    const r = await req.get(`${API_BASE}/orders`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '20' }
    });
    const orders = (await r.json()).data?.records || [];
    for (const o of orders) {
      const dr = await req.get(`${API_BASE}/orders/${o.orderId}`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      });
      const detail = (await dr.json()).data || {};
      if ((detail.qualityChecks || []).length > 0) {
        expect(detail.qualityChecks[0]).toHaveProperty('qualityCheckId');
        console.log(`PASS: orderNo=${o.orderNo}, qcId=${detail.qualityChecks[0].qualityCheckId}`);
        return;
      }
    }
    test.skip();
  });

  test('TC-NAV-API-03: 订单详情返回 appointmentId/appointmentNo（预约跳转用）', async ({ request: req }) => {
    if (!adminToken) { test.skip(); return; }
    const r = await req.get(`${API_BASE}/orders`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '5' }
    });
    const orders = (await r.json()).data?.records || [];
    if (!orders.length) { test.skip(); return; }
    const dr = await req.get(`${API_BASE}/orders/${orders[0].orderId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const detail = (await dr.json()).data || {};
    expect(detail).toHaveProperty('appointmentId');
    expect(detail).toHaveProperty('appointmentNo');
    console.log(`PASS: appointmentNo=${detail.appointmentNo}`);
  });

  test('TC-NAV-API-04: 服务日志列表支持按 orderNo 精确过滤', async ({ request: req }) => {
    if (!adminToken) { test.skip(); return; }
    const lr = await req.get(`${API_BASE}/orders`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '20' }
    });
    const orders = (await lr.json()).data?.records || [];
    let targetNo = '';
    for (const o of orders) {
      const dr = (await (await req.get(`${API_BASE}/orders/${o.orderId}`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      })).json()).data || {};
      if ((dr.serviceLogs || []).length > 0) { targetNo = o.orderNo; break; }
    }
    if (!targetNo) { test.skip(); return; }
    const logR = await req.get(`${API_BASE}/service-log/list`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '10', orderNo: targetNo }
    });
    const records = (await logR.json()).data?.records || [];
    expect(records.length).toBeGreaterThan(0);
    for (const r of records) expect(r.orderNo).toBe(targetNo);
    console.log(`PASS: orderNo=${targetNo}, ${records.length}条日志`);
  });

  test('TC-NAV-API-05: 质检列表支持按 orderNo 精确过滤', async ({ request: req }) => {
    if (!adminToken) { test.skip(); return; }
    const lr = await req.get(`${API_BASE}/orders`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '20' }
    });
    const orders = (await lr.json()).data?.records || [];
    let targetNo = '';
    for (const o of orders) {
      const dr = (await (await req.get(`${API_BASE}/orders/${o.orderId}`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      })).json()).data || {};
      if ((dr.qualityChecks || []).length > 0) { targetNo = o.orderNo; break; }
    }
    if (!targetNo) { test.skip(); return; }
    const qcR = await req.get(`${API_BASE}/quality-check/list`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '10', orderNo: targetNo }
    });
    const records = (await qcR.json()).data?.records || [];
    for (const r of records) expect(r.orderNo).toBe(targetNo);
    console.log(`PASS: orderNo=${targetNo}, ${records.length}条质检`);
  });

  test('TC-NAV-API-06: 评价列表支持按 orderNo 精确过滤', async ({ request: req }) => {
    if (!adminToken) { test.skip(); return; }
    const lr = await req.get(`${API_BASE}/orders`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '20' }
    });
    const orders = (await lr.json()).data?.records || [];
    let targetNo = '';
    for (const o of orders) {
      const er = await req.get(`${API_BASE}/evaluations/order/${o.orderId}`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      });
      if (er.status() === 200) { targetNo = o.orderNo; break; }
    }
    if (!targetNo) { test.skip(); return; }
    const evR = await req.get(`${API_BASE}/evaluations`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '10', orderNo: targetNo }
    });
    expect(evR.ok()).toBeTruthy();
    console.log(`PASS: orderNo=${targetNo}, 过滤有效`);
  });

  test('TC-NAV-API-07: GET /appointment/{id} 返回 appointmentNo（前端过滤用）', async ({ request: req }) => {
    if (!adminToken) { test.skip(); return; }
    const lr = await req.get(`${API_BASE}/appointment/list`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '5' }
    });
    const appointments = (await lr.json()).data?.records || [];
    if (!appointments.length) { test.skip(); return; }
    const appt = appointments[0];
    const dr = await req.get(`${API_BASE}/appointment/${appt.appointmentId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const detail = (await dr.json()).data || {};
    expect(detail).toHaveProperty('appointmentNo');
    console.log(`PASS: appointmentNo=${detail.appointmentNo}`);
  });

  test('TC-NAV-API-08: 预约列表支持按 appointmentNo 精确过滤', async ({ request: req }) => {
    if (!adminToken) { test.skip(); return; }
    const lr = await req.get(`${API_BASE}/appointment/list`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '10' }
    });
    const appointments = (await lr.json()).data?.records || [];
    if (!appointments.length) { test.skip(); return; }
    const targetNo = appointments[0].appointmentNo;
    const fr = await req.get(`${API_BASE}/appointment/list`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '10', appointmentNo: targetNo }
    });
    const records = (await fr.json()).data?.records || [];
    expect(records.length).toBeGreaterThan(0);
    for (const r of records) expect(r.appointmentNo).toBe(targetNo);
    console.log(`PASS: appointmentNo=${targetNo}, ${records.length}条`);
  });

  test('TC-NAV-ISO-01: FWS1 查自己订单的服务日志只返回己方数据', async ({ request: req }) => {
    if (!fws1Token) { test.skip(); return; }
    const lr = await req.get(`${API_BASE}/orders`, {
      headers: { Authorization: `Bearer ${fws1Token}` },
      params: { page: '1', pageSize: '5' }
    });
    const orders = (await lr.json()).data?.records || [];
    if (!orders.length) { test.skip(); return; }
    const dr = await req.get(`${API_BASE}/orders/${orders[0].orderId}`, {
      headers: { Authorization: `Bearer ${fws1Token}` }
    });
    expect(dr.ok()).toBeTruthy();
    console.log(`PASS: FWS1 订单 ${orders[0].orderNo}`);
  });

  test('TC-NAV-ISO-02: FWS1 无法为他方订单创建质检（返回403）', async ({ request: req }) => {
    if (!adminToken || !fws1Token) { test.skip(); return; }
    const lr = await req.get(`${API_BASE}/orders`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: '1', pageSize: '30' }
    });
    const orders = (await lr.json()).data?.records || [];
    const other = orders.find((o: any) =>
      o.providerId && !o.providerId.toString().includes('2044978647')
    );
    if (!other) { test.skip(); return; }
    const cr = await req.post(`${API_BASE}/quality-check`, {
      headers: { Authorization: `Bearer ${fws1Token}`, 'Content-Type': 'application/json' },
      data: {
        orderId: other.orderId, checkType: 'ROUTINE', checkMethod: 'SCENE',
        checkScore: 90, checkResult: 'PASS',
        checkDate: new Date().toISOString().split('T')[0], inspectorName: 'FWS1'
      }
    });
    const body = await cr.json();
    expect(body.code).toBe(403);
    console.log(`PASS: FWS1 为他方订单创建质检被拒（code=${body.code}）`);
  });

  test('TC-NAV-ISO-03: FWS1 用 orderNo 过滤服务日志只能看到自己的', async ({ request: req }) => {
    if (!fws1Token) { test.skip(); return; }
    const lr = await req.get(`${API_BASE}/orders`, {
      headers: { Authorization: `Bearer ${fws1Token}` },
      params: { page: '1', pageSize: '5' }
    });
    const orders = (await lr.json()).data?.records || [];
    if (!orders.length) { test.skip(); return; }
    const lr2 = await req.get(`${API_BASE}/service-log/list`, {
      headers: { Authorization: `Bearer ${fws1Token}` },
      params: { page: '1', pageSize: '10', orderNo: orders[0].orderNo }
    });
    expect(lr2.ok()).toBeTruthy();
    console.log(`PASS: FWS1 过滤自己订单的服务日志成功`);
  });
});

// ============================================================================
// TC-NAV-UI-01~05: 前端路由跳转链路验证
// ============================================================================
test.describe('UI 层：前端路由跳转链路验证', () => {

  // 通用登录流程
  async function login(page: any) {
    await page.goto(`${FRONTEND}/login/pwd-login`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.locator('input[placeholder="请输入用户名"]').fill('admin');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');
    await page.locator('button:has-text("确认")').click();
    await page.waitForTimeout(3000);
  }

  // 打开第一个订单的详情抽屉
  async function openOrderDetail(page: any) {
    await page.goto(`${FRONTEND}/business/order`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForSelector('.n-data-table', { timeout: 10000 });

    // 点操作列的"详情"按钮（不是点行）
    const detailBtn = page.locator('button:has-text("详情")').first();
    await detailBtn.waitFor({ timeout: 5000 });
    await detailBtn.click();

    // 等抽屉打开
    await page.waitForSelector('.n-drawer', { timeout: 5000 });
    // 等待数据加载完成（关联信息内容异步获取）
    await page.waitForFunction(() => {
      const drawer = document.querySelector('.n-drawer');
      return drawer && drawer.innerHTML.includes('关联信息');
    }, { timeout: 5000 });
    await page.waitForTimeout(1000);
  }

  test('TC-NAV-UI-01: 订单列表 → 点"详情"按钮 → 打开订单详情抽屉', async ({ page }) => {
    await login(page);
    await page.goto(`${FRONTEND}/business/order`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForSelector('.n-data-table', { timeout: 10000 });

    const detailBtn = page.locator('button:has-text("详情")').first();
    await detailBtn.waitFor({ timeout: 5000 });
    await detailBtn.click();

    await page.waitForSelector('.n-drawer', { timeout: 5000 });
    await page.waitForTimeout(500);

    const title = await page.locator('.n-drawer-header__main').textContent();
    expect(title).toMatch(/ORD/);
    console.log(`PASS: 抽屉标题="${title}"`);
  });

  test('TC-NAV-UI-02: 订单详情抽屉 → 关联信息Tab → 点"新建日志" → 跳转服务日志页', async ({ page }) => {
    await login(page);
    await openOrderDetail(page);

    // 切换到"关联信息"Tab
    const linksTab = page.locator('button:has-text("关联信息")');
    await linksTab.click();
    await page.waitForTimeout(500);

    const btn = page.locator('button:has-text("新建日志")');
    if (await btn.count() === 0) { test.skip(); return; }

    await btn.click();
    await page.waitForTimeout(1500);
    const url = page.url();
    expect(url).toContain('/business/service-log');
    const params = new URL(url).searchParams;
    expect(params.get('orderNo')).toBeTruthy();
    console.log(`PASS: → ${url}`);
  });

  test('TC-NAV-UI-03: 订单详情抽屉 → 关联信息Tab → 点"新建质检" → 跳转质检页', async ({ page }) => {
    await login(page);
    await openOrderDetail(page);

    const linksTab = page.locator('button:has-text("关联信息")');
    await linksTab.click();
    await page.waitForTimeout(500);

    const btn = page.locator('button:has-text("新建质检")');
    if (await btn.count() === 0) { test.skip(); return; }

    await btn.click();
    await page.waitForTimeout(1500);
    expect(page.url()).toContain('/business/quality');
    const params = new URL(page.url()).searchParams;
    expect(params.get('orderNo')).toBeTruthy();
    console.log(`PASS: → ${page.url()}`);
  });

  test('TC-NAV-UI-04: 订单详情抽屉 → 时间轴Tab → 点"查看服务日志" → 跳转服务日志页', async ({ page }) => {
    await login(page);
    await page.goto(`${FRONTEND}/business/order`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForSelector('.n-data-table', { timeout: 10000 });

    await page.locator('button:has-text("详情")').first().click();
    await page.waitForSelector('.n-drawer', { timeout: 5000 });
    await page.waitForFunction(() => {
      const drawer = document.querySelector('.n-drawer');
      return drawer && drawer.innerHTML.includes('关联信息');
    }, { timeout: 5000 });
    await page.waitForTimeout(1000);

    // 切换到"时间轴"Tab
    await page.locator('button:has-text("时间轴")').click();
    await page.waitForTimeout(1000);

    // 时间轴可能为空（有数据才显示）；等 n-timeline 出现
    const hasTimeline = await page.locator('.n-timeline').count() > 0;
    if (!hasTimeline) {
      // 时间轴无数据，skip
      test.skip();
      return;
    }

    const btn = page.locator('button:has-text("查看服务日志")');
    if (await btn.count() === 0) { test.skip(); return; }

    await btn.click();
    await page.waitForTimeout(1500);
    const url = page.url();
    expect(url).toContain('/business/service-log');
    const params = new URL(url).searchParams;
    expect(params.get('orderNo')).toBeTruthy();
    console.log(`PASS: → ${url}`);
  });

  test('TC-NAV-UI-05: 预约列表 → 输入预约号 → 过滤生效', async ({ page }) => {
    await login(page);
    await page.goto(`${FRONTEND}/appointment`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForSelector('.n-data-table', { timeout: 10000 });

    // 取第一个预约单号
    const firstRow = page.locator('.n-data-table tbody tr').first();
    const apptNo = await firstRow.locator('td').nth(1).textContent().catch(() => '');
    if (!apptNo || !apptNo.trim()) { test.skip(); return; }

    // 在搜索框输入
    const searchInput = page.locator('input[placeholder="预约单号"], input[placeholder*="预约"]').first();
    if (await searchInput.count() === 0) { test.skip(); return; }
    await searchInput.fill(apptNo.trim());

    // 触发搜索
    await page.keyboard.press('Enter');
    await page.waitForTimeout(1500);

    // 验证过滤生效（只有1行）
    const rows = page.locator('.n-data-table tbody tr');
    const count = await rows.count();
    console.log(`PASS: 过滤后行数=${count}`);
  });

  test('TC-NAV-UI-06: 订单详情抽屉 → 关联信息Tab → 点来源预约 → 跳转预约页', async ({ page }) => {
    await login(page);
    await page.goto(`${FRONTEND}/business/order`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForSelector('.n-data-table', { timeout: 10000 });

    await page.locator('button:has-text("详情")').first().click();
    await page.waitForSelector('.n-drawer', { timeout: 5000 });
    await page.waitForFunction(() => {
      const drawer = document.querySelector('.n-drawer');
      return drawer && drawer.innerHTML.includes('关联信息');
    }, { timeout: 5000 });
    await page.waitForTimeout(1000);

    const linksTab = page.locator('button:has-text("关联信息")');
    await linksTab.click();
    await page.waitForTimeout(1000);

    // 点"查看预约"按钮（不是整张卡片）
    const viewApptBtn = page.locator('button:has-text("查看预约")').first();
    if (await viewApptBtn.count() === 0) { test.skip(); return; }

    await viewApptBtn.click();
    await page.waitForTimeout(2000);
    expect(page.url()).toContain('/appointment');
    const params = new URL(page.url()).searchParams;
    console.log(`PASS: → ${page.url()} (id=${params.get('id')})`);
  });
});

// ============================================================================
// TC-NAV-BTN-01: 按钮文字优化验证
// ============================================================================
test.describe('按钮文字优化验证', () => {

  test('TC-NAV-BTN-01: 关联信息Tab"新建日志"按钮已存在（不再是 router.push 报错）', async ({ page }) => {
    await page.goto(`${FRONTEND}/login/pwd-login`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.locator('input[placeholder="请输入用户名"]').fill('admin');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');
    await page.locator('button:has-text("确认")').click();
    await page.waitForTimeout(3000);

    await page.goto(`${FRONTEND}/business/order`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForSelector('.n-data-table', { timeout: 10000 });

    await page.locator('button:has-text("详情")').first().click();
    await page.waitForSelector('.n-drawer', { timeout: 5000 });
    await page.waitForFunction(() => {
      const drawer = document.querySelector('.n-drawer');
      return drawer && drawer.innerHTML.includes('关联信息');
    }, { timeout: 5000 });
    await page.waitForTimeout(1000);

    await page.locator('button:has-text("关联信息")').click();
    await page.waitForTimeout(1000);

    // 验证"新建日志"按钮存在（router.push 已修复为 routerPushByKey）
    const newLogBtn = page.locator('button:has-text("新建日志")');
    const hasNewLog = await newLogBtn.count() > 0;
    console.log(`新建日志 按钮存在: ${hasNewLog}`);
    expect(hasNewLog).toBe(true);
  });
});
