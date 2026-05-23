/**
 * API探测 - 确认正确路径和请求格式
 */
import { test, expect, request } from '@playwright/test';

const API = 'https://wisdomdance.cn/jxy/api';

async function getToken(page: any) {
  const resp = await page.request.post(`${API}/auth/login`, {
    data: { username: 'admin', password: 'admin123' }
  });
  return (await resp.json()).data?.accessToken || '';
}

test('探测各模块正确路径', async ({ page }) => {
  const token = await getToken(page);
  expect(token).toBeTruthy();

  const endpoints = [
    { path: '/providers', data: { providerName: '探测服务商A', creditCode: '91110000MA7JQ00A01', providerType: 'ELDER_CARE', serviceCategory: 'ELDER_CARE', legalPerson: '张', contactPhone: '13800001111', status: 'ENABLED' }, name: 'provider' },

    // staff - 先探测正确路径
    { path: '/staff', data: { staffName: '探测员工', phone: '13900001001', gender: 0, status: 'ACTIVE' }, name: 'staff' },

    // elder
    { path: '/elders', data: { elderName: '探测老人', elderIdCard: '610102199001010011', elderPhone: '13900002001', gender: 0 }, name: 'elder' },
  ];

  for (const ep of endpoints) {
    const resp = await page.request.post(`${API}${ep.path}`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: ep.data
    });
    const body = await resp.json();
    console.log(`POST ${ep.path}: HTTP ${resp.status()} code=${body.code} msg=${(body.message || '').substring(0, 80)}`);
  }

  // 探测订单路径
  const listResp = await page.request.get(`${API}/providers/list?page=1&size=3`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  const pList = (await listResp.json()).data?.records || [];
  const providerId = pList[0]?.providerId;
  console.log('\nproviderId:', providerId);

  if (providerId) {
    const apptResp = await page.request.get(`${API}/appointment/list?page=1&size=3`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const appointments = (await apptResp.json()).data?.records || [];
    const apptId = appointments[0]?.appointmentId;
    console.log('appointmentId:', apptId);

    if (apptId) {
      // 探测确认路径
      const confirmPaths = [
        `/appointment/confirm/${apptId}`,
        `/appointment/${apptId}/confirm`,
        `/appointments/confirm/${apptId}`,
      ];
      for (const p of confirmPaths) {
        const r = await page.request.post(`${API}${p}`, {
          headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
          data: { providerId, appointmentTime: new Date().toISOString() }
        });
        const b = await r.json();
        console.log(`POST ${p}: HTTP ${r.status()} code=${b.code} msg=${(b.message || '').substring(0, 80)}`);
      }
    }
  }
});

test('探测订单操作路径', async ({ page }) => {
  const token = await getToken(page);

  // 查订单列表
  const resp = await page.request.get(`${API}/orders/list?page=1&size=3`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  const orders = (await resp.json()).data?.records || [];
  const orderId = orders[0]?.orderId;
  console.log('orderId:', orderId);

  if (orderId) {
    const ops = [
      { path: `/orders/confirm/${orderId}`, data: {} },
      { path: `/orders/start/${orderId}`, data: {} },
      { path: `/orders/complete/${orderId}`, data: {} },
      { path: `/orders/${orderId}/confirm`, data: {} },
    ];
    for (const op of ops) {
      const r = await page.request.post(`${API}${op.path}`, {
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        data: op.data
      });
      const b = await r.json();
      console.log(`POST ${op.path}: HTTP ${r.status()} code=${b.code}`);
    }
  }
});

test('确认最终数据状态', async ({ page }) => {
  const token = await getToken(page);

  const tables = [
    { name: 'providers', path: '/providers/list?page=1&size=5' },
    { name: 'staff', path: '/staff/list?page=1&size=5' },
    { name: 'elders', path: '/elders/list?page=1&size=5' },
    { name: 'appointments', path: '/appointment/list?page=1&size=5' },
    { name: 'orders', path: '/orders/list?page=1&size=5' },
  ];

  for (const t of tables) {
    const r = await page.request.get(`${API}${t.path}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const b = await r.json();
    const records = b.data?.records || [];
    console.log(`${t.name}: ${records.length}条`);
  }
});
