/**
 * API 路径探测 - 确认各模块列表真实返回结构
 */
import { test } from '@playwright/test';

const API = 'https://wisdomdance.cn/jxy/api';

async function getToken(page: any) {
  const resp = await page.request.post(`${API}/auth/login`, {
    data: { username: 'admin', password: 'admin123' }
  });
  return (await resp.json()).data?.accessToken || '';
}

test('探测各表正确列表路径和字段', async ({ page }) => {
  const token = await getToken(page);

  const tests = [
    // providers
    { path: '/providers/list?page=1&size=3' },
    { path: '/provider/list?page=1&size=3' },
    { path: '/providers?page=1&size=3' },
    // staff
    { path: '/staff/list?page=1&size=3' },
    { path: '/staffs?page=1&size=3' },
    // elders
    { path: '/elders?page=1&size=3' },
    { path: '/elder/list?page=1&size=3' },
    // orders
    { path: '/orders/list?page=1&size=3' },
    { path: '/order/list?page=1&size=3' },
    { path: '/orders?page=1&size=3' },
    // service logs
    { path: '/service-logs/list?page=1&size=3' },
    { path: '/service-log/list?page=1&size=3' },
    // appointments
    { path: '/appointment/list?page=1&size=3' },
    { path: '/appointments?page=1&size=3' },
  ];

  for (const t of tests) {
    const resp = await page.request.get(`${API}${t.path}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const body = await resp.json();
    const data = body.data;

    // 分析返回结构
    let info = `HTTP ${resp.status()} `;
    if (data === null || data === undefined) {
      info += 'null';
    } else if (Array.isArray(data)) {
      info += `array[${data.length}]`;
    } else if (typeof data === 'object') {
      const keys = Object.keys(data).slice(0, 8);
      const records = data.records || data.list || data.data;
      if (Array.isArray(records)) {
        info += `obj{${keys.join(',')}} records[${records.length}]`;
        if (records.length > 0) {
          const first = records[0];
          const fKeys = Object.keys(first).slice(0, 5).join(',');
          info += ` → ${fKeys}`;
        }
      } else {
        info += `obj{${keys.join(',')}}`;
      }
    }
    console.log(`${t.path.padEnd(50)} => ${info}`);
  }

  // 探测 provider 列表（确认 ID 字段名）
  console.log('\n--- Provider 详情 ---');
  const pResp = await page.request.get(`${API}/providers/list?page=1&size=3`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  const pBody = await pResp.json();
  console.log(JSON.stringify(pBody.data, null, 2).substring(0, 500));

  // 探测 order 详情
  console.log('\n--- Order 详情 ---');
  const oResp = await page.request.get(`${API}/orders/list?page=1&size=3`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  const oBody = await oResp.json();
  console.log(JSON.stringify(oBody.data, null, 2).substring(0, 500));
});
