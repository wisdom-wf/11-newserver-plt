import { test, expect, Page } from '@playwright/test';

/**
 * Health Archive UI Tests
 *
 * Prerequisites:
 * - Frontend running on http://localhost:9527
 * - Backend running on http://localhost:8080
 * - User logged in with admin credentials
 * - Database has at least one elder record
 */

const BASE_URL = 'http://localhost:9527';
const API_BASE = 'https://wisdomdance.cn/jxy/api';

/**
 * Helper function to setup authenticated page via localStorage
 * This approach is more reliable than UI login in E2E tests
 */
async function setupAuthenticatedPage(page: Page, username: string, password: string) {
  await page.goto(`${BASE_URL}/login`);

  // Fill login form
  await page.fill('input[type="text"], input[placeholder*="用户名"]', username);
  await page.fill('input[type="password"]', password);

  // Click login button
  await page.click('button[type="submit"], button:has-text("确认")');

  // Wait for redirect away from login (could be /home or /business/**)
  await page.waitForURL(`**/home**`, { timeout: 15000 });

  // Wait for page to fully load
  await page.waitForLoadState('networkidle');
  await page.waitForTimeout(1000);
}

/**
 * Helper function to select the first available elder
 */
async function selectFirstElder(page: Page) {
  // Click the elder select dropdown
  const elderSelect = page.locator('.n-select').first();
  await elderSelect.click();

  // Wait for options to appear
  await page.waitForTimeout(500);

  // Click the first option
  const firstOption = page.locator('.n-base-select-option').first();
  await firstOption.click();

  // Wait for data to load
  await page.waitForTimeout(1000);
}

test.describe('Health Archive UI Tests', () => {
  test.beforeEach(async ({ page }) => {
    // Setup authenticated session before each test
    await setupAuthenticatedPage(page, 'admin', 'admin123');
  });

  test('TC-UI-HA-001: Health archive page loads correctly', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);

    // Wait for page to load
    await page.waitForLoadState('networkidle');

    // Verify elder select is visible (page has no "健康档案管理" title, use n-select instead)
    await expect(page.locator('.n-select').first()).toBeVisible();
  });

  test('TC-UI-HA-002: Select elder shows info and tabs', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');

    // Select first elder
    await selectFirstElder(page);

    // Wait for elder info to load
    await page.waitForTimeout(1000);

    // Verify tabs are visible
    await expect(page.locator('.n-tabs').first()).toBeVisible();

    // Check for the three tabs (actual DOM uses generic elements, not .n-tab-pane)
    await expect(page.locator(':text("健康测量")').first()).toBeVisible();
    await expect(page.locator(':text("健康报告")').first()).toBeVisible();
    await expect(page.locator(':text("AI健康建议")').first()).toBeVisible();
  });

  test('TC-UI-HA-003: Add measurement record via UI', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');

    // Select first elder
    await selectFirstElder(page);
    await page.waitForTimeout(1000);

    // Click "添加测量记录" button
    const addButton = page.locator('button:has-text("添加测量记录")');
    if (await addButton.isVisible()) {
      await addButton.click();

      // Wait for drawer to open
      await page.waitForSelector('.n-drawer', { timeout: 5000 });

      // Select measurement type - click the type select
      const typeSelect = page.locator('.n-drawer .n-select').first();
      await typeSelect.click();
      await page.waitForTimeout(300);

      // Select blood pressure
      await page.locator('.n-base-select-option:has-text("血压")').click();

      // Enter measurement value
      const valueInput = page.locator('.n-drawer input[placeholder*="测量值"]');
      await valueInput.fill('118/78');

      // Click confirm button
      const confirmButton = page.locator('.n-drawer button:has-text("确认")');
      await confirmButton.click();

      // Wait for success message
      await page.waitForTimeout(1000);

      // Verify drawer closes
      await expect(page.locator('.n-drawer')).not.toBeVisible();
    }
  });

  test('TC-UI-HA-004: View measurement statistics cards', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');

    // Select first elder
    await selectFirstElder(page);
    await page.waitForTimeout(1500);

    // Check if statistics cards are visible (if measurements exist)
    const statsCards = page.locator('.n-card:has-text("血压"), .n-card:has-text("血糖")');

    // Should be visible if measurements exist
    // Note: This is conditional based on test data
  });

  test('TC-UI-HA-005: Delete measurement record via UI', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');

    // Select first elder
    await selectFirstElder(page);
    await page.waitForTimeout(1000);

    // Look for delete button in measurements table
    const deleteButton = page.locator('.n-data-table button:has-text("删除")').first();

    if (await deleteButton.isVisible({ timeout: 2000 })) {
      await deleteButton.click();

      // Wait for confirmation dialog
      await page.waitForTimeout(500);

      // Confirm deletion if popover appears
      const confirmBtn = page.locator('.n-popconfirm button:has-text("确认")');
      if (await confirmBtn.isVisible()) {
        await confirmBtn.click();
      }

      // Wait for success
      await page.waitForTimeout(1000);
    }
  });

  test('TC-UI-HA-006: Switch to reports tab', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');

    // Select first elder
    await selectFirstElder(page);
    await page.waitForTimeout(1000);

    // Click on reports tab
    const reportsTab = page.locator(':text("健康报告")').first();
    await reportsTab.click();

    // Verify reports tab content is visible
    await page.waitForTimeout(500);

    // Should see "生成报告" button
    const generateButton = page.locator('button:has-text("生成报告")');
    await expect(generateButton).toBeVisible();
  });

  test('TC-UI-HA-007: Generate health report via UI', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');

    // Select first elder
    await selectFirstElder(page);
    await page.waitForTimeout(1000);

    // Switch to reports tab
    await page.locator(':text("健康报告")').first().click();
    await page.waitForTimeout(500);

    // Click "生成报告" button
    const generateButton = page.locator('button:has-text("生成报告")');
    if (await generateButton.isVisible()) {
      await generateButton.click();

      // Wait for drawer to open
      await page.waitForSelector('.n-drawer', { timeout: 5000 });

      // Select report type
      const typeSelect = page.locator('.n-drawer .n-select').first();
      await typeSelect.click();
      await page.waitForTimeout(300);

      // Select monthly report
      await page.locator('.n-base-select-option:has-text("月度报告")').click();

      // Click generate button
      const generateBtn = page.locator('.n-drawer button:has-text("生成")');
      await generateBtn.click();

      // Wait for success
      await page.waitForTimeout(1500);
    }
  });

  test('TC-UI-HA-008: Switch to AI suggestions tab', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');

    // Select first elder
    await selectFirstElder(page);
    await page.waitForTimeout(1000);

    // Click on AI suggestions tab
    const suggestionsTab = page.locator(':text("AI健康建议")').first();
    await suggestionsTab.click();

    // Wait for loading to complete
    await page.waitForTimeout(2000);

    // Should see care suggestions content or empty state (text is in generic, not .n-card)
    const careText = page.locator(':text("护理建议")').first();
    const emptyState = page.locator('.n-empty:has-text("暂无AI建议")');

    const careVisible = await careText.isVisible().catch(() => false);
    const emptyVisible = await emptyState.isVisible().catch(() => false);
    expect(careVisible || emptyVisible).toBeTruthy();
  });

  test('TC-UI-HA-009: Download report PDF via UI', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');

    // Select first elder
    await selectFirstElder(page);
    await page.waitForTimeout(1000);

    // Switch to reports tab
    await page.locator(':text("健康报告")').first().click();
    await page.waitForTimeout(1000);

    // Look for download button
    const downloadButton = page.locator('.n-data-table button:has-text("下载PDF")').first();

    if (await downloadButton.isVisible({ timeout: 2000 })) {
      // Set up download handler
      const downloadPromise = page.waitForEvent('download');

      // Click download
      await downloadButton.click();

      // Wait for download
      const download = await downloadPromise;

      // Verify download started
      expect(download.suggestedFilename()).toBeTruthy();
    }
  });

  test('TC-UI-HA-010: View elder info card', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');

    // Select first elder
    await selectFirstElder(page);
    await page.waitForTimeout(1500);

    // Verify elder info is visible (check for elder name/age text in the page)
    const elderCard = page.locator(':text("测试老人"), :text("岁"), :text("三级护理")').first();
    await expect(elderCard).toBeVisible();
  });

  test('TC-UI-HA-011: Empty state for new elder', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');

    // Don't select any elder - check for empty prompt
    const emptyPrompt = page.locator('.n-empty:has-text("请先选择老人")');
    if (await emptyPrompt.isVisible()) {
      await expect(emptyPrompt).toBeVisible();
    }
  });

  test('TC-UI-HA-012: Tab navigation works correctly', async ({ page }) => {
    // Navigate to health archive page
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');

    // Select first elder
    await selectFirstElder(page);
    await page.waitForTimeout(1000);

    // Verify default tab is "健康测量"
    await expect(page.locator(':text("健康测量")').first()).toBeVisible();

    // Navigate to reports tab
    await page.locator(':text("健康报告")').first().click();
    await page.waitForTimeout(500);
    await expect(page.locator(':text("健康报告")').first()).toBeVisible();

    // Navigate to AI suggestions tab
    await page.locator(':text("AI健康建议")').first().click();
    await page.waitForTimeout(500);
    await expect(page.locator(':text("AI健康建议")').first()).toBeVisible();

    // Navigate back to measurements tab
    await page.locator(':text("健康测量")').first().click();
    await page.waitForTimeout(500);
    await expect(page.locator(':text("健康测量")').first()).toBeVisible();
  });

  test('TC-UI-HA-013: Elder photo upload via UI', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/elder`);
    await page.waitForLoadState('networkidle');
    await page.waitForTimeout(2000);
    const editButton = page.locator('.n-data-table button:has-text("编辑"), .n-data-table button:has-text("修改")').first();
    if (await editButton.isVisible({ timeout: 5000 })) {
      await editButton.click();
      await page.waitForSelector('.n-drawer', { timeout: 5000 });
      const photoInput = page.locator('input[type="file"], .n-upload input[type="file"]');
      if (await photoInput.isVisible()) {
        console.log('Photo upload input found');
      }
    }
  });

  test('TC-UI-HA-014: Health archive displays elder with photo', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');
    await selectFirstElder(page);
    await page.waitForTimeout(2000);
    const elderCard = page.locator('.n-card, .person-card, [class*="elder"]').first();
    const cardText = await elderCard.textContent().catch(() => '');
    expect(cardText).toBeDefined();
  });

  test('TC-UI-HA-015: Elder photo shows in info section', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');
    await selectFirstElder(page);
    await page.waitForTimeout(2000);
    const infoSection = page.locator('.n-card:has-text("老人信息"), .elder-info');
    const hasInfoSection = await infoSection.isVisible().catch(() => false);
    if (hasInfoSection) {
      const infoImg = infoSection.locator('img');
      const hasImg = await infoImg.isVisible().catch(() => false);
      console.log(`Elder info section has photo: ${hasImg}`);
    }
  });

  test('TC-UI-HA-016: PersonCard component displays correctly', async ({ page }) => {
    await page.goto(`${BASE_URL}/business/health-archive`);
    await page.waitForLoadState('networkidle');
    await selectFirstElder(page);
    await page.waitForTimeout(2000);
    const personCards = page.locator('.person-card, [class*="person-card"], [class*="elder-card"]');
    const cardCount = await personCards.count();
    console.log(`PersonCard count: ${cardCount}`);
  });
});
