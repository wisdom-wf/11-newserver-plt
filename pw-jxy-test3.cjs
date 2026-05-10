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

  // 直接goto健康档案
  await page.goto('https://wisdomdance.cn/jxy/#/business/health-archive', { timeout: 20000 });
  
  // 等待更长时间让Vue渲染
  await page.waitForTimeout(8000);
  
  await page.screenshot({ path: '/tmp/pw-health-archive.png', fullPage: true });
  console.log('URL:', page.url());
  
  // 检查页面内容
  const bodyText = await page.locator('body').textContent();
  console.log('Body snippet:', bodyText.substring(0, 600));
  
  // 查找NTabs
  const tabsEl = await page.locator('.n-tabs, [class*="tabs"]').count();
  console.log('Tabs elements:', tabsEl);
  
  // 查找任何包含"健康"的文字
  const healthText = await page.locator(':text("健康测量")').count();
  console.log('Contains 健康测量:', healthText);
  
  // 查看网络请求
  console.log('Console errors:', errors.length ? errors.join('\n') : 'none');
  await browser.close();
})();
