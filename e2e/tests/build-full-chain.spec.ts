/**
 * 完整数据链路构造 - 基于探明确的路径和字段
 * appointment(confirm) → order(dispatch→receive→start) → service-log → quality → evaluation
 */
import { test, expect } from '@playwright/test';

const API = 'https://wisdomdance.cn/jxy/api';

async function getToken(page: any) {
  const resp = await page.request.post(`${API}/auth/login`, {
    data: { username: 'admin', password: 'admin123' }
  });
  return (await resp.json()).data?.accessToken;
}

async function apiGet(page: any, path: string, token: string) {
  const r = await page.request.get(`${API}${path}`, { headers: { Authorization: `Bearer ${token}` } });
  return { status: r.status(), body: await r.json() };
}

async function apiPost(page: any, path: string, data: any, token: string) {
  const r = await page.request.post(`${API}${path}`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data
  });
  return { status: r.status(), body: await r.json() };
}

async function apiPut(page: any, path: string, data: any, token: string) {
  const r = await page.request.put(`${API}${path}`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data
  });
  return { status: r.status(), body: await r.json() };
}

function log(...args: any[]) {
  const msg = args.map(a => typeof a === 'string' ? a.substring(0, 150) : JSON.stringify(a)).join(' ');
  console.log(msg);
}

function extractRecords(body: any) {
  if (!body?.data) return [];
  if (Array.isArray(body.data)) return body.data;
  if (Array.isArray(body.data.records)) return body.data.records;
  return [];
}

// ====== 1. 建服务商 ======
test('① 建服务商 ×3', async ({ page }) => {
  const token = await getToken(page);
  const providers = [
    { providerName: '延安养老服务A社', creditCode: '91610113MA7JQ00B01', providerType: 'ELDER_CARE', serviceCategory: 'ELDER_CARE', legalPerson: '张经理', contactPhone: '13800001111', address: '延安市宝塔区服务路1号', status: 'ENABLED' },
    { providerName: '延安养老服务B社', creditCode: '91610113MA7JQ00B02', providerType: 'HOME_CARE', serviceCategory: 'HOME_CARE', legalPerson: '李经理', contactPhone: '13800002222', address: '延安市宝塔区服务路2号', status: 'ENABLED' },
    { providerName: '延安养老服务C社', creditCode: '91610113MA7JQ00B03', providerType: 'TECH_SERVICE', serviceCategory: 'ELDER_CARE', legalPerson: '王经理', contactPhone: '13800003333', address: '延安市宝塔区服务路3号', status: 'ENABLED' },
  ];
  for (const p of providers) {
    const r = await apiPost(page, '/providers', p, token);
    log('provider', p.providerName, r.status, r.body.code);
  }
  const { body } = await apiGet(page, '/providers?page=1&size=5', token);
  expect(extractRecords(body).length).toBeGreaterThanOrEqual(3);
});

// ====== 2. 建服务人员 ======
test('② 建服务人员 ×6', async ({ page }) => {
  const token = await getToken(page);
  const { body: pBody } = await apiGet(page, '/providers?page=1&size=1', token);
  const provider = extractRecords(pBody)[0];
  const providerId = provider?.providerId;
  expect(providerId).toBeTruthy();

  const staffList = [
    { staffName: '张服务员A', phone: '13900001001', gender: '0', idCard: '610102199001010011', status: 'ON_JOB' },
    { staffName: '李服务员B', phone: '13900001002', gender: '1', idCard: '610102199001010012', status: 'ON_JOB' },
    { staffName: '王服务员C', phone: '13900001003', gender: '0', idCard: '610102199001010013', status: 'ON_JOB' },
    { staffName: '赵服务员D', phone: '13900001004', gender: '1', idCard: '610102199001010014', status: 'ON_JOB' },
    { staffName: '刘服务员E', phone: '13900001005', gender: '0', idCard: '610102199001010015', status: 'ON_JOB' },
    { staffName: '陈服务员F', phone: '13900001006', gender: '1', idCard: '610102199001010016', status: 'ON_JOB' },
  ];
  for (const s of staffList) {
    const r = await apiPost(page, '/staff', { ...s, providerId }, token);
    log('staff', s.staffName, r.status, r.body.code);
  }
  const { body } = await apiGet(page, '/staff?page=1&size=10', token);
  expect(extractRecords(body).length).toBeGreaterThanOrEqual(6);
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
    const r = await apiPost(page, '/elders', e, token);
    log('elder', e.name, r.status, r.body.code);
  }
  const { body } = await apiGet(page, '/elders?page=1&size=15', token);
  expect(extractRecords(body).length).toBeGreaterThanOrEqual(10);
});

// ====== 4. 派单确认预约 ======
test('④ 派单（PUT appointment/{id}/confirm）', async ({ page }) => {
  const token = await getToken(page);
  const { body: pBody } = await apiGet(page, '/providers?page=1&size=1', token);
  const providerId = extractRecords(pBody)[0]?.providerId;
  expect(providerId).toBeTruthy();

  const { body: apptBody } = await apiGet(page, '/appointment/list?page=1&size=10', token);
  const appointments = extractRecords(apptBody);
  log('appointments(PENDING)', appointments.length);

  let ok = 0;
  for (const appt of appointments) {
    const r = await apiPut(page, `/appointment/${appt.appointmentId}/confirm`, {
      providerId,
      appointmentTime: new Date().toISOString()
    }, token);
    log('confirm', appt.appointmentId?.toString().substring(0, 10), '→', r.body.code, (r.body.message || '').substring(0, 40));
    if (r.body.code === 200) ok++;
  }
  log('派单成功', ok, '条');
  expect(ok).toBeGreaterThan(0);
});

// ====== 5. 订单派单 dispatch ======
test('⑤ 订单派单 dispatch（POST orders/{id}/dispatch）', async ({ page }) => {
  const token = await getToken(page);

  const { body: pBody } = await apiGet(page, '/providers?page=1&size=1', token);
  const providerId = extractRecords(pBody)[0]?.providerId;

  const { body: sBody } = await apiGet(page, '/staff?page=1&size=1', token);
  const staffId = extractRecords(sBody)[0]?.staffId;
  expect(staffId).toBeTruthy();

  const { body: oBody } = await apiGet(page, '/orders?page=1&size=5', token);
  const orders = extractRecords(oBody).filter((o: any) => o.status === 'CREATED');
  log('orders(CREATED)', orders.length, orders.map((o: any) => o.orderId?.toString().substring(0, 8)));

  let ok = 0;
  for (const order of orders) {
    const r = await apiPost(page, `/orders/${order.orderId}/dispatch`, {
      providerId,
      staffId,
      dispatchType: 'MANUAL'
    }, token);
    log('dispatch', order.orderId?.toString().substring(0, 8), '→', r.body.code, (r.body.message || '').substring(0, 60));
    if (r.body.code === 200) ok++;
  }
  log('dispatch成功', ok, '条');
  expect(ok).toBeGreaterThan(0);
});

// ====== 6. 订单接单+开始 ======
test('⑥ 订单接单+开始（PUT orders/{id}/receive/start）', async ({ page }) => {
  const token = await getToken(page);

  const { body: oBody } = await apiGet(page, '/orders?page=1&size=5', token);
  // RECEIVED 订单（已dispatch但未receive）
  const orders = extractRecords(oBody);
  log('all orders', orders.map((o: any) => `${o.orderId?.toString().substring(0,8)}:${o.status}`));

  for (const order of orders) {
    if (order.status === 'RECEIVED' || order.status === 'SERVICE_STARTED') {
      continue;
    }
    // receive
    const r1 = await apiPut(page, `/orders/${order.orderId}/receive`, {}, token);
    log('receive', order.orderId?.toString().substring(0,8), r1.body.code);

    // start
    const r2 = await apiPut(page, `/orders/${order.orderId}/start`, {}, token);
    log('start', order.orderId?.toString().substring(0,8), r2.body.code);
  }
});

// ====== 7. 提交服务日志 ======
test('⑦ 提交服务日志（POST service-log）', async ({ page }) => {
  const token = await getToken(page);

  const { body: oBody } = await apiGet(page, '/orders?page=1&size=10', token);
  const orders = extractRecords(oBody).filter((o: any) =>
    o.status === 'SERVICE_STARTED' || o.status === 'RECEIVED'
  );
  log('SERVICE_STARTED orders', orders.length);

  const { body: sBody } = await apiGet(page, '/staff?page=1&size=1', token);
  const staffId = extractRecords(sBody)[0]?.staffId;

  let ok = 0;
  for (const order of orders.slice(0, 5)) {
    const r = await apiPost(page, '/service-log', {
      orderId: order.orderId,
      orderNo: order.orderNo,
      staffId,
      serviceContent: '养老服务按时完成，老人状态良好，配合度良好。',
      serviceStartTime: new Date(Date.now() - 3600000).toISOString(),
      serviceEndTime: new Date().toISOString(),
      serviceDuration: 60,
      remarks: '服务正常完成'
    }, token);
    log('service-log', order.orderId?.toString().substring(0,8), '→', r.body.code, (r.body.message||'').substring(0,40));
    if (r.body.code === 200) ok++;
  }
  log('service-log成功', ok, '条');
  expect(ok).toBeGreaterThan(0);
});

// ====== 8. 状态汇总 ======
test('⑧ 最终数据状态', async ({ page }) => {
  const token = await getToken(page);
  const tables = [
    ['providers', '/providers?page=1&size=5'],
    ['staff', '/staff?page=1&size=5'],
    ['elders', '/elders?page=1&size=5'],
    ['appointments', '/appointment/list?page=1&size=5'],
    ['orders', '/orders?page=1&size=5'],
    ['serviceLogs', '/service-log/list?page=1&size=5'],
  ];
  for (const [name, path] of tables) {
    const { body } = await apiGet(page, path, token);
    const recs = extractRecords(body);
    log(name, recs.length);
  }
});
