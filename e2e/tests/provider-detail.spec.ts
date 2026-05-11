import { test, expect } from '@playwright/test';

const FRONTEND_URL = '';
const BACKEND_URL = 'https://wisdomdance.cn/jxy/api';

test.describe('服务商详情抽屉测试', () => {
  test('后端API - 服务商详情返回正确结构', async ({ request }) => {
    const loginResponse = await request.post(`${BACKEND_URL}/api/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });

    expect(loginResponse.ok()).toBeTruthy();
    const loginData = await loginResponse.json();
    const token = loginData?.data?.accessToken;
    expect(token).toBeTruthy();

    // 获取服务商列表
    const listResponse = await request.get(`${BACKEND_URL}/api/providers?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    expect(listResponse.ok()).toBeTruthy();
    const listData = await listResponse.json();
    expect(listData.code).toBe(200);
    expect(listData.data.records.length).toBeGreaterThan(0);

    const providerId = listData.data.records[0].providerId;

    // 获取服务商详情
    const detailResponse = await request.get(`${BACKEND_URL}/api/providers/${providerId}`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    expect(detailResponse.ok()).toBeTruthy();
    const detailData = await detailResponse.json();
    expect(detailData.code).toBe(200);
    expect(detailData.data.providerId).toBe(providerId);
    expect(detailData.data.providerName).toBeTruthy();
    expect(detailData.data.qualifications).toBeTruthy();
    expect(Array.isArray(detailData.data.qualifications)).toBe(true);
    expect(detailData.data.serviceTypes).toBeTruthy();
    expect(Array.isArray(detailData.data.serviceTypes)).toBe(true);

    console.log('✓ 服务商详情API返回正确结构');
  });

  test('前端 - 服务商列表显示详情按钮', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/login/pwd-login`);
    await page.waitForLoadState('networkidle');

    await page.locator('input[placeholder="请输入用户名"]').fill('admin');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');
    await page.locator('button:has-text("确认")').click();

    await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);

    // 导航到服务商管理
    await page.goto(`${FRONTEND_URL}/provider`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // 检查详情按钮是否存在
    const detailBtn = page.locator('button:has-text("详情")').first();
    const isVisible = await detailBtn.isVisible().catch(() => false);
    console.log('服务商列表详情按钮可见:', isVisible);

    expect(isVisible).toBe(true);
  });

  test('前端 - 点击详情按钮打开抽屉', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/login/pwd-login`);
    await page.waitForLoadState('networkidle');

    await page.locator('input[placeholder="请输入用户名"]').fill('admin');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');
    await page.locator('button:has-text("确认")').click();

    await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
    await page.waitForTimeout(2000);

    // 导航到服务商管理
    await page.goto(`${FRONTEND_URL}/provider`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // 点击详情按钮
    const detailBtn = page.locator('button:has-text("详情")').first();
    if (await detailBtn.isVisible()) {
      await detailBtn.click();
      await page.waitForTimeout(1000);

      // 检查抽屉是否打开
      const drawer = page.locator('.n-drawer');
      const drawerVisible = await drawer.isVisible().catch(() => false);
      console.log('详情抽屉打开:', drawerVisible);

      expect(drawerVisible).toBe(true);
    }
  });
});
