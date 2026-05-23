/**
 * 诊断派单500根因
 */
import { test } from '@playwright/test';

const API = 'https://wisdomdance.cn/jxy/api';

async function getToken(page: any) {
  const resp = await page.request.post(`${API}/auth/login`, {
    data: { username: 'admin', password: 'admin123' }
  });
  return (await resp.json()).data?.accessToken || '';
}

test('诊断派单根因', async ({ page }) => {
  const token = await getToken(page);

  // 拿一个待确认的预约
  const { body: apptBody } = await (page.request.get(`${API}/appointment/list?page=1&size=3`, {
    headers: { Authorization: `Bearer ${token}` }
  }));
  const appt = (apptBody.data?.records || [])[0];
  console.log('预约:', JSON.stringify(appt, null, 2));

  // 拿一个 provider
  const { body: pBody } = await (page.request.get(`${API}/providers?page=1&size=1`, {
    headers: { Authorization: `Bearer ${token}` }
  }));
  const provider = (pBody.data?.records || [])[0];
  console.log('服务商:', JSON.stringify(provider, null, 2));

  const apptId = appt?.appointmentId;
  const providerId = provider?.providerId;
  console.log('\napptId:', apptId, '  providerId:', providerId);

  // 探测 confirm 接口的 body 字段
  const paths = [
    `/appointment/confirm/${apptId}`,
    `/appointments/confirm/${apptId}`,
    `/appointment/${apptId}/confirm`,
  ];
  for (const p of paths) {
    const r = await page.request.post(`${API}${p}`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: { providerId }
    });
    const b = await r.json();
    console.log(`POST ${p} → ${r.status()} code=${b.code} msg=${(b.message||'').substring(0,200)}`);
  }

  // 探测 order confirm 路径
  console.log('\n--- 探测 staff list 500 根因 ---');
  const sR = await page.request.get(`${API}/staff/list?page=1&size=1`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  console.log(`staff/list: ${sR.status()}`, await sR.text().then(t => t.substring(0, 200)));

  // 尝试 elders 路径获取 staff（通过订单找）
  console.log('\n--- 通过 elder/appointment/staff 联合探测 ---');
  const elderR = await page.request.get(`${API}/elders?page=1&size=1`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  const elder = (await elderR.json()).data?.records?.[0];
  console.log('elder:', JSON.stringify(elder));
});
