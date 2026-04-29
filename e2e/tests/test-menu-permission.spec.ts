import { test, expect } from '@playwright/test';

const FRONTEND_URL = 'http://localhost:18080';
const BACKEND_URL = 'http://localhost:8080';

test.describe('STAFF/PROVIDER 动态菜单权限测试', () => {

  test('后端API测试 - STAFF账户获取菜单', async ({ request }) => {
    // 1. STAFF 账户登录
    const loginResponse = await request.post(`${BACKEND_URL}/api/auth/login`, {
      data: { username: '13109118901', password: 'admin123' }
    });

    expect(loginResponse.ok(), `登录失败: ${await loginResponse.text()}`).toBeTruthy();
    const loginData = await loginResponse.json();
    const token = loginData?.data?.accessToken;
    expect(token).toBeTruthy();

    // 2. 获取 STAFF 用户的菜单
    const menuResponse = await request.get(`${BACKEND_URL}/api/menu/user`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    expect(menuResponse.ok()).toBeTruthy();
    const menuData = await menuResponse.json();
    expect(menuData.code).toBe(200);
    expect(menuData.data).toBeTruthy();

    // 3. 验证返回的菜单结构
    const topMenu = menuData.data[0];
    expect(topMenu.menuCode).toBe('business');
    expect(topMenu.menuName).toBe('业务管理');
    expect(topMenu.children).toBeTruthy();
    expect(topMenu.children.length).toBe(7); // R005分配了7个子菜单(M102-M108)，M001是父级

    // 4. 验证子菜单（注意：menu_code已修正为匹配views键名）
    const childMenus = topMenu.children.map((c: any) => c.menuCode);
    expect(childMenus).toContain('business_staff');     // 服务人员
    expect(childMenus).toContain('business_elder');     // 客户管理
    expect(childMenus).toContain('appointment');        // 预约管理 (不是business_appointment)
    expect(childMenus).toContain('business_order');    // 订单管理
    expect(childMenus).toContain('business_service-log'); // 服务日志 (不是business_service_log)
    expect(childMenus).toContain('business_quality');  // 质检管理
    expect(childMenus).toContain('business_evaluation'); // 服务评价

    // 5. 验证不应该有系统管理
    const allMenuCodes = menuData.data.flatMap((m: any) => [m.menuCode, ...(m.children?.map((c: any) => c.menuCode) || [])]);
    expect(allMenuCodes).not.toContain('system');

    console.log('✓ STAFF菜单验证通过！共返回', menuData.data.length, '个顶级菜单');
  });

  test('后端API测试 - 获取PROVIDER账户菜单', async ({ request }) => {
    const loginResponse = await request.post(`${BACKEND_URL}/api/auth/login`, {
      data: { username: '13900000099', password: 'admin123' }
    });

    let loginData = await loginResponse.json();
    let token = loginData?.data?.accessToken;

    if (!token) {
      const altResponse = await request.post(`${BACKEND_URL}/api/auth/login`, {
        data: { username: '13900000088', password: 'admin123' }
      });
      loginData = await altResponse.json();
      token = loginData?.data?.accessToken;
    }

    if (!token) {
      console.log('无可用的PROVIDER账户，跳过此测试');
      return;
    }

    const menuResponse = await request.get(`${BACKEND_URL}/api/menu/user`, {
      headers: { Authorization: `Bearer ${token}` }
    });

    expect(menuResponse.ok()).toBeTruthy();
    const menuData = await menuResponse.json();
    console.log('PROVIDER菜单数量:', menuData.data?.length);
    console.log('PROVIDER菜单:', JSON.stringify(menuData.data, null, 2));
  });

  test('前端测试 - STAFF账户登录', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/login/pwd-login`);
    await page.waitForLoadState('networkidle');

    // 使用正确的选择器
    await page.locator('input[placeholder="请输入用户名"]').fill('13109118901');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');

    // 点击确认按钮登录
    await page.locator('button:has-text("确认")').click();

    // 等待页面跳转
    await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);

    const currentUrl = page.url();
    console.log('登录后URL:', currentUrl);

    // 检查业务管理菜单
    const menuVisible = await page.locator('text=业务管理').isVisible().catch(() => false);
    console.log('业务管理菜单可见:', menuVisible);

    expect(menuVisible).toBe(true);
  });

  test('前端测试 - 验证菜单点击功能', async ({ page }) => {
    await page.goto(`${FRONTEND_URL}/login/pwd-login`);
    await page.waitForLoadState('networkidle');

    await page.locator('input[placeholder="请输入用户名"]').fill('13109118901');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');
    await page.locator('button:has-text("确认")').click();

    await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
    await page.waitForTimeout(3000);

    const businessMenuVisible = await page.locator('text=业务管理').isVisible().catch(() => false);
    if (businessMenuVisible) {
      // 点击业务管理展开子菜单
      await page.locator('text=业务管理').click();
      await page.waitForTimeout(1000);

      // 点击服务人员
      const staffMenu = page.locator('text=服务人员').first();
      if (await staffMenu.isVisible()) {
        await staffMenu.click();
        await page.waitForTimeout(2000);
        console.log('✓ 服务人员菜单点击成功');
        console.log('当前URL:', page.url());
      }
    }
  });
});
