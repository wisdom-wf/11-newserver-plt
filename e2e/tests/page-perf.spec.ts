import { test, expect } from '@playwright/test';

test('各模块表格数据渲染 < 4秒', async ({ page }) => {
  const errors: string[] = [];
  page.on('console', msg => { if (msg.type() === 'error') errors.push(msg.text()); });
  
  // 登录
  const t0 = Date.now();
  await page.goto('https://wisdomdance.cn/jxy/');
  await page.getByText('超级管理员').click();
  await page.waitForURL('**/home', { timeout: 15000 });
  console.log('登录:', Date.now() - t0, 'ms');
  
  // 服务日志 - 从点击到表格行出现
  const t1 = Date.now();
  await page.getByText('业务管理').click();
  await page.waitForTimeout(300);
  await page.getByText('服务日志').click();
  await page.waitForSelector('.n-data-table-tr', { timeout: 15000 });
  const sl = Date.now() - t1;
  console.log('服务日志:', sl, 'ms', sl < 4000 ? 'PASS' : 'FAIL');
  
  // 健康档案 - 等待数据加载
  const t2 = Date.now();
  await page.getByText('健康档案').click();
  await page.waitForSelector('.n-data-table-tr', { timeout: 15000 }).catch(() => {});
  const ha = Date.now() - t2;
  console.log('健康档案:', ha, 'ms', ha < 4000 ? 'PASS' : 'FAIL');
  
  // 服务商管理
  const t3 = Date.now();
  await page.getByText('服务商管理').click();
  await page.waitForSelector('.n-data-table-tr', { timeout: 15000 }).catch(() => {});
  const pv = Date.now() - t3;
  console.log('服务商管理:', pv, 'ms', pv < 4000 ? 'PASS' : 'FAIL');
  
  if (errors.length > 0) console.log('Errors:', errors.slice(0, 3));
  
  expect(sl).toBeLessThan(4000);
  expect(ha).toBeLessThan(4000);
  expect(pv).toBeLessThan(4000);
});
