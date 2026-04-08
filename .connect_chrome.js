const { chromium } = require('playwright');

(async () => {
  const browser = await chromium.connectOverCDP('ws://127.0.0.1:9222');
  const context = browser.contexts()[0] || await browser.newContext();
  const page = context.pages()[0] || await context.newPage();
  
  console.log('Connected! Page URL:', page.url());
  
  await page.screenshot({ path: 'screenshots/chrome_connected.png' });
  console.log('Screenshot saved!');
  
  // Save the cdpSession for later use
  const cdpSession = await page.context().newCDPSession(page);
  process.stdout.write('READY\n');
})();
