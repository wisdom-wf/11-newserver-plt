import { test, expect, Page, ConsoleMessage } from '@playwright/test';

const BASE_URL = 'http://localhost:9527';

async function login(page: Page) {
  await page.goto(`${BASE_URL}/login`);
  await page.waitForLoadState('networkidle');
  const userInput = page.locator('input[placeholder*="用户"]').first();
  const pwdInput = page.locator('input[type="password"]').first();
  if (await userInput.isVisible({ timeout: 3000 })) {
    await userInput.fill('admin');
    await pwdInput.fill('admin123');
    await page.click('button:has-text("确认")');
    await page.waitForURL('**/home', { timeout: 10000 }).catch(() => {});
  }
}

test.describe('Screenshot Audit - Problem Pages', () => {
  test('Screenshot: Home page (has bad data pattern)', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE_URL}/home`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'e2e/screenshots/home-page.png', fullPage: true });
  });

  test('Screenshot: Order page (has bad data pattern)', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'e2e/screenshots/order-page.png', fullPage: true });
  });

  test('Screenshot: Elder page', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE_URL}/business/elder`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'e2e/screenshots/elder-page.png', fullPage: true });
  });

  test('Screenshot: Staff page', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE_URL}/business/staff`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'e2e/screenshots/staff-page.png', fullPage: true });
  });

  test('Screenshot: Provider page', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE_URL}/provider`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'e2e/screenshots/provider-page.png', fullPage: true });
  });

  test('Screenshot: Financial page', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE_URL}/business/financial`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);
    await page.screenshot({ path: 'e2e/screenshots/financial-page.png', fullPage: true });
  });

  test('Screenshot: Login page', async ({ page }) => {
    await page.goto(`${BASE_URL}/login`);
    await page.waitForLoadState('networkidle');
    await page.screenshot({ path: 'e2e/screenshots/login-page.png', fullPage: true });
  });

  test('Screenshot: 404 error page', async ({ page }) => {
    await page.goto(`${BASE_URL}/not-exist-page-xyz`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: 'e2e/screenshots/404-page.png', fullPage: true });
  });
});
