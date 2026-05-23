/**
 * 探索菜单权限 - 直接访问所有业务路由
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
  await page.waitForTimeout(2000);
}

test('admin直接访问业务路由', async ({ page }) => {
  await login(page);

  const routes = [
    '/business/staff',
    '/business/order',
    '/business/elder',
    '/business/quality',
    '/business/evaluation',
    '/business/service-log',
  ];

  for (const route of routes) {
    await page.goto(`${FRONTEND}${route}`);
    await page.waitForLoadState('networkidle').catch(() => {});
    await page.waitForTimeout(1000);
    const bodyLen = await page.evaluate(() => document.body.innerText.length);
    const title = await page.title();
    const url = page.url();
    console.log(`${route} => ${url} | body=${bodyLen} | title=${title}`);
  }
});

test('找完整侧边栏菜单', async ({ page }) => {
  await login(page);
  await page.waitForTimeout(2000);

  // 展开所有子菜单
  const expanded = await page.evaluate(() => {
    const items: any[] = [];
    document.querySelectorAll('.n-menu-item, .n-menu-item-content, .n-submenu .n-menu-item-content').forEach(el => {
      const text = (el as HTMLElement).innerText?.replace(/\n/g, ' ').trim() || '';
      if (text && text.length > 0 && text.length < 40) {
        items.push({
          text,
          href: el.getAttribute('href') || '',
          path: el.getAttribute('data-path') || (el as any).__vnode?.key || '',
        });
      }
    });
    return items;
  });

  console.log('\n=== 所有菜单项 ===');
  expanded.forEach(m => console.log(`  "${m.text}" => ${m.href || m.path}`));
});
