const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: false, slowMo: 100 });
  const context = await browser.newContext();
  const page = await context.newPage();
  
  // 启用请求/响应日志
  page.on('request', request => {
    if (request.url().includes('/api/')) {
      console.log('>> Request:', request.method(), request.url());
    }
  });
  
  page.on('response', async response => {
    if (response.url().includes('/api/')) {
      const status = response.status();
      const url = response.url();
      console.log('<< Response:', status, url);
      if (status >= 400) {
        try {
          const body = await response.text();
          console.log('   Error body:', body.substring(0, 500));
        } catch (e) {}
      }
    }
  });
  
  // 监听控制台错误
  page.on('console', msg => {
    if (msg.type() === 'error') {
      console.log('Console Error:', msg.text());
    }
  });
  
  // 访问登录页
  console.log('Navigating to login page...');
  await page.goto('http://localhost:9527', { waitUntil: 'networkidle' });
  
  // 等待页面加载
  await page.waitForTimeout(2000);
  
  console.log('Page loaded. Please login manually and navigate to appointment page.');
  console.log('Press Ctrl+C to stop when done.');
  
  // 保持浏览器打开以便手动操作
  await new Promise(() => {});
})();
