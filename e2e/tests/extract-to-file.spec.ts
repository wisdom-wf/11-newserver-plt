import { test, Page } from '@playwright/test';
import * as fs from 'fs';

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

async function extractPage(page: Page, name: string, url: string) {
  await login(page);
  await page.goto(`${BASE_URL}${url}`);
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(3000);

  // Get all text content
  const body = page.locator('body');
  const text = await body.innerText();

  // Get all data values from tables
  const tableData: string[] = [];
  const tables = page.locator('table, .n-data-table');
  const tableCount = await tables.count();

  // Find number values
  const numPattern = /\b\d+\.\d+\b/g;
  const numbers = text.match(numPattern) || [];
  const badNumbers = numbers.filter(n => n.startsWith('0.'));

  // Get page title
  const title = await page.title();

  const report = {
    page: name,
    url: url,
    title,
    tableCount,
    textLength: text.length,
    allNumbers: [...new Set(numbers)].slice(0, 50),
    suspiciousValues: badNumbers,
    first2000chars: text.slice(0, 2000),
  };

  fs.writeFileSync(`e2e/screenshots/${name}-content.json`, JSON.stringify(report, null, 2));
  console.log(`Extracted ${name}: textLen=${text.length}, tables=${tableCount}, susp=${badNumbers.join(',')}`);
}

test('Extract home page', async ({ page }) => {
  await extractPage(page, 'home', '/home');
});

test('Extract order page', async ({ page }) => {
  await extractPage(page, 'order', '/business/order');
});

test('Extract staff page', async ({ page }) => {
  await extractPage(page, 'staff', '/business/staff');
});

test('Extract elder page', async ({ page }) => {
  await extractPage(page, 'elder', '/business/elder');
});
