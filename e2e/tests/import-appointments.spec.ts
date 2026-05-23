/**
 * 预约导入 - 浏览器登录后用 fetch 直接上传
 */
import { test, expect } from '@playwright/test';
import * as fs from 'fs';

const FRONTEND = 'https://wisdomdance.cn/jxy';
const API = 'https://wisdomdance.cn/jxy/api';

test('导入预约Excel（30条）', async ({ page }) => {
  // 1. 浏览器登录
  await page.goto(`${FRONTEND}/login/pwd-login`);
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.locator('input[placeholder="请输入用户名"]').fill('admin');
  await page.locator('input[placeholder="请输入密码"]').fill('admin123');
  await page.locator('button:has-text("确认")').click();
  await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
  await page.waitForTimeout(2000);

  // 2. 从 localStorage 读 token
  const token = await page.evaluate(() => {
    for (const key of Object.keys(localStorage)) {
      if (key.toLowerCase().includes('token') || key.includes('WIS') || key.includes('auth')) {
        const v = localStorage.getItem(key);
        if (!v) continue;
        try {
          const parsed = JSON.parse(v);
          if (parsed.accessToken) return parsed.accessToken;
          if (typeof parsed === 'string' && parsed.length > 50) return parsed;
        } catch {}
        if (typeof v === 'string' && v.length > 50) return v;
      }
    }
    return '';
  });

  console.log('Token:', token ? `${token.substring(0, 15)}...` : 'MISSING');
  expect(token.length).toBeGreaterThan(50);

  // 3. 读取 xlsx 文件
  const xlsxPath = '/tmp/appointment_import_all.xlsx';
  const buffer = fs.readFileSync(xlsxPath);
  const base64 = buffer.toString('base64');

  // 4. 通过浏览器 fetch 上传（cookie 自动携带）
  const result = await page.evaluate(async ({ b64, apiUrl, tokenStr }) => {
    const bytes = Uint8Array.from(atob(b64), c => c.charCodeAt(0));
    const blob = new Blob([bytes], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const formData = new FormData();
    formData.append('file', blob, 'appointment_import_test.xlsx');

    const res = await fetch(`${apiUrl}/appointment/import`, {
      method: 'POST',
      headers: tokenStr ? { Authorization: `Bearer ${tokenStr}` } : {},
      body: formData,
    });
    const body = await res.json();
    return { status: res.status, body };
  }, { b64: base64, apiUrl: API, tokenStr: token });

  console.log('上传结果:', JSON.stringify(result, null, 2));

  expect(result.status).toBe(200);
  expect(result.body.code).toBe(200);
});
