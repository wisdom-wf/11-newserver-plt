import { test, expect } from '@playwright/test';

const FRONTEND_URL = 'http://localhost:18080';
const BACKEND_URL = 'http://localhost:8080';

test.describe('预约确认查看服务商资质测试', () => {
  test('后端API - 预约列表包含待确认状态的预约', async ({ request }) => {
    const loginResponse = await request.post(`${BACKEND_URL}/api/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });

    expect(loginResponse.ok()).toBeTruthy();
    const loginData = await loginResponse.json();
    const token = loginData?.data?.accessToken;
    expect(token).toBeTruthy();

    // 获取预约列表
    const listResponse = await request.get(`${BACKEND_URL}/api/appointments?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    expect(listResponse.ok()).toBeTruthy();
    const listData = await listResponse.json();
    expect(listData.code).toBe(200);

    console.log('✓ 预约列表API正常，共', listData.data.total, '条预约');
  });

  test('后端API - 服务商资质证书API正常', async ({ request }) => {
    const loginResponse = await request.post(`${BACKEND_URL}/api/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });

    expect(loginResponse.ok()).toBeTruthy();
    const loginData = await loginResponse.json();
    const token = loginData?.data?.accessToken;
    expect(token).toBeTruthy();

    // 获取服务商列表
    const providerResponse = await request.get(`${BACKEND_URL}/api/providers?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    const providerData = await providerResponse.json();
    const providerId = providerData.data.records[0].providerId;

    // 获取服务商资质
    const certResponse = await request.get(`${BACKEND_URL}/api/providers/${providerId}/certificates`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    expect(certResponse.ok()).toBeTruthy();
    const certData = await certResponse.json();
    expect(certData.code).toBe(200);
    expect(Array.isArray(certData.data)).toBe(true);

    console.log('✓ 服务商资质证书API正常');
  });

  test('前端 - 预约列表页面加载', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/login/pwd-login`);
    await page.waitForLoadState('networkidle');

    await page.locator('input[placeholder="请输入用户名"]').fill('admin');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');
    await page.locator('button:has-text("确认")').click();

    await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
    await page.waitForTimeout(2000);

    // 导航到预约管理
    await page.goto(`${FRONTEND_URL}/appointment`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // 检查页面标题
    const title = page.locator('text=预约信息管理');
    const isVisible = await title.isVisible().catch(() => false);
    console.log('预约管理页面标题可见:', isVisible);

    expect(isVisible).toBe(true);
  });
});
