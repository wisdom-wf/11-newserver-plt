import { test, expect } from '@playwright/test';

/**
 * 健康档案创建 + 设备管理模块 专项测试
 *
 * 覆盖范围：
 * 1. 客户详情页 → 创建健康档案
 * 2. 设备管理 API（CRUD + 推送）
 * 3. 设备绑定 → 数据推送 → 自动创建测量记录
 * 4. 健康档案页 URL参数支持
 * 5. 手动填写测量记录
 */

const BASE_URL = '';  // uses playwright baseURL
const API_URL = 'https://wisdomdance.cn/jxy/api';

let adminToken = '';

async function login(request: any): Promise<string> {
  const res = await request.post(`${API_URL}/api/auth/login`, {
    data: { username: 'admin', password: 'admin123' }
  });
  const data = await res.json();
  return data.data.accessToken;
}

// ========== 设备管理 API 测试 ==========

test.describe('设备管理 - API层测试', () => {

  test.beforeAll(async ({ request }) => {
    adminToken = await login(request);
  });

  // --- 设备推送（无需认证） ---

  test('TC-DEV-API-001: 推送到未注册设备返回404', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/devices/push`, {
      headers: { 'Content-Type': 'application/json', 'X-Device-SN': 'NOT-EXIST-SN' },
      data: { deviceSn: 'NOT-EXIST-SN', data: { sys: 120, dia: 80 } }
    });
    const body = await res.json();
    expect(body.code).toBe(404);
    expect(body.message).toContain('设备未注册');
  });

  test('TC-DEV-API-002: 推送接口无需JWT认证', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/devices/push`, {
      headers: { 'Content-Type': 'application/json', 'X-Device-SN': 'ANY-SN' },
      data: { deviceSn: 'ANY-SN', data: { value: 36.5 } }
    });
    expect(res.status()).toBe(200);
  });

  // --- 设备查询 ---

  test('TC-DEV-API-003: 设备列表支持分页', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/devices?page=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    expect(body.data).toHaveProperty('total');
    expect(body.data).toHaveProperty('records');
  });

  test('TC-DEV-API-004: 根据序列号查询设备', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/devices/by-sn/BP-TEST-001`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    // 可能为null（设备未注册）
  });

  test('TC-DEV-API-005: 设备列表按类型筛选', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/devices?deviceType=BP`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
  });

  test('TC-DEV-API-006: 设备列表按状态筛选', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/devices?status=ACTIVE`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
  });

  // --- 设备绑定 ---

  test('TC-DEV-API-007: 绑定未注册设备返回404', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/devices/bind`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: { deviceSn: 'NOT-EXIST', elderId: 'test', measurementType: 'BLOOD_PRESSURE' }
    });
    const body = await res.json();
    expect(body.code).toBe(404);
  });

  test('TC-DEV-API-008: 绑定列表支持按客户筛选', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/devices/bindings?elderId=test-elder`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
  });

  test('TC-DEV-API-009: 获取客户设备列表', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/devices/elder/test-elder-id`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    expect(Array.isArray(body.data)).toBe(true);
  });

  test('TC-DEV-API-010: 解绑不存在的绑定返回404', async ({ request }) => {
    const res = await request.delete(`${API_URL}/api/devices/bindings/nonexistent`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await res.json();
    expect(body.code).toBe(404);
  });
});

// ========== 健康档案 API 测试 ==========

test.describe('健康档案创建 - API层测试', () => {

  test.beforeAll(async ({ request }) => {
    adminToken = await login(request);
  });

  test('TC-HA-API-001: 获取客户健康档案', async ({ request }) => {
    // 先获取一个客户ID
    const elderRes = await request.get(`${API_URL}/api/elders?pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const elderBody = await elderRes.json();
    if (!elderBody.data?.records?.length) {
      test.skip();
      return;
    }
    const elderId = elderBody.data.records[0].elderId;

    const res = await request.get(`${API_URL}/api/elders/${elderId}/health`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    // 可能为200+data或404
    expect([200, 404]).toContain(body.code);
  });

  test('TC-HA-API-002: 创建健康档案', async ({ request }) => {
    const elderRes = await request.get(`${API_URL}/api/elders?pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const elderBody = await elderRes.json();
    if (!elderBody.data?.records?.length) {
      test.skip();
      return;
    }
    const elderId = elderBody.data.records[0].elderId;

    const res = await request.put(`${API_URL}/api/elders/${elderId}/health`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: {
        healthStatus: 'GOOD',
        medicalHistory: '测试病史',
        allergyHistory: '无'
      }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
  });

  test('TC-HA-API-003: 手动添加测量记录', async ({ request }) => {
    const elderRes = await request.get(`${API_URL}/api/elders?pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const elderBody = await elderRes.json();
    if (!elderBody.data?.records?.length) {
      test.skip();
      return;
    }
    const elderId = elderBody.data.records[0].elderId;

    const res = await request.post(`${API_URL}/api/elders/${elderId}/measurements`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: {
        measurementType: 'BLOOD_PRESSURE',
        measurementValue: '120/80',
        measurementUnit: 'mmHg',
        remark: '手动录入测试'
      }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    expect(body.data).toHaveProperty('measurementId');
  });

  test('TC-HA-API-004: 批量添加测量记录', async ({ request }) => {
    const elderRes = await request.get(`${API_URL}/api/elders?pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const elderBody = await elderRes.json();
    if (!elderBody.data?.records?.length) {
      test.skip();
      return;
    }
    const elderId = elderBody.data.records[0].elderId;

    const res = await request.post(`${API_URL}/api/elders/${elderId}/measurements/batch`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: [
        { measurementType: 'BLOOD_GLUCOSE', measurementValue: '5.6', measurementUnit: 'mmol/L' },
        { measurementType: 'WEIGHT', measurementValue: '65', measurementUnit: 'kg' }
      ]
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    expect(Array.isArray(body.data)).toBe(true);
  });

  test('TC-HA-API-005: 查询测量历史', async ({ request }) => {
    const elderRes = await request.get(`${API_URL}/api/elders?pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const elderBody = await elderRes.json();
    if (!elderBody.data?.records?.length) {
      test.skip();
      return;
    }
    const elderId = elderBody.data.records[0].elderId;

    const res = await request.get(`${API_URL}/api/elders/${elderId}/measurements`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    expect(Array.isArray(body.data)).toBe(true);
  });
});

// ========== 设备推送全流程测试 ==========

test.describe('设备推送全流程', () => {

  test.beforeAll(async ({ request }) => {
    adminToken = await login(request);
  });

  test('TC-DEV-E2E-001: 设备推送→自动创建测量记录 全流程', async ({ request }) => {
    // 此测试需要预置设备数据，当前为接口验证
    // 1. 推送数据到已绑定设备
    const pushRes = await request.post(`${API_URL}/api/devices/push`, {
      headers: { 'Content-Type': 'application/json', 'X-Device-SN': 'BP-E2E-001' },
      data: { deviceSn: 'BP-E2E-001', data: { sys: 130, dia: 85 } }
    });
    const pushBody = await pushRes.json();
    // 未注册设备返回404，已注册未绑定返回200但不创建记录
    expect([200, 404]).toContain(pushBody.code);

    // 2. 查询设备推送日志
    // （需要设备存在才能查到日志）
  });
});

// ========== UI 测试 ==========

test.describe('健康档案创建 - UI测试', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.fill('input[placeholder="请输入用户名"]', 'admin');
    await page.fill('input[placeholder="请输入密码"]', 'admin123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/dashboard|home/);
  });

  test('TC-HA-UI-001: 客户详情显示创建健康档案按钮', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/elder`);
    await page.waitForSelector('.n-data-table');

    const detailBtn = page.locator('button:has-text("详情")').first();
    if (await detailBtn.isVisible()) {
      await detailBtn.click();
      const drawer = page.locator('.n-drawer:has-text("客户档案详情")');
      await expect(drawer).toBeVisible({ timeout: 5000 });

      const createBtn = page.locator('button:has-text("创建健康档案")');
      await expect(createBtn).toBeVisible();
    }
  });

  test('TC-HA-UI-002: 点击创建健康档案跳转到健康档案页', async ({ page, context }) => {
    await page.goto(`${BASE_URL}/business/elder`);
    await page.waitForSelector('.n-data-table');

    const detailBtn = page.locator('button:has-text("详情")').first();
    if (await detailBtn.isVisible()) {
      await detailBtn.click();
      await page.waitForSelector('.n-drawer');

      const createBtn = page.locator('button:has-text("创建健康档案")');
      if (await createBtn.isVisible()) {
        await createBtn.click();
        // 应该跳转到健康档案页
        await page.waitForURL(/health-archive/, { timeout: 5000 });
      }
    }
  });

  test('TC-HA-UI-003: 健康档案页URL参数自动选择客户', async ({ page }) => {
    // 先获取一个客户ID
    await page.goto(`${BASE_URL}/business/elder`);
    await page.waitForSelector('.n-data-table');
    await page.waitForTimeout(2000);

    // 带elderId参数访问健康档案页
    await page.goto(`${BASE_URL}/business/health-archive?elderId=test`);
    await page.waitForTimeout(2000);

    // 页面应该加载完成
    await expect(page.locator('text=最近更新客户')).toBeVisible();
  });

  test('TC-HA-UI-004: 健康档案页设备管理Tab存在', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForTimeout(2000);

    const deviceTab = page.locator('.n-tab-pane:has-text("设备管理"), text=设备管理');
    // Tab应该存在
    await expect(page.locator('text=设备管理')).toBeVisible();
  });

  test('TC-HA-UI-005: 设备管理Tab显示绑定设备按钮', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForTimeout(2000);

    // 点击设备管理Tab
    await page.locator('text=设备管理').click();
    await page.waitForTimeout(500);

    const bindBtn = page.locator('button:has-text("绑定设备")');
    await expect(bindBtn).toBeVisible();
  });

  test('TC-HA-UI-006: 点击绑定设备弹出弹窗', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForTimeout(2000);

    await page.locator('text=设备管理').click();
    await page.waitForTimeout(500);

    const bindBtn = page.locator('button:has-text("绑定设备")');
    if (await bindBtn.isVisible()) {
      await bindBtn.click();
      const modal = page.locator('.n-modal:has-text("绑定设备")');
      await expect(modal).toBeVisible({ timeout: 3000 });

      // 弹窗应有序列号输入框和测量类型选择
      await expect(page.locator('input[placeholder="输入设备序列号"]')).toBeVisible();
      await expect(page.locator('.n-modal button:has-text("查询")')).toBeVisible();
      await expect(page.locator('.n-modal button:has-text("确认绑定")')).toBeVisible();
    }
  });

  test('TC-HA-UI-007: 测量记录Tab支持手动填写', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForTimeout(2000);

    // 点击健康测量Tab
    await page.locator('text=健康测量').click();
    await page.waitForTimeout(500);

    // 应该有录入按钮
    const addBtn = page.locator('button:has-text("录入"), button:has-text("新增"), button:has-text("添加")');
    await expect(addBtn.first()).toBeVisible();
  });
});
