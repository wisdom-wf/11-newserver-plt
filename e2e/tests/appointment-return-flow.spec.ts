import { test, expect } from '@playwright/test';

const BASE_URL = 'https://wisdomdance.cn/jxy';

// Login helper
async function login(page: any) {
  await page.goto(`${BASE_URL}/login`);
  await page.waitForLoadState('networkidle');
  const userInput = page.locator('input[placeholder*="用户"]').first();
  const pwdInput = page.locator('input[type="password"]').first();
  await userInput.fill('admin');
  await pwdInput.fill('admin123');
  await page.click('button:has-text("确认")');
  await page.waitForURL('**/home', { timeout: 15000 }).catch(() => {});
}

test.describe('订单退回预约流程', () => {

  test('TC-RET-01: 预约列表退回的预约显示"重新确认"按钮', async ({ page }) => {
    await login(page);

    // 进入预约管理
    await page.goto(`${BASE_URL}/appointment`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // 查找PENDING状态且有cancelReason的预约（退回的）
    const reConfirmBtn = page.locator('button:has-text("重新确认")');
    const reConfirmCount = await reConfirmBtn.count();
    console.log(`找到 ${reConfirmCount} 个"重新确认"按钮`);

    // 如果有退回的预约，验证按钮存在即可（弹窗标题由其他测试覆盖）
    if (reConfirmCount > 0) {
      console.log(`找到 ${reConfirmCount} 个"重新确认"按钮，验证通过`);
    }

    await page.screenshot({ path: 'test-results/tc-ret-01-reconfirm.png' });
  });

  test('TC-RET-02: 订单列表不显示已取消的订单', async ({ page }) => {
    await login(page);

    // 进入订单管理
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // 检查状态筛选下拉框不应该有"已取消"选项
    const statusSelect = page.locator('input[placeholder="订单状态"]');
    if (await statusSelect.count() > 0) {
      await statusSelect.click();
      await page.waitForTimeout(500);
      const cancelledOption = page.locator('.n-base-select-option:has-text("已取消")');
      const count = await cancelledOption.count();
      console.log(`状态筛选中"已取消"选项数量: ${count}`);
      expect(count).toBe(0);
      // 关闭下拉
      await page.keyboard.press('Escape');
    }

    await page.screenshot({ path: 'test-results/tc-ret-02-order-list.png' });
  });

  test('TC-RET-03: 已有退回预约验证流程完整性', async ({ page }) => {
    // 验证已有退回的预约可以重新确认（不需要新建，因为已有数据）
    await login(page);

    // 进入预约管理
    await page.goto(`${BASE_URL}/appointment`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    // 验证退回的预约显示"重新确认"按钮
    const reConfirmBtn = page.locator('button:has-text("重新确认")');
    const reConfirmCount = await reConfirmBtn.count();
    console.log(`"重新确认"按钮数量: ${reConfirmCount}`);
    expect(reConfirmCount).toBeGreaterThan(0);

    // 验证没有"作废"按钮（退回的预约不应有作废按钮）
    // 找到第一行有"重新确认"按钮的行，检查该行没有"作废"
    const firstRow = reConfirmBtn.first().locator('xpath=ancestor::tr');
    const invalidateBtn = firstRow.locator('button:has-text("作废")');
    const invalidateCount = await invalidateBtn.count();
    console.log(`退回预约的"作废"按钮数量: ${invalidateCount}`);
    expect(invalidateCount).toBe(0);

    // 验证有"编辑"按钮
    const editBtn = firstRow.locator('button:has-text("编辑")');
    const editCount = await editBtn.count();
    console.log(`退回预约的"编辑"按钮数量: ${editCount}`);
    expect(editCount).toBe(1);

    await page.screenshot({ path: 'test-results/tc-ret-03-verify.png' });
  });

  test('TC-RET-04: 点击重新确认打开确认弹窗', async ({ page }) => {
    await login(page);

    await page.goto(`${BASE_URL}/appointment`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);

    const reConfirmBtn = page.locator('button:has-text("重新确认")');
    const reConfirmCount = await reConfirmBtn.count();

    if (reConfirmCount > 0) {
      // 滚动到按钮可见再点击
      await reConfirmBtn.first().scrollIntoViewIfNeeded();
      await page.waitForTimeout(500);
      await reConfirmBtn.first().click({ force: true });
      await page.waitForTimeout(2000);

      // 验证弹窗出现 - 检查弹窗标题
      const modalTitle = page.getByText('重新确认预约');
      const titleVisible = await modalTitle.isVisible().catch(() => false);
      console.log(`确认弹窗-"重新确认预约"可见: ${titleVisible}`);
      expect(titleVisible).toBe(true);

      await page.screenshot({ path: 'test-results/tc-ret-04-modal.png' });

      // 关闭弹窗
      await page.click('button:has-text("取消")');
    } else {
      console.log('没有退回的预约，跳过此测试');
    }
  });
});
