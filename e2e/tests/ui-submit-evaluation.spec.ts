/**
 * 浏览器模拟：提交满意度评价
 * 流程：登录 → 评价页 → 选订单 → 填表单 → 提交
 */

import { test, expect } from '@playwright/test';

const WEB = 'https://wisdomdance.cn/jxy';
const log = (...args: any[]) => console.log('[evaluation-ui]', ...args);

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

// ─── 诊断：先看评价页表单结构 ─────────────────────────
test('诊断：评价页表单结构', async ({ page }) => {
  await doLogin(page, 'admin', 'admin123');
  await page.goto(`${WEB}/business/evaluation`);
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(2000);

  await page.screenshot({ path: 'test-results/eval-01-page.png', fullPage: false });

  // 找新增按钮
  const addBtn = page.locator('button:has-text("新增"), button:has-text("发起评价")').first();
  const addBtnCount = await addBtn.count();
  log('新增/发起评价按钮数:', addBtnCount);

  if (addBtnCount > 0) {
    await addBtn.click();
    await page.waitForTimeout(1000);
    await page.screenshot({ path: 'test-results/eval-02-dialog.png', fullPage: false });

    // 打印所有输入字段
    const inputs = page.locator('input, textarea, .n-input, .n-select');
    const count = await inputs.count();
    log('输入字段数:', count);

    for (let i = 0; i < Math.min(count, 30); i++) {
      const el = inputs.nth(i);
      const ph = await el.getAttribute('placeholder').catch(() => '');
      const aria = await el.getAttribute('aria-label').catch(() => '');
      if (ph || aria) log(`  [${i}] ph="${ph}" aria="${aria}"`);
    }
  }
});

// ─── 提交评价 #1 ───────────────────────────────────────
test('提交满意度评价 #1', async ({ page }) => {
  await doLogin(page, 'admin', 'admin123');
  await page.goto(`${WEB}/business/evaluation`);
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1500);

  // 点新增/发起评价
  const addBtn = page.locator('button:has-text("新增"), button:has-text("发起评价")').first();
  await addBtn.click();
  await page.waitForTimeout(1000);

  // 填订单号
  const orderInput = page.locator('input[placeholder*="订单"]');
  if (await orderInput.count() > 0) {
    await orderInput.first().fill('ORD202605225749');
    await page.waitForTimeout(500);
  }

  // 截表单图
  await page.screenshot({ path: 'test-results/eval-03-form.png', fullPage: false });

  // 重新找所有输入字段（填写阶段）
  const inputs = page.locator('input[placeholder], textarea[placeholder]');
  const count = await inputs.count();
  for (let i = 0; i < count; i++) {
    const ph = await inputs.nth(i).getAttribute('placeholder').catch(() => '');
    log(`  [${i}] ph="${ph}"`);
  }

  // 填服务评分
  const scoreInput = page.locator('input[placeholder*="评分"], .n-rate');
  if (await scoreInput.count() > 0) {
    // 点击第5颗星
    const stars = page.locator('.n-rate-star');
    const starCount = await stars.count();
    log('星星数量:', starCount);
    if (starCount >= 5) {
      await stars.nth(4).click(); // 第5颗 = 5分
      await page.waitForTimeout(200);
    }
  }

  // 填评价内容
  const commentArea = page.locator('textarea[placeholder*="评价"], textarea[placeholder*="意见"]').first();
  if (await commentArea.count() > 0) {
    await commentArea.fill('服务态度好，专业度高按时上门，整体满意。');
    await page.waitForTimeout(200);
  }

  // 提交
  const submitBtn = page.locator('.n-drawer').locator('button:has-text("提交"), button:has-text("确定")').last();
  if (await submitBtn.count() > 0) {
    await submitBtn.click({ force: true });
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'test-results/eval-04-after.png', fullPage: false });
    log('提交完成');
  }
});

// ─── 提交评价 #2（换个订单）────────────────────────────
test('提交满意度评价 #2（不同订单）', async ({ page }) => {
  await doLogin(page, 'admin', 'admin123');
  await page.goto(`${WEB}/business/evaluation`);
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1500);

  const addBtn = page.locator('button:has-text("新增"), button:has-text("发起评价")').first();
  await addBtn.click();
  await page.waitForTimeout(1000);

  // 尝试另一个订单号
  const orderInput = page.locator('input[placeholder*="订单"]');
  if (await orderInput.count() > 0) {
    await orderInput.first().fill('ORD20250401001');
    await page.waitForTimeout(500);
  }

  // 评分
  const stars = page.locator('.n-rate-star');
  if (await stars.count() > 0) {
    await stars.nth(4).click();
    await page.waitForTimeout(200);
  }

  // 评价内容
  const commentArea = page.locator('textarea[placeholder*="评价"], textarea[placeholder*="意见"]').first();
  if (await commentArea.count() > 0) {
    await commentArea.fill('助洁服务认真细致，环境清洁到位，推荐。');
    await page.waitForTimeout(200);
  }

  const submitBtn = page.locator('.n-drawer').locator('button:has-text("提交"), button:has-text("确定")').last();
  if (await submitBtn.count() > 0) {
    await submitBtn.click({ force: true });
    await page.waitForTimeout(3000);
    log('提交完成');
  }
});
