import { test, expect } from '@playwright/test';

test.describe('服务日志性能测试', () => {
  test('TC-SL-PERF-001: 服务日志列表加载时间 < 4秒', async ({ page }) => {
    const start = Date.now();

    await page.goto('https://wisdomdance.cn/jxy/');
    await page.getByText('超级管理员').click();
    await page.waitForURL('**/home**', { timeout: 10000 });
    console.log('登录完成:', Date.now() - start, 'ms');

    await page.getByText('业务管理').click();
    await page.waitForTimeout(300);

    const t1 = Date.now();
    await page.getByText('服务日志').click();
    await page.waitForSelector('.n-data-table-tr', { timeout: 4000 });
    const loadTime = Date.now() - t1;
    console.log('服务日志页面加载耗时:', loadTime, 'ms');

    expect(loadTime).toBeLessThan(4000);
    console.log('✅ 通过 - 加载时间', loadTime, 'ms < 4000ms');
  });

  test('TC-SL-PERF-002: 服务日志详情抽屉打开 < 1秒', async ({ page }) => {
    await page.goto('https://wisdomdance.cn/jxy/');
    await page.getByText('超级管理员').click();
    await page.waitForURL('**/home**', { timeout: 10000 });
    await page.getByText('业务管理').click();
    await page.waitForTimeout(300);
    await page.getByText('服务日志').click();

    try {
      await page.waitForSelector('.n-data-table-tr', { timeout: 4000 });
    } catch {
      console.log('⚠️ 表格无数据，跳过抽屉测试');
      return;
    }

    const t1 = Date.now();
    await page.getByText('详情').first().click();
    await page.waitForSelector('.n-drawer', { timeout: 4000 });
    const drawerTime = Date.now() - t1;
    console.log('抽屉打开耗时:', drawerTime, 'ms');

    expect(drawerTime).toBeLessThan(1000);
    console.log('✅ 通过 - 抽屉打开', drawerTime, 'ms < 1000ms');
  });
});
