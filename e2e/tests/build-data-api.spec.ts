/**
 * 数据构造 v2 - API 直调，基于真实 DTO 字段
 * Provider → Staff → Elder → Appointment confirm → Order advance → ServiceLog
 */
import { test, expect, request } from '@playwright/test';

const API = 'https://wisdomdance.cn/jxy/api';

async function getToken(page: any): Promise<string> {
  const resp = await page.request.post(`${API}/auth/login`, {
    data: { username: 'admin', password: 'admin123' }
  });
  return (await resp.json()).data?.accessToken || '';
}

async function post(page: any, path: string, data: any, token: string) {
  const resp = await page.request.post(`${API}${path}`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data
  });
  return { status: resp.status(), body: await resp.json() };
}

async function get(page: any, path: string, token: string) {
  const resp = await page.request.get(`${API}${path}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  return { status: resp.status(), body: await resp.json() };
}

function log(label: string, ...args: any[]) {
  const msg = args.map(a => typeof a === 'string' ? a.substring(0, 120) : JSON.stringify(a)).join(' ');
  console.log(`[${label}] ${msg}`);
}

// ====== 1. 建服务商 ======

test('① 建服务商 ×3', async ({ page }) => {
  const token = await getToken(page);
  expect(token.length).toBeGreaterThan(10);

  const providers = [
    { providerName: '延安养老服务A', creditCode: '91610113MA7JQ00A01', providerType: 'ELDER_CARE', serviceCategory: 'ELDER_CARE', legalPerson: '张经理', contactPhone: '13800001111', address: '延安市宝塔区服务路1号', status: 'ENABLED' },
    { providerName: '延安养老服务B', creditCode: '91610113MA7JQ00A02', providerType: 'HOME_CARE', serviceCategory: 'HOME_CARE', legalPerson: '李经理', contactPhone: '13800002222', address: '延安市宝塔区服务路2号', status: 'ENABLED' },
    { providerName: '延安养老服务C', creditCode: '91610113MA7JQ00A03', providerType: 'TECH_SERVICE', serviceCategory: 'ELDER_CARE', legalPerson: '王经理', contactPhone: '13800003333', address: '延安市宝塔区服务路3号', status: 'ENABLED' },
  ];

  for (const p of providers) {
    const r = await post(page, '/providers', p, token);
    log('provider', p.providerName, r.status, r.body.code, (r.body.message || '').substring(0, 60));
  }

  const { body } = await get(page, '/providers?page=1&size=5', token);
  const count = body.data?.records?.length || 0;
  log('providers', `共${count}条`);
  expect(count).toBeGreaterThanOrEqual(3);
});

// ====== 2. 建服务人员 ======

test('② 建服务人员 ×6', async ({ page }) => {
  const token = await getToken(page);

  // 取第一个 provider
  const { body: pBody } = await get(page, '/providers?page=1&size=1', token);
  const providerId = pBody.data?.records?.[0]?.providerId;
  log('providerId', providerId);
  expect(providerId).toBeTruthy();

  const staffList = [
    { staffName: '张服务员', phone: '13900001001', gender: '0', idCard: '610102199001010001', status: 'ACTIVE' },
    { staffName: '李服务员', phone: '13900001002', gender: '1', idCard: '610102199001010002', status: 'ACTIVE' },
    { staffName: '王服务员', phone: '13900001003', gender: '0', idCard: '610102199001010003', status: 'ACTIVE' },
    { staffName: '赵服务员', phone: '13900001004', gender: '1', idCard: '610102199001010004', status: 'ACTIVE' },
    { staffName: '刘服务员', phone: '13900001005', gender: '0', idCard: '610102199001010005', status: 'ACTIVE' },
    { staffName: '陈服务员', phone: '13900001006', gender: '1', idCard: '610102199001010006', status: 'ACTIVE' },
  ];

  for (const s of staffList) {
    const r = await post(page, '/staff', { ...s, providerId }, token);
    log('staff', s.staffName, r.status, r.body.code, (r.body.message || '').substring(0, 80));
  }

  const { body } = await get(page, '/staff/list?page=1&size=10', token);
  const count = body.data?.records?.length || 0;
  log('staff', `共${count}条`);
  expect(count).toBeGreaterThanOrEqual(6);
});

// ====== 3. 建老人档案 ======

test('③ 建老人档案 ×10', async ({ page }) => {
  const token = await getToken(page);

  const elders = [
    { name: '张大爷', gender: 0, birthDate: '1945-03-15', idCard: '612601194503150011', phone: '13900002001', address: '延安市宝塔区东惠苑1号', careLevel: 1 },
    { name: '李奶奶', gender: 1, birthDate: '1948-07-22', idCard: '612601194807220022', phone: '13900002002', address: '延安市宝塔区农昌园2号', careLevel: 2 },
    { name: '王大爷', gender: 0, birthDate: '1942-11-08', idCard: '612601194211080033', phone: '13900002003', address: '延安市宝塔区新区杨家岭北苑3号', careLevel: 1 },
    { name: '赵阿姨', gender: 1, birthDate: '1950-05-18', idCard: '612601195005180044', phone: '13900002004', address: '延安市宝塔区东苑路4号', careLevel: 0 },
    { name: '刘大爷', gender: 0, birthDate: '1940-09-30', idCard: '612601194009300055', phone: '13900002005', address: '延安市宝塔区服务路5号', careLevel: 2 },
    { name: '陈奶奶', gender: 1, birthDate: '1946-12-01', idCard: '612601194612010066', phone: '13900002006', address: '延安市宝塔区长安路6号', careLevel: 1 },
    { name: '孙大爷', gender: 0, birthDate: '1944-04-25', idCard: '612601194404250077', phone: '13900002007', address: '延安市宝塔区科技路7号', careLevel: 3 },
    { name: '周阿姨', gender: 1, birthDate: '1952-08-14', idCard: '612601195208140088', phone: '13900002008', address: '延安市宝塔区凤城路8号', careLevel: 0 },
    { name: '吴大爷', gender: 0, birthDate: '1943-01-20', idCard: '612601194301200099', phone: '13900002009', address: '延安市宝塔区长乐路9号', careLevel: 1 },
    { name: '黄奶奶', gender: 1, birthDate: '1947-06-05', idCard: '612601194706050100', phone: '13900002010', address: '延安市宝塔区劳动路10号', careLevel: 2 },
  ];

  for (const e of elders) {
    const r = await post(page, '/elders', e, token);
    log('elder', e.name, r.status, r.body.code, (r.body.message || '').substring(0, 80));
  }

  const { body } = await get(page, '/elders?page=1&size=15', token);
  const count = body.data?.records?.length || 0;
  log('elders', `共${count}条`);
  expect(count).toBeGreaterThanOrEqual(10);
});

// ====== 4. 派单 ======

test('④ 派单（前10个待确认预约）', async ({ page }) => {
  const token = await getToken(page);

  const { body: pBody } = await get(page, '/providers?page=1&size=1', token);
  const providerId = pBody.data?.records?.[0]?.providerId;
  expect(providerId).toBeTruthy();

  const { body: apptBody } = await get(page, '/appointment/list?page=1&size=20', token);
  const appointments = apptBody.data?.records || [];
  log('appointments', `共${appointments.length}条`);

  let dispatched = 0;
  for (const appt of appointments.slice(0, 10)) {
    const r = await post(page, `/appointment/confirm/${appt.appointmentId}`, {
      providerId,
      appointmentTime: new Date().toISOString()
    }, token);
    if (r.body.code === 200 || r.body.code === 0) dispatched++;
    log('confirm', appt.appointmentId, r.body.code);
  }

  log('dispatched', `${dispatched}条`);
  expect(dispatched).toBeGreaterThan(0);
});

// ====== 5. 订单状态推进 ======

test('⑤ 订单确认+开始（各5条）', async ({ page }) => {
  const token = await getToken(page);

  // 查 CREATED → confirm
  const { body: created } = await get(page, '/orders?page=1&size=5', token);
  const orders = created.data?.records || [];
  log('orders(CREATED)', orders.length);

  for (const o of orders) {
    const r = await post(page, `/orders/confirm/${o.orderId}`, {}, token);
    log('confirm', o.orderId, r.body.code);
  }

  // 查 CONFIRMED → start
  const { body: confirmed } = await get(page, '/orders?page=1&size=5', token);
  const confirmedOrders = confirmed.data?.records || [];

  for (const o of confirmedOrders.slice(0, 3)) {
    const r = await post(page, `/orders/start/${o.orderId}`, {}, token);
    log('start', o.orderId, r.body.code);
  }
});

// ====== 6. 服务日志 ======

test('⑥ 提交服务日志（5条）', async ({ page }) => {
  const token = await getToken(page);

  const { body } = await get(page, '/orders?page=1&size=10', token);
  const orders = body.data?.records || [];
  const inProgress = orders.filter((o: any) => o.status === 'IN_PROGRESS' || o.status === 'CONFIRMED');
  log('orders', orders.map((o: any) => `${o.orderId}→${o.status}`).join(', '));

  const { body: sBody } = await get(page, '/staff/list?page=1&size=1', token);
  const staffId = sBody.data?.records?.[0]?.staffId;
  log('staffId', staffId);

  let created = 0;
  for (const o of inProgress.slice(0, 5)) {
    const r = await post(page, '/service-logs', {
      orderId: o.orderId,
      orderNo: o.orderNo,
      staffId,
      serviceContent: '养老服务按时完成，老人状态良好，配合度良好。',
      serviceStartTime: new Date(Date.now() - 3600000).toISOString(),
      serviceEndTime: new Date().toISOString(),
      serviceDuration: 60,
      remarks: '服务正常'
    }, token);
    if (r.body.code === 200 || r.body.code === 0) created++;
    log('serviceLog', o.orderId, r.body.code, (r.body.message || '').substring(0, 60));
  }

  log('serviceLogs', `创建${created}条`);
  expect(created).toBeGreaterThan(0);
});

// ====== 7. 状态汇总 ======

test('最终数据状态', async ({ page }) => {
  const token = await getToken(page);
  const tables = [
    ['providers', '/providers?page=1&size=5'],
    ['staff', '/staff/list?page=1&size=5'],
    ['elders', '/elders?page=1&size=5'],
    ['appointments', '/appointment/list?page=1&size=5'],
    ['orders', '/orders?page=1&size=5'],
    ['serviceLogs', '/service-log/list?page=1&size=5'],
  ];

  for (const [name, path] of tables) {
    const { body } = await get(page, path, token);
    const count = body.data?.records?.length ?? (Array.isArray(body.data) ? body.data.length : '?');
    log(name, count);
  }
});
