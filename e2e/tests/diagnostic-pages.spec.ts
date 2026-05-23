/**
 * 页面诊断 - 输出真实 DOM 元素
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
  await page.waitForTimeout(1500);
}

test('诊断服务商页面', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/provider`);
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.waitForTimeout(2000);

  const info = await page.evaluate(() => {
    const results: any = {};

    // 所有按钮
    const btns = Array.from(document.querySelectorAll('button'));
    results.buttons = btns.map(b => ({
      text: b.innerText.trim(),
      visible: b.offsetParent !== null,
      class: b.className
    })).filter((b: any) => b.visible && b.text.length < 20);

    // 新增相关按钮
    results.addBtns = btns
      .filter(b => /新增|添加|创建/i.test(b.innerText))
      .map(b => ({ text: b.innerText.trim(), rect: b.getBoundingClientRect() }));

    // 表格相关
    results.tableHeaders = Array.from(document.querySelectorAll('th'))
      .map(th => th.innerText.trim()).filter(Boolean);
    results.tableRows = document.querySelectorAll('tbody tr').length;

    // 表单输入
    results.inputs = Array.from(document.querySelectorAll('input, textarea'))
      .map(inp => ({
        placeholder: inp.getAttribute('placeholder') || inp.getAttribute('aria-label') || '',
        type: inp.type,
        visible: (inp as HTMLElement).offsetParent !== null
      })).filter((i: any) => i.visible);

    // 下拉框
    results.selects = Array.from(document.querySelectorAll('.n-select'))
      .map(s => ({ text: s.innerText.trim(), visible: s.offsetParent !== null }));

    return results;
  });

  console.log('=== 页面诊断 ===');
  console.log('表格行数:', info.tableRows);
  console.log('表头:', JSON.stringify(info.tableHeaders));
  console.log('可见按钮:', JSON.stringify(info.buttons.slice(0, 10), null, 2));
  console.log('新增按钮:', JSON.stringify(info.addBtns, null, 2));
  console.log('表单输入:', JSON.stringify(info.inputs.slice(0, 8), null, 2));
  console.log('下拉框:', JSON.stringify(info.selects.slice(0, 8), null, 2));
});

test('诊断老人档案页面', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/business/elder`);
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.waitForTimeout(2000);

  const info = await page.evaluate(() => {
    const btns = Array.from(document.querySelectorAll('button'));
    return {
      addBtns: btns.filter(b => /新增|添加|创建/i.test(b.innerText))
        .map(b => ({ text: b.innerText.trim(), rect: b.getBoundingClientRect() })),
      tableHeaders: Array.from(document.querySelectorAll('th'))
        .map(th => th.innerText.trim()).filter(Boolean),
      tableRows: document.querySelectorAll('tbody tr').length,
      inputs: Array.from(document.querySelectorAll('input, textarea'))
        .map(inp => ({
          placeholder: inp.getAttribute('placeholder') || '',
          type: inp.type,
          visible: (inp as HTMLElement).offsetParent !== null
        })).filter((i: any) => i.visible).slice(0, 10),
      selects: Array.from(document.querySelectorAll('.n-select'))
        .map(s => ({ text: s.innerText.trim() })).slice(0, 8)
    };
  });

  console.log('\n=== 老人档案页面诊断 ===');
  console.log('表格行数:', info.tableRows);
  console.log('表头:', JSON.stringify(info.tableHeaders));
  console.log('新增按钮:', JSON.stringify(info.addBtns, null, 2));
  console.log('输入框:', JSON.stringify(info.inputs, null, 2));
  console.log('下拉框:', JSON.stringify(info.selects, null, 2));
});

test('诊断服务人员页面', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/business/staff`);
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.waitForTimeout(2000);

  const info = await page.evaluate(() => {
    const btns = Array.from(document.querySelectorAll('button'));
    return {
      addBtns: btns.filter(b => /新增|添加|创建/i.test(b.innerText))
        .map(b => ({ text: b.innerText.trim() })),
      tableHeaders: Array.from(document.querySelectorAll('th'))
        .map(th => th.innerText.trim()).filter(Boolean),
      tableRows: document.querySelectorAll('tbody tr').length,
      inputs: Array.from(document.querySelectorAll('input, textarea'))
        .map(inp => ({
          placeholder: inp.getAttribute('placeholder') || '',
          visible: (inp as HTMLElement).offsetParent !== null
        })).filter((i: any) => i.visible).slice(0, 10),
      selects: Array.from(document.querySelectorAll('.n-select'))
        .map(s => ({ text: s.innerText.trim() })).slice(0, 8)
    };
  });

  console.log('\n=== 服务人员页面诊断 ===');
  console.log('表格行数:', info.tableRows);
  console.log('表头:', JSON.stringify(info.tableHeaders));
  console.log('新增按钮:', JSON.stringify(info.addBtns, null, 2));
  console.log('输入框:', JSON.stringify(info.inputs, null, 2));
  console.log('下拉框:', JSON.stringify(info.selects, null, 2));
});
