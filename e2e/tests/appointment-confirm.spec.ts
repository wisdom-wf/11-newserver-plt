import { test, expect, Page } from '@playwright/test';

/**
 * 预约确认功能测试
 * 测试目标：验证预约确认后不会显示"the backend request error"错误提示
 *
 * 问题背景：V0.8第一轮测试问题1 - 预约确认后显示后端错误提示
 * 修复方案：将统计接口调用单独try-catch，避免统计失败时显示错误提示
 */

const BACKEND_URL = 'http://localhost:8080';
const FRONTEND_URL = 'http://localhost:9527';

test.describe('预约确认功能', () => {
  /**
   * 登录辅助函数 - 使用真实账号登录
   */
  async function login(page: Page) {
    await page.goto(`${FRONTEND_URL}/login/pwd-login`);
    await page.waitForLoadState('networkidle').catch(() => {});

    await page.locator('input[placeholder="请输入用户名"]').fill('admin');
    await page.locator('input[placeholder="请输入密码"]').fill('admin123');
    await page.locator('button:has-text("确认")').click();

    await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
    await page.waitForTimeout(1000);
  }

  /**
   * 导航到预约管理页面
   */
  async function navigateToAppointment(page: Page) {
    await page.goto(`${FRONTEND_URL}/appointment`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForSelector('.n-data-table', { timeout: 15000 }).catch(() => {});
  }

  test('预约确认后不应显示后端错误提示', async ({ page }) => {
    // 1. 登录系统
    await login(page);

    // 2. 导航到预约管理页面
    await navigateToAppointment(page);

    // 3. 截图记录初始状态
    await page.screenshot({ path: 'e2e/screenshots/01-appointment-list.png' });

    // 4. 查找待确认的预约（第一行）
    const confirmButton = page.locator('button:has-text("确认")').first();

    // 检查是否存在待确认的预约
    const confirmButtonCount = await confirmButton.count();
    if (confirmButtonCount === 0) {
      test.skip('没有待确认的预约，跳过测试');
      return;
    }

    // 5. 点击确认按钮
    await confirmButton.click();

    // 6. 等待确认对话框出现
    await page.waitForSelector('.n-modal', { timeout: 5000 });
    await page.screenshot({ path: 'e2e/screenshots/02-confirm-modal.png' });

    // 7. 选择服务商（如果下拉框存在）
    const providerSelect = page.locator('.n-select').first();
    if (await providerSelect.isVisible().catch(() => false)) {
      await providerSelect.click();
      await page.waitForTimeout(500);
      // 选择第一个选项
      const firstOption = page.locator('.n-base-select-option').first();
      if (await firstOption.isVisible().catch(() => false)) {
        await firstOption.click();
      }
    }

    // 8. 点击确认对话框的确认按钮
    const modalConfirmButton = page.locator('.n-modal .n-button:has-text("确认")').last();
    await modalConfirmButton.click();

    // 9. 等待操作完成，观察是否有错误提示
    await page.waitForTimeout(2000);

    // 10. 截图记录结果
    await page.screenshot({ path: 'e2e/screenshots/03-after-confirm.png' });

    // 11. 验证：不应出现"the backend request error"错误提示
    const errorMessage = page.locator('.n-message--error');
    const errorCount = await errorMessage.count();

    if (errorCount > 0) {
      // 获取错误消息内容
      const errorTexts = await errorMessage.allTextContents();
      const hasBackendError = errorTexts.some(
        text => text.includes('the backend request error') || text.includes('backend request error')
      );

      expect(hasBackendError, `不应显示后端错误提示，但发现: ${errorTexts.join(', ')}`).toBe(false);
    }

    // 12. 验证：应该显示"确认成功"提示
    const successMessage = page.locator('.n-message--success');
    await expect(successMessage).toBeVisible({ timeout: 5000 });
    const successText = await successMessage.textContent();
    expect(successText).toContain('确认成功');

    console.log('✅ 测试通过：预约确认成功，无后端错误提示');
  });

  test('预约取消后不应显示后端错误提示', async ({ page }) => {
    // 1. 登录系统
    await login(page);

    // 2. 导航到预约管理页面
    await navigateToAppointment(page);

    // 3. 查找待确认的预约
    const cancelButton = page.locator('button:has-text("取消")').first();

    const cancelButtonCount = await cancelButton.count();
    if (cancelButtonCount === 0) {
      test.skip('没有可取消的预约，跳过测试');
      return;
    }

    // 4. 点击取消按钮
    await cancelButton.click();

    // 5. 等待取消对话框
    await page.waitForSelector('.n-modal', { timeout: 5000 });

    // 6. 填写取消原因
    const reasonInput = page.locator('.n-modal textarea');
    await reasonInput.fill('测试取消原因');

    // 7. 点击确认
    await page.locator('.n-modal .n-button:has-text("确认")').last().click();

    // 8. 等待操作完成
    await page.waitForTimeout(2000);

    // 9. 截图记录
    await page.screenshot({ path: 'e2e/screenshots/04-after-cancel.png' });

    // 10. 验证：不应出现后端错误提示
    const errorMessage = page.locator('.n-message--error');
    const errorTexts = await errorMessage.allTextContents();
    const hasBackendError = errorTexts.some(
      text => text.includes('the backend request error') || text.includes('backend request error')
    );

    expect(hasBackendError, `不应显示后端错误提示，但发现: ${errorTexts.join(', ')}`).toBe(false);

    // 11. 验证：应该显示"取消成功"提示
    const successMessage = page.locator('.n-message--success');
    await expect(successMessage).toBeVisible({ timeout: 5000 });

    console.log('✅ 测试通过：预约取消成功，无后端错误提示');
  });

  test('预约作废后不应显示后端错误提示', async ({ page }) => {
    // 1. 登录系统
    await login(page);

    // 2. 导航到预约管理页面
    await navigateToAppointment(page);

    // 3. 查找待确认的预约
    const invalidateButton = page.locator('button:has-text("作废")').first();

    const invalidateButtonCount = await invalidateButton.count();
    if (invalidateButtonCount === 0) {
      test.skip('没有可作废的预约，跳过测试');
      return;
    }

    // 4. 点击作废按钮
    await invalidateButton.click();

    // 5. 等待作废对话框
    await page.waitForSelector('.n-modal', { timeout: 5000 });

    // 6. 填写作废原因
    const reasonInput = page.locator('.n-modal textarea');
    await reasonInput.fill('测试作废原因');

    // 7. 点击确认
    await page.locator('.n-modal .n-button:has-text("确认")').last().click();

    // 8. 等待操作完成
    await page.waitForTimeout(2000);

    // 9. 截图记录
    await page.screenshot({ path: 'e2e/screenshots/05-after-invalidate.png' });

    // 10. 验证：不应出现后端错误提示
    const errorMessage = page.locator('.n-message--error');
    const errorTexts = await errorMessage.allTextContents();
    const hasBackendError = errorTexts.some(
      text => text.includes('the backend request error') || text.includes('backend request error')
    );

    expect(hasBackendError, `不应显示后端错误提示，但发现: ${errorTexts.join(', ')}`).toBe(false);

    // 11. 验证：应该显示"作废成功"提示
    const successMessage = page.locator('.n-message--success');
    await expect(successMessage).toBeVisible({ timeout: 5000 });

    console.log('✅ 测试通过：预约作废成功，无后端错误提示');
  });
});
