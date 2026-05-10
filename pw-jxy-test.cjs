const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  const errors = [];
  page.on('console', msg => { if (msg.type() === 'error') errors.push(msg.text()); });
  page.on('pageerror', err => errors.push(err.message));

  // 直接访问登录页
  await page.goto('https://wisdomdance.cn/jxy/login', { waitUntil: 'networkidle', timeout: 15000 });
  await page.waitForTimeout(1000);
  await page.screenshot({ path: '/tmp/pw-login.png' });

  // 找超级管理员按钮
  const adminBtn = page.locator('button:has-text("超级管理员")').first();
  if (await adminBtn.isVisible()) {
    await adminBtn.click();
    await page.waitForTimeout(3000);
    console.log('Clicked admin button');
    await page.screenshot({ path: '/tmp/pw-after-admin.png' });
    console.log('URL after click:', page.url());
  } else {
    console.log('超级管理员按钮未找到');
    const bodyText = await page.locator('body').textContent();
    console.log('Body text:', bodyText.substring(0, 300));
  }

  // 导航到健康档案页
  await page.goto('https://wisdomdance.cn/jxy/#/business/health-archive', { timeout: 15000 });
  await page.waitForTimeout(3000);
  await page.screenshot({ path: '/tmp/pw-health-archive.png', fullPage: true });
  console.log('Health archive URL:', page.url());

  // 点击AI健康建议Tab
  const aiTab = page.locator('[role="tab"]').filter({ hasText: 'AI健康建议' }).first();
  if (await aiTab.isVisible()) {
    await aiTab.click();
    await page.waitForTimeout(2000);
    await page.screenshot({ path: '/tmp/pw-ai-tab.png', fullPage: true });
    console.log('AI Tab clicked');
  } else {
    console.log('AI Tab not visible');
    const tabs = await page.locator('[role="tab"]').allTextContents();
    console.log('All tabs:', tabs);
  }

  console.log('Console errors:', errors.length ? errors.join('\n') : 'none');

  await browser.close();
})();