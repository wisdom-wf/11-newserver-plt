const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  const errors = [];
  page.on('console', msg => { if (msg.type() === 'error') errors.push(msg.text()); });

  // 登录
  await page.goto('https://wisdomdance.cn/jxy/login', { waitUntil: 'networkidle', timeout: 15000 });
  await page.locator('button:has-text("超级管理员")').first().click();
  await page.waitForTimeout(3000);
  console.log('Logged in, URL:', page.url());

  // 导航到健康档案
  await page.goto('https://wisdomdance.cn/jxy/#/business/health-archive', { timeout: 15000 });
  
  // 等待页面稳定
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.waitForTimeout(5000);
  
  await page.screenshot({ path: '/tmp/pw-health-archive.png', fullPage: true });
  
  // 检查是否有tabs
  const tabs = await page.locator('[role="tab"]').allTextContents();
  console.log('All tabs found:', tabs);
  
  // 检查页面内容
  const bodyText = await page.locator('body').textContent();
  console.log('Body snippet:', bodyText.substring(0, 500));
  
  console.log('Console errors:', errors.length ? errors.join('\n') : 'none');
  await browser.close();
})();
