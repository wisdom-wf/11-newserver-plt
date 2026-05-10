import { test, expect } from '@playwright/test';

test('研发报告在系统管理最后', async ({ page }) => {
  await page.goto('https://wisdomdance.cn/jxy/#/login/pwd-login');
  await page.waitForTimeout(2000);

  // Login
  await page.fill('input[type="text"]', 'admin');
  await page.fill('input[type="password"]', 'admin123');
  await page.click('button[type="submit"]');
  await page.waitForTimeout(2000);

  // Expand system management
  const systemMenu = page.locator('.n-menu-item-content').filter({ hasText: '系统管理' }).first();
  await systemMenu.click();
  await page.waitForTimeout(1000);

  // Get all submenu items in system management
  const items = page.locator('.n-submenu .n-menu-item-content');
  const count = await items.count();
  console.log('Submenu count:', count);

  const texts = await items.allTextContents();
  const clean = texts.map(t => t.trim()).filter(t => t.length > 0 && t.length < 20);
  console.log('Submenu items:', clean);

  // Verify 研发报告 exists
  expect(clean).toContain('研发报告');
});
