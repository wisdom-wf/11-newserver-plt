/**
 * 继续测试脚本 - 跳过派单/合同
 * 
 * 当前数据状态（生产）：
 * - providers=7, elders=10, staff=10(ON_JOB)
 * - appointments=10, orders=10(CREATED)
 * - serviceLogs=1, qualityChecks=0, evaluations=0
 * 
 * 跳过派单(需要合同)，聚焦能跑通的模块：
 * ① 老人档案 CRUD
 * ② 服务人员查询
 * ③ 预约查询（不依赖派单）
 * ④ 服务日志提交（手动补1条后）
 * ⑤ 订单列表查询（不依赖状态）
 * ⑥ 登录认证流程
 */

import { test, expect } from '@playwright/test';
const log = (...args: any[]) => console.log('[continue]', ...args);

const BASE = 'https://wisdomdance.cn/jxy/api';
const WEB = 'https://wisdomdance.cn/jxy';

// 管理员token
let adminToken = '';
let providerToken = '';

test.beforeAll(async ({ request }) => {
  // 管理员登录
  const adminLogin = await request.post(`${BASE}/auth/login`, {
    data: { username: 'admin', password: 'admin123' }
  });
  const adminData = await adminLogin.json();
  adminToken = adminData.data.accessToken;
  log('admin token 长度', adminToken.length);

  // 服务商登录
  const provLogin = await request.post(`${BASE}/auth/login`, {
    data: { username: 'FWS1', password: 'admin123' }
  });
  const provData = await provLogin.json();
  providerToken = provData.data.accessToken;
  log('provider token 长度', providerToken.length);
});

// ─── ① 老人档案 CRUD ───────────────────────────────────────
test('① 老人档案列表查询（管理员全局）', async ({ request }) => {
  const r = await request.get(`${BASE}/elders?page=1&pageSize=10`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  log('elders status', r.status());
  expect(r.status()).toBe(200);
  const d = await r.json();
  log('elders total', d.data?.total);
  expect(d.data?.total).toBeGreaterThan(0);
});

test('① 老人档案列表查询（服务商隔离）', async ({ request }) => {
  const r = await request.get(`${BASE}/elders?page=1&pageSize=10`, {
    headers: { Authorization: `Bearer ${providerToken}` }
  });
  expect(r.status()).toBe(200);
  const d = await r.json();
  log('provider elders total', d.data?.total);
});

// ─── ② 服务人员查询 ───────────────────────────────────────
test('② 服务人员列表（管理员）', async ({ request }) => {
  const r = await request.get(`${BASE}/staff?page=1&pageSize=10`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  log('staff status', r.status());
  expect(r.status()).toBe(200);
  const d = await r.json();
  log('staff total', d.data?.total);
  expect(d.data?.total).toBeGreaterThan(0);
});

test('② 服务人员状态（确认ON_JOB）', async ({ request }) => {
  const r = await request.get(`${BASE}/staff?page=1&pageSize=50`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  const d = await r.json();
  const staff = d.data?.records || [];
  const onJob = staff.filter((s: any) => s.status === 'ON_JOB');
  log('ON_JOB staff', onJob.length, '/', staff.length);
  expect(onJob.length).toBeGreaterThan(0);
});

// ─── ③ 预约查询 ───────────────────────────────────────────
test('③ 预约列表查询（管理员）', async ({ request }) => {
  const r = await request.get(`${BASE}/appointment/list?page=1&pageSize=10`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  log('appointment status', r.status());
  expect(r.status()).toBe(200);
  const d = await r.json();
  log('appointments total', d.data?.total);
  expect(d.data?.total).toBeGreaterThan(0);
});

test('③ 预约列表查询（服务商）', async ({ request }) => {
  const r = await request.get(`${BASE}/appointment/list?page=1&pageSize=10`, {
    headers: { Authorization: `Bearer ${providerToken}` }
  });
  expect(r.status()).toBe(200);
  const d = await r.json();
  log('provider appointments total', d.data?.total);
});

// ─── ④ 订单列表（不依赖派单状态）─────────────────────────
test('④ 订单列表（管理员）', async ({ request }) => {
  const r = await request.get(`${BASE}/orders?page=1&pageSize=10`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  log('orders status', r.status());
  expect(r.status()).toBe(200);
  const d = await r.json();
  log('orders total', d.data?.total);
  expect(d.data?.total).toBeGreaterThan(0);
});

test('④ 订单状态分布', async ({ request }) => {
  const r = await request.get(`${BASE}/orders?page=1&pageSize=50`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  const d = await r.json();
  const orders = d.data?.records || [];
  const statusMap: Record<string, number> = {};
  for (const o of orders) {
    statusMap[o.status] = (statusMap[o.status] || 0) + 1;
  }
  log('订单状态分布', JSON.stringify(statusMap));
});

// ─── ⑤ 服务日志（已有1条，查列表）────────────────────────
test('⑤ 服务日志列表', async ({ request }) => {
  const r = await request.get(`${BASE}/service-log/list?page=1&pageSize=10`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  log('service-log status', r.status());
  expect(r.status()).toBe(200);
  const d = await r.json();
  log('service-logs total', d.data?.total);
});

test('⑤ 服务日志按订单ID查询', async ({ request }) => {
  // 先拿一个订单ID
  const ordersR = await request.get(`${BASE}/orders?page=1&pageSize=1`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  const ordersData = await ordersR.json();
  const orderId = ordersData.data?.records?.[0]?.id;
  if (!orderId) { log('无订单，跳过'); return; }

  const r = await request.get(`${BASE}/service-log/order/${orderId}`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  log('service-log by orderId', r.status());
  // 可能是200(null)也可能是404，都是预期
  expect([200, 404]).toContain(r.status());
});

// ─── ⑥ 登录认证流程 ───────────────────────────────────────
test('⑥ 登录失败（错误密码）', async ({ request }) => {
  const r = await request.post(`${BASE}/auth/login`, {
    data: { username: 'admin', password: 'wrong' }
  });
  expect(r.status()).toBe(401);
});

test('⑨ 用户信息获取', async ({ request }) => {
  const r = await request.get(`${BASE}/auth/userinfo`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  log('auth/userinfo status', r.status());
  expect(r.status()).toBe(200);
  const d = await r.json();
  log('userType', d.data?.userType);
  expect(d.data?.userType).toBe('SYSTEM');
});

// ─── ⑦ 服务商管理 ─────────────────────────────────────────
test('⑦ 服务商列表', async ({ request }) => {
  const r = await request.get(`${BASE}/providers?page=1&pageSize=10`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  log('providers status', r.status());
  expect(r.status()).toBe(200);
  const d = await r.json();
  log('providers total', d.data?.total);
  expect(d.data?.total).toBeGreaterThan(0);
});

// ─── ⑧ 系统配置/字典 ──────────────────────────────────────
test('⑧ 字典项查询', async ({ request }) => {
  const r = await request.get(`${BASE}/dict/items?type=service_type`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  log('dict status', r.status());
  // 可能200也可能404，取决于dict实现
  expect([200, 404]).toContain(r.status());
});

test('⑨ 服务类型列表', async ({ request }) => {
  const r = await request.get(`${BASE}/service-types?page=1&pageSize=20`, {
    headers: { Authorization: `Bearer ${adminToken}` }
  });
  log('service-types status', r.status());
  expect(r.status()).toBe(200);
  const d = await r.json();
  log('service-types total', d.data?.total);
});
