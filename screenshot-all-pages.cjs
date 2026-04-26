const { chromium } = require('@playwright/test');

async function screenshotPages() {
  const browser = await chromium.launch({ headless: true });
  const context = await browser.newContext({ viewport: { width: 1920, height: 1080 } });
  const page = await context.newPage();
  
  const pages = [
    { url: '/login', name: '01-login' },
    { url: '/dashboard', name: '02-dashboard' },
    { url: '/business/elder', name: '03-elder' },
    { url: '/business/order', name: '04-order' },
    { url: '/business/service-log', name: '05-service-log' },
    { url: '/business/quality', name: '06-quality' },
    { url: '/business/evaluation', name: '07-evaluation' },
    { url: '/business/settlement', name: '08-settlement' },
    { url: '/business/config', name: '09-config' },
  ];

  // 先登录
  console.log('Navigating to login...');
  await page.goto('http://localhost:9527/login', { waitUntil: 'networkidle', timeout: 15000 });
  
  // 找登录表单
  const loginForm = await page.$('.n-card');
  if (loginForm) {
    console.log('Login form found, filling...');
    // 尝试填写登录信息
    const inputs = await page.$$('input');
    if (inputs.length >= 2) {
      await inputs[0].fill('admin');
      await inputs[1].fill('admin123');
      const submitBtn = await page.$('button[type="submit"], .n-button--primary');
      if (submitBtn) {
        await submitBtn.click();
        await page.waitForTimeout(2000);
      }
    }
  }
  
  // 保存登录页截图
  await page.screenshot({ path: 'docs/evaluations/screenshots/01-login.png', fullPage: false });
  console.log('Screenshot: 01-login.png');

  // 遍历各页面
  for (const p of pages.slice(1)) {
    try {
      console.log('Navigating to ' + p.url + '...');
      await page.goto('http://localhost:9527' + p.url, { waitUntil: 'networkidle', timeout: 15000 });
      await page.waitForTimeout(1000);
      await page.screenshot({ path: 'docs/evaluations/screenshots/' + p.name + '.png', fullPage: false });
      console.log('Screenshot: ' + p.name + '.png');
    } catch (e) {
      console.log('Error on ' + p.url + ': ' + e.message);
    }
  }

  await browser.close();
  console.log('Done!');
}

screenshotPages().catch(console.error);
