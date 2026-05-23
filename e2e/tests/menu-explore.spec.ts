/**
 * 菜单点击探索 - 确认真实 URL
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

test('探索侧边栏所有菜单项', async ({ page }) => {
  await login(page);
  await page.waitForTimeout(2000);

  const menuItems = await page.evaluate(() => {
    const results: any[] = [];
    // 找所有菜单项
    document.querySelectorAll('.n-menu-item-content, .n-menu-item-content-wrapper, .n-menu-item').forEach(el => {
      const text = (el as HTMLElement).innerText?.trim() || '';
      const href = (el as HTMLElement).getAttribute('href') || '';
      const dataHref = el.getAttribute('href') || '';
      if (text && text.length < 30 && !text.includes('\n')) {
        results.push({ text, href: dataHref || href, class: el.className.substring(0, 50) });
      }
    });
    return results;
  });

  console.log('\n=== 侧边栏菜单 ===');
  menuItems.forEach(m => console.log(`  "${m.text}" => ${m.href}`));
});

test('点击找真实URL', async ({ page }) => {
  await login(page);
  await page.waitForTimeout(2000);

  const targets = ['服务商', '服务人员', '老人', '档案', '预约', '订单', '服务日志', '质检', '评价'];

  for (const target of targets) {
    // 找到包含文本的菜单项并点击
    const menuItem = page.locator(`.n-menu-item-content:has-text("${target}"), .n-menu-item:has-text("${target}")`).first();
    if (await menuItem.isVisible().catch(() => false)) {
      await menuItem.click();
      await page.waitForTimeout(2000);
      console.log(`点击"${target}" => URL: ${page.url()}, body长度: ${await page.evaluate(() => document.body.innerText.length)}`);
    } else {
      console.log(`"${target}" 菜单项未找到`);
    }
  }
});
