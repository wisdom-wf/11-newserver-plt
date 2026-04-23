import { test, expect, Page } from '@playwright/test';

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

test.describe('Extract Page Content for Analysis', () => {
  test('Extract home page text', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE_URL}/home`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);
    const text = await page.locator('body').innerText();
    console.log('HOME_PAGE_CONTENT_START');
    console.log(text.slice(0, 5000));
    console.log('HOME_PAGE_CONTENT_END');
  });

  test('Extract order page text', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE_URL}/business/order`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);
    const text = await page.locator('body').innerText();
    console.log('ORDER_PAGE_CONTENT_START');
    console.log(text.slice(0, 5000));
    console.log('ORDER_PAGE_CONTENT_END');
  });

  test('Extract staff page text', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE_URL}/business/staff`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);
    const text = await page.locator('body').innerText();
    console.log('STAFF_PAGE_CONTENT_START');
    console.log(text.slice(0, 5000));
    console.log('STAFF_PAGE_CONTENT_END');
  });

  test('Extract elder page text', async ({ page }) => {
    await login(page);
    await page.goto(`${BASE_URL}/business/elder`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(3000);
    const text = await page.locator('body').innerText();
    console.log('ELDER_PAGE_CONTENT_START');
    console.log(text.slice(0, 5000));
    console.log('ELDER_PAGE_CONTENT_END');
  });
});
