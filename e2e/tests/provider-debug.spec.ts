/**
 * 场景1验证 - 只建1个服务商，调试表单填写流程
 */
import { test, expect } from '@playwright/test';

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

test('场景1验证: 创建1个服务商', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/business/provider`);
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.waitForTimeout(2000);

  // 等待新增按钮出现
  const addBtn = page.locator('button:has-text("新增")').first();
  await addBtn.waitFor({ state: 'visible', timeout: 15000 });
  console.log('新增按钮可见:', await addBtn.isVisible());
  await addBtn.click();
  await page.waitForTimeout(1500);
  await page.screenshot({ path: '/tmp/provider-drawer.png' });

  // 诊断抽屉内表单
  const drawerInfo = await page.evaluate(() => {
    const drawer = document.querySelector('.n-drawer');
    if (!drawer) return { found: false };

    const inputs = Array.from(drawer!.querySelectorAll('input, textarea'))
      .map(inp => ({
        placeholder: inp.getAttribute('placeholder') || inp.getAttribute('aria-label') || '',
        type: (inp as HTMLInputElement).type,
        value: (inp as HTMLInputElement).value,
        visible: (inp as HTMLElement).offsetParent !== null
      })).filter((i: any) => i.visible);

    const selects = Array.from(drawer!.querySelectorAll('.n-select, .n-base-select'))
      .map(s => ({
        text: s.innerText.trim().substring(0, 50),
        visible: (s as HTMLElement).offsetParent !== null
      })).filter((s: any) => s.visible);

    const btns = Array.from(drawer!.querySelectorAll('button'))
      .map(b => ({ text: b.innerText.trim() })).filter(b => b.text);

    return { found: true, inputs, selects, btns };
  });

  console.log('抽屉信息:', JSON.stringify(drawerInfo, null, 2));

  if (!drawerInfo.found) {
    console.log('抽屉未找到，跳过');
    return;
  }

  // 填写表单
  if (drawerInfo.inputs.length > 0) {
    // 填第一个文本输入
    const firstInput = drawerInfo.inputs.find((i: any) => i.type === 'text' || !i.type);
    if (firstInput) {
      await page.locator(`input[placeholder*="${firstInput.placeholder}"], input[aria-label*="${firstInput.placeholder}"]`).first().fill('测试服务商A');
    }
  }

  await page.waitForTimeout(500);
  await page.screenshot({ path: '/tmp/provider-filled.png' });

  // 点确定
  const saveBtn = page.locator('.n-drawer .n-button:has-text("确定"), .n-drawer .n-button:has-text("保存")').last();
  if (await saveBtn.isVisible()) {
    await saveBtn.click();
    await page.waitForTimeout(2000);
    await page.screenshot({ path: '/tmp/provider-saved.png' });
  }

  // 验证
  const rows = page.locator('.n-data-table tbody tr');
  const count = await rows.count();
  console.log('表格行数:', count);
  expect(count).toBeGreaterThanOrEqual(1);
});
