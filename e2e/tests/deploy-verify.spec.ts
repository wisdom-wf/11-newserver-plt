import { test, expect } from '@playwright/test';

const BASE = 'https://wisdomdance.cn/jxy';

async function loginAndGetToken(request) {
  const res = await request.post(`${BASE}/api/auth/login`, {
    data: { username: 'admin', password: 'admin123' }
  });
  const body = await res.json();
  return body.data?.accessToken || '';
}

test.describe('部署验证 - API健康检查', () => {

  test('登录接口', async ({ request }) => {
    const res = await request.post(`${BASE}/api/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.code).toBe(200);
    expect(body.data.accessToken).toBeTruthy();
    console.log('✅ 登录成功, token长度:', body.data.accessToken.length);
  });

  test('首页驾驶舱概览', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/cockpit/overview`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    console.log('✅ 驾驶舱概览:', JSON.stringify(body.data).substring(0, 200));
  });

  test('订单统计', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/orders/statistics`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    console.log('✅ 订单统计:', JSON.stringify(body.data).substring(0, 200));
  });

  test('质检统计', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/quality-check/statistics`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    console.log('✅ 质检统计正常');
  });

  test('财务统计', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/statistics/financial`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    console.log('✅ 财务统计正常');
  });

  test('老人统计', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/statistics/elder`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    console.log('✅ 老人统计正常');
  });

  test('服务商列表', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/providers?page=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    console.log('✅ 服务商列表:', body.data?.total ?? 'N/A', '条');
  });

  test('订单列表', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/orders?page=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    console.log('✅ 订单列表:', body.data?.total ?? 'N/A', '条');
  });

  test('服务人员列表', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/staff?page=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    console.log('✅ 服务人员列表:', body.data?.total ?? 'N/A', '条');
  });

  test('老人列表', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/elders?page=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    console.log('✅ 老人列表:', body.data?.total ?? 'N/A', '条');
  });

  test('评价列表', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/evaluations?page=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    console.log('✅ 评价列表:', body.data?.total ?? 'N/A', '条');
  });

  test('结算列表', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/settlements?page=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    console.log('✅ 结算列表:', body.data?.total ?? 'N/A', '条');
  });

  test('菜单权限接口', async ({ request }) => {
    const token = await loginAndGetToken(request);
    const res = await request.get(`${BASE}/api/system/menus`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    console.log('✅ 菜单权限:', body.data?.length ?? 'N/A', '个菜单');
  });

  test('公开评价接口(无token)', async ({ request }) => {
    const res = await request.get(`${BASE}/public/survey?token=FAKE_TOKEN_TEST`);
    // 应该返回错误但不是500
    const body = await res.json();
    console.log('公开评价响应:', body.code, body.message?.substring(0, 60));
    // 期望是业务错误(400/404)而不是服务器崩溃(500)
    expect(body.code).not.toBe(500);
  });

  test('前端页面可访问', async ({ page }) => {
    await page.goto(BASE + '/');
    await page.waitForLoadState('networkidle');
    const title = await page.title();
    console.log('✅ 前端页面标题:', title);
    expect(title).toContain('智慧');
  });
});
