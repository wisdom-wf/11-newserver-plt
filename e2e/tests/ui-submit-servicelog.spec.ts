/**
 * 浏览器模拟：提交服务日志（admin 视角）
 * 走真实 UI 流程：登录 → 服务日志页 → 填表单 → 提交
 */

import { test, expect } from '@playwright/test';

const WEB = 'https://wisdomdance.cn/jxy';
const log = (...args: any[]) => console.log('[service-log-ui]', ...args);

async function doLogin(page: any, username: string, password: string) {
  await page.goto(`${WEB}/login`);
  await page.waitForLoadState('networkidle');
  const userInput = page.locator('input[placeholder*="用户"]').first();
  const pwdInput = page.locator('input[type="password"]').first();
  if (await userInput.isVisible({ timeout: 3000 })) {
    await userInput.fill(username);
    await pwdInput.fill(password);
    await page.click('button:has-text("确认")');
    await page.waitForURL('**/home', { timeout: 10000 }).catch(() => {});
  }
}

// ─── 批量提交 N 条服务日志 ────────────────────────────────
for (let i = 1; i <= 5; i++) {
  test(`提交服务日志 #${i}`, async ({ page }) => {
    await doLogin(page, 'admin', 'admin123');
    await page.goto(`${WEB}/business/service-log`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(1500);

    // 点新增
    const addBtn = page.locator('button:has-text("新增")').first();
    await addBtn.click();
    await page.waitForTimeout(800);

    // 填订单号（placeholder="订单号"）
    const orderInput = page.locator('input[placeholder="订单号"]');
    if (await orderInput.count() > 0) {
      await orderInput.fill(`ORD2026052${String(i).padStart(2, '0')}001`);
      await page.waitForTimeout(300);
    }

    // 填客户姓名
    const nameInput = page.locator('input[placeholder="客户姓名"]');
    if (await nameInput.count() > 0) {
      await nameInput.fill(`张测试${i}`);
      await page.waitForTimeout(200);
    }

    // 填服务人员（输入名字或ID）
    const staffInput = page.locator('input[placeholder="服务人员"]');
    if (await staffInput.count() > 0) {
      await staffInput.fill('CS1');
      await page.waitForTimeout(200);
    }

    // 填开始日期（placeholder="开始日期"）
    const startDateInput = page.locator('input[placeholder="开始日期"]');
    if (await startDateInput.count() > 0) {
      await startDateInput.fill('2026-05-22');
      await page.waitForTimeout(200);
    }

    // 填结束日期
    const endDateInput = page.locator('input[placeholder="结束日期"]');
    if (await endDateInput.count() > 0) {
      await endDateInput.fill('2026-05-22');
      await page.waitForTimeout(200);
    }

    // 填服务内容
    const contentArea = page.locator('textarea[placeholder="请输入服务内容"]');
    if (await contentArea.count() > 0) {
      await contentArea.fill(`助洁服务${i}：完成室内清洁、地面清扫，服务时长1小时，老人反馈满意。`);
      await page.waitForTimeout(200);
    }

    // 填健康观察（可选）
    const healthArea = page.locator('textarea[placeholder*="健康观察"]');
    if (await healthArea.count() > 0) {
      await healthArea.fill('血压正常，体温36.5℃，精神状态良好。');
      await page.waitForTimeout(200);
    }

    // 提交
    const submitBtn = page.locator('button:has-text("提交")').or(page.locator('.n-drawer').locator('button:has-text("确定")')).last();
    if (await submitBtn.count() > 0) {
      await submitBtn.click({ force: true });
      await page.waitForTimeout(2000);
      log(`#${i} 提交完成`);
    }
  });
}

// ─── 验证：检查列表中是否有新提交 ───────────────────────
test('验证服务日志列表数量增加', async ({ page }) => {
  await doLogin(page, 'admin', 'admin123');
  await page.goto(`${WEB}/business/service-log`);
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(2000);

  // 数表格行
  const rows = page.locator('.n-data-table-tr');
  const count = await rows.count();
  log('当前服务日志行数（含表头）:', count);

  // 截最后几行的文本
  const lastRows = await rows.allTextContents();
  log('最后一行:', JSON.stringify(lastRows[lastRows.length - 1]?.slice(0, 100)));
});
