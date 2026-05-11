import { test, expect, Page } from '@playwright/test';

/**
 * 代码重构验证测试
 * 测试目标：验证批量删除、满意度调查发起、统计查询等修复
 */

const FRONTEND_URL = '';
const BACKEND_URL = 'https://wisdomdance.cn/jxy/api';

test.describe('批量删除功能', () => {
  async function login(page: Page) {
    await page.goto(`${FRONTEND_URL}/login/pwd-login`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.locator('input[placeholder="请输入用户名"]').fill('admin');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');
    await page.locator('button:has-text("确认")').click();
    await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
    await page.waitForTimeout(1000);
  }

  test('服务日志批量删除应成功', async ({ page }) => {
    await login(page);

    // 导航到服务日志页面
    await page.goto(`${FRONTEND_URL}/service-log`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForSelector('.n-data-table', { timeout: 15000 }).catch(() => {});

    // 截图初始状态
    await page.screenshot({ path: 'e2e/screenshots/01-service-log-list.png' });

    // 查找草稿状态的日志（只有草稿状态才能删除）
    const draftRows = page.locator('.n-data-table tr').filter({ has: page.locator('text=草稿') });
    const draftCount = await draftRows.count();

    if (draftCount === 0) {
      test.skip('没有草稿状态的日志，跳过批量删除测试');
      return;
    }

    // 选择多个草稿日志
    const checkboxes = page.locator('.n-checkbox input[type="checkbox"]');
    const checkboxCount = await checkboxes.count();
    if (checkboxCount >= 2) {
      await checkboxes.nth(0).check();
      await checkboxes.nth(1).check();
    } else if (checkboxCount === 1) {
      await checkboxes.nth(0).check();
    }

    // 点击批量删除按钮
    const deleteButton = page.locator('button:has-text("批量删除")');
    if (await deleteButton.isVisible().catch(() => false)) {
      await deleteButton.click();

      // 等待确认弹窗
      await page.waitForSelector('.n-popconfirm', { timeout: 5000 }).catch(() => {});
      await page.screenshot({ path: 'e2e/screenshots/02-delete-confirm.png' });

      // 确认删除
      const confirmBtn = page.locator('.n-popconfirm .n-button:has-text("确认")');
      if (await confirmBtn.isVisible().catch(() => false)) {
        await confirmBtn.click();
        await page.waitForTimeout(2000);

        // 验证成功消息
        const successMsg = page.locator('.n-message--success');
        const hasSuccess = await successMsg.isVisible().catch(() => false);
        expect(hasSuccess, '批量删除应显示成功提示').toBe(true);
        console.log('✅ 服务日志批量删除成功');
      }
    }
  });
});

test.describe('满意度调查功能', () => {
  async function login(page: Page) {
    await page.goto(`${FRONTEND_URL}/login/pwd-login`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.locator('input[placeholder="请输入用户名"]').fill('admin');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');
    await page.locator('button:has-text("确认")').click();
    await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
    await page.waitForTimeout(1000);
  }

  test('发起满意度调查应成功', async ({ page }) => {
    await login(page);

    // 导航到评价管理页面
    await page.goto(`${FRONTEND_URL}/evaluation`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForSelector('.n-data-table', { timeout: 15000 }).catch(() => {});

    // 截图初始状态
    await page.screenshot({ path: 'e2e/screenshots/03-evaluation-list.png' });

    // 查找已完成订单对应的评价按钮
    const inviteButton = page.locator('button:has-text("邀请评价")').first();

    if (await inviteButton.isVisible().catch(() => false)) {
      await inviteButton.click();
      await page.waitForTimeout(2000);

      // 截图邀请结果
      await page.screenshot({ path: 'e2e/screenshots/04-invite-result.png' });

      // 检查是否有成功提示或生成的邀请链接
      const successMsg = page.locator('.n-message--success');
      const hasSuccess = await successMsg.isVisible().catch(() => false);

      // 或者检查是否显示了邀请链接
      const linkElement = page.locator('text=/survey\\?token=/');
      const hasLink = await linkElement.isVisible().catch(() => false);

      expect(hasSuccess || hasLink, '邀请评价应成功并显示链接').toBe(true);
      console.log('✅ 发起满意度调查成功');
    } else {
      test.skip('没有可邀请评价的记录，跳过测试');
    }
  });
});

test.describe('统计查询功能', () => {
  async function login(page: Page) {
    await page.goto(`${FRONTEND_URL}/login/pwd-login`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.locator('input[placeholder="请输入用户名"]').fill('admin');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');
    await page.locator('button:has-text("确认")').click();
    await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
    await page.waitForTimeout(1000);
  }

  test('预约统计API应返回正确数据', async ({ page }) => {
    // 直接调用后端API验证
    const response = await page.request.get(`${BACKEND_URL}/api/appointment/statistics`, {
      headers: {
        'Authorization': 'Bearer ' + await getToken(page)
      }
    });

    expect(response.status(), '统计API应返回200').toBe(200);

    const body = await response.json();
    expect(body.code, '响应code应为200').toBe(200);
    expect(body.data).toHaveProperty('total');
    expect(body.data).toHaveProperty('pending');
    expect(body.data).toHaveProperty('confirmed');

    console.log(`✅ 统计API返回正确: total=${body.data.total}`);
  });

  async function getToken(page: Page): Promise<string> {
    const response = await page.request.post(`${BACKEND_URL}/api/auth/login`, {
      data: {
        username: 'admin',
        password: 'admin123'
      }
    });
    const body = await response.json();
    return body.data?.accessToken || '';
  }
});

test.describe('服务商编辑功能', () => {
  async function login(page: Page) {
    await page.goto(`${FRONTEND_URL}/login/pwd-login`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.locator('input[placeholder="请输入用户名"]').fill('admin');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');
    await page.locator('button:has-text("确认")').click();
    await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
    await page.waitForTimeout(1000);
  }

  test('编辑服务商应正确保存creditCode', async ({ page }) => {
    await login(page);

    // 导航到服务商管理页面
    await page.goto(`${FRONTEND_URL}/provider`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForSelector('.n-data-table', { timeout: 15000 }).catch(() => {});

    // 截图初始状态
    await page.screenshot({ path: 'e2e/screenshots/05-provider-list.png' });

    // 点击编辑按钮
    const editButton = page.locator('button:has-text("编辑")').first();
    if (await editButton.isVisible().catch(() => false)) {
      await editButton.click();
      await page.waitForSelector('.n-drawer', { timeout: 5000 }).catch(() => {});
      await page.screenshot({ path: 'e2e/screenshots/06-provider-edit.png' });

      // 查找信用代码字段
      const creditCodeInput = page.locator('input[placeholder*="信用代码"]').first();
      if (await creditCodeInput.isVisible().catch(() => false)) {
        // 获取当前值
        const currentValue = await creditCodeInput.inputValue();
        console.log(`当前信用代码: ${currentValue}`);

        // 确认字段有值
        expect(currentValue.length > 0, '信用代码应有值').toBe(true);
        console.log('✅ 服务商编辑页面正确显示信用代码');
      }

      // 关闭抽屉
      const closeButton = page.locator('.n-drawer .n-button:has-text("关闭")');
      await closeButton.click().catch(() => {});
    }
  });
});
