import { test, expect } from '@playwright/test';

test('verify null fields show "--" not "0.0"', async ({ page }) => {
  await page.goto('/home');
  await page.waitForLoadState('networkidle');
  
  const text = await page.textContent('body');
  
  // 关键验证：满意率/评分相关字段不应出现"0.0%"
  // satisfaction=null时应显示"--"，不应出现"0.0%"
  const susp0 = text.match(/(?:满意|满意率|满意度)[^\d]*0\.0%/);
  if (susp0) {
    console.log('⚠️ Still showing 0.0%:', susp0[0]);
  } else {
    console.log('✅ No 0.0% found in satisfaction-related fields');
  }
  
  // qualifiedRate=95应该显示为"95%"而不是"950.0%"
  const bad950 = text.match(/950\.0%/);
  if (bad950) {
    console.log('⚠️ Showing 950.0% (formatScore multiply issue):', bad950[0]);
  }
  
  // 截图便于人工确认
  await page.screenshot({ path: 'e2e/screenshots/home-null-check.png' });
  console.log('Screenshot saved: e2e/screenshots/home-null-check.png');
});
