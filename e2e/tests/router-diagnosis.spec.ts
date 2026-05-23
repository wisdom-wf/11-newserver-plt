/**
 * 路由诊断 - 通过菜单点击确认真实 URL
 */
import { test } from '@playwright/test';

const FRONTEND = 'https://wisdomdance.cn/jxy';

async function login(page: any) {
  await page.goto(`${FRONTEND}/login/pwd-login`);
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.locator('input[placeholder="请输入用户名"]').fill('admin');
  await page.locator('input[placeholder="请输入密码"]').fill('admin123');
  await page.locator('button:has-text("确认")').click();
  await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
  await page.waitForTimeout(1500);
}

test('菜单导航诊断', async ({ page }) => {
  await login(page);

  // 等待侧边栏菜单加载
  await page.waitForTimeout(2000);

  // 展开"业务管理"菜单
  const menuItems = await page.evaluate(() => {
    const results: any[] = [];
    document.querySelectorAll('.n-menu .n-menu-item, .n-submenu').forEach(el => {
      const text = el.innerText?.trim() || '';
      if (text) results.push({ text, href: (el as HTMLElement).getAttribute('href') || window.location.href });
    });
    return results.slice(0, 30);
  });

  console.log('\n=== 菜单项 ===');
  menuItems.forEach(m => console.log(`  ${m.text}: ${m.href}`));

  // 找到服务商菜单项并点击
  const providerLink = page.locator('.n-menu-item, .n-menu-item-content').filter({ hasText: '服务商' }).first();
  if (await providerLink.isVisible().catch(() => false)) {
    await providerLink.click();
    await page.waitForTimeout(2000);
    console.log('\n服务商页面 URL:', page.url());
  }

  // 找老人档案
  const elderLink = page.locator('.n-menu-item, .n-menu-item-content').filter({ hasText: '老人' }).first();
  if (await elderLink.isVisible().catch(() => false)) {
    await elderLink.click();
    await page.waitForTimeout(2000);
    console.log('老人档案页面 URL:', page.url());
  }

  // 找服务人员
  const staffLink = page.locator('.n-menu-item, .n-menu-item-content').filter({ hasText: '服务人员' }).first();
  if (await staffLink.isVisible().catch(() => false)) {
    await staffLink.click();
    await page.waitForTimeout(2000);
    console.log('服务人员页面 URL:', page.url());
  }

  // 找预约
  const apptLink = page.locator('.n-menu-item, .n-menu-item-content').filter({ hasText: '预约' }).first();
  if (await apptLink.isVisible().catch(() => false)) {
    await apptLink.click();
    await page.waitForTimeout(2000);
    console.log('预约页面 URL:', page.url());
  }
});

test('URL直接访问诊断', async ({ page }) => {
  await login(page);
  await page.waitForTimeout(1000);

  const urls = [
    '/provider',
    '/business/provider',
    '/elder',
    '/business/elder',
    '/staff',
    '/business/staff',
    '/business/appointment',
    '/business/service-log',
    '/business/quality',
    '/business/evaluation',
  ];

  for (const url of urls) {
    await page.goto(`${FRONTEND}${url}`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForTimeout(1500);

    const bodyLen = await page.evaluate(() => document.body.innerText.length);
    const title = await page.title();
    console.log(`${url} => ${page.url()} | body=${bodyLen} | title=${title}`);
  }
});
