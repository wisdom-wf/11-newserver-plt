const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.launch({ headless: true });
  const page = await browser.newPage();
  const errors = [];
  page.on('console', msg => { if (msg.type() === 'error') errors.push(msg.text()); });
  page.on('pageerror', err => errors.push(err.message));

  console.log('=== 本地环境测试开始 ===');

  // 1. 访问登录页
  console.log('1. 访问登录页...');
  await page.goto('http://localhost:9527', { waitUntil: 'networkidle', timeout: 15000 });
  await page.waitForTimeout(2000);
  await page.screenshot({ path: '/tmp/local-login.png' });
  console.log('登录页截图已保存');

  // 2. 检查页面内容
  const title = await page.title();
  console.log('页面标题:', title);

  // 3. 检查是否有登录按钮
  const loginBtn = page.locator('button:has-text("登录")').first();
  if (await loginBtn.isVisible()) {
    console.log('✓ 找到登录按钮');
  } else {
    console.log('✗ 未找到登录按钮');
  }

  // 4. 检查是否有超级管理员按钮
  const adminBtn = page.locator('button:has-text("超级管理员")').first();
  if (await adminBtn.isVisible()) {
    console.log('✓ 找到超级管理员按钮');
    await adminBtn.click();
    await page.waitForTimeout(3000);
    console.log('点击超级管理员按钮');
    await page.screenshot({ path: '/tmp/local-after-admin.png' });
    console.log('登录后截图已保存');
  } else {
    console.log('✗ 未找到超级管理员按钮');
  }

  // 5. 检查控制台错误
  console.log('控制台错误:', errors.length ? errors.join('\n') : '无');

  console.log('=== 本地环境测试完成 ===');
  await browser.close();
})();