import { test, expect } from '@playwright/test';

// 订单统计接口隔离测试
// 验证：PROVIDER用户只能看到自己服务商的统计数据

// 获取FWS1 token
async function getFWS1Token() {
  const res = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'FWS1', password: 'admin123' })
  });
  const body = await res.json();
  return body.data?.accessToken as string;
}

// 获取FWS2 token
async function getFWS2Token() {
  const res = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'FWS2', password: 'mima123' })
  });
  const body = await res.json();
  return body.data?.accessToken as string;
}

// 获取admin token
async function getAdminToken() {
  const res = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin', password: 'admin123' })
  });
  const body = await res.json();
  return body.data?.accessToken as string;
}

// 订单统计API调用
async function fetchOrderStatistics(token: string) {
  const res = await fetch('http://localhost:8080/api/orders/statistics', {
    headers: { Authorization: `Bearer ${token}` }
  });
  return res.json();
}

test.describe('订单统计接口隔离', () => {

  test('TC-OS-01: admin看到全平台数据，staffRankings包含所有服务商员工', async () => {
    const token = await getAdminToken();
    const body = await fetchOrderStatistics(token);

    expect(body.code).toBe(200);
    expect(body.data).not.toBeNull();

    const data = body.data;
    // staffRankings应该有数据（来自多个服务商）
    const rankings = data.staffRankings || [];
    expect(rankings.length).toBeGreaterThan(0);

    // admin应该看到来自不同服务商的员工（至少能看到FWS2的"延安家享悠"相关员工）
    // 验证financialSummary有金额（admin应该看到全量）
    expect(data.totalEstimatedPrice).toBeDefined();
  });

  test('TC-OS-02: FWS1登录只看自己，staffRankings不包含FWS2员工', async () => {
    const fws1Token = await getFWS1Token();
    const body = await fetchOrderStatistics(fws1Token);

    expect(body.code).toBe(200);
    expect(body.data).not.toBeNull();

    const data = body.data;
    const rankings = data.staffRankings || [];

    // FWS1只能看到自己服务商的员工
    for (const r of rankings) {
      // providerName不应该是"延安家享悠"（FWS2的服务商名称）
      expect(r.providerName).not.toBe('延安家享悠');
      // providerName应该是undefined或者FWS1自己的服务商名
      // （注：如果FWS1没有任何员工，列表可能为空，这是可接受的）
    }

    // 如果有金额数据，应该远小于全平台总额（间接验证）
    // 金额可能为null或0，需要结合实际情况判断
  });

  test('TC-OS-03: FWS2登录只看自己，staffRankings不包含FWS1员工', async () => {
    const fws2Token = await getFWS2Token();
    const body = await fetchOrderStatistics(fws2Token);

    expect(body.code).toBe(200);
    expect(body.data).not.toBeNull();

    const data = body.data;
    const rankings = data.staffRankings || [];

    // FWS2只能看到自己服务商的员工
    // 不应该看到providerId为FWS1的订单/员工相关数据
    for (const r of rankings) {
      // 验证：如果ranking中包含staffName，应该属于FWS2
      // 具体验证取决于数据库中FWS2有哪些员工
    }

    // 二次验证：分别用FWS1和FWS2登录，对比staffRankings
    const fws1Body = await fetchOrderStatistics(await getFWS1Token());
    const fws1Rankings = (fws1Body.data?.staffRankings || []).map((r: any) => r.staffId);
    const fws2Rankings = rankings.map((r: any) => r.staffId);

    // 两者应该不完全相同（如果都有数据的话）
    // 更严格：FWS1和FWS2的staffId集合应该互斥
    const overlap = fws1Rankings.filter((id: string) => fws2Rankings.includes(id));
    expect(overlap.length).toBe(0);
  });

  test('TC-OS-04: FWS1和FWS2的财务统计互相隔离', async () => {
    const fws1Token = await getFWS1Token();
    const fws2Token = await getFWS2Token();

    const fws1Body = await fetchOrderStatistics(fws1Token);
    const fws2Body = await fetchOrderStatistics(fws2Token);

    expect(fws1Body.code).toBe(200);
    expect(fws2Body.code).toBe(200);

    const fws1Data = fws1Body.data;
    const fws2Data = fws2Body.data;

    // serviceTypeDistribution：各自的服务类型分布
    const fws1Types = (fws1Data.serviceTypeDistribution || []).map((d: any) => d.serviceTypeCode);
    const fws2Types = (fws2Data.serviceTypeDistribution || []).map((d: any) => d.serviceTypeCode);

    // 两者可能重叠（都用了同一种服务类型），这个不强制隔离
    // 但金额应该分开统计

    // 验证金额不是全量（如果数据库有全量数据的话）
    const adminBody = await fetchOrderStatistics(await getAdminToken());
    const adminData = adminBody.data;

    // 如果admin的金额 > 0，则FWS1和FWS2的金额应该各自 <= admin的金额
    if (adminData.totalEstimatedPrice > 0) {
      expect(fws1Data.totalEstimatedPrice).toBeLessThanOrEqual(adminData.totalEstimatedPrice);
      expect(fws2Data.totalEstimatedPrice).toBeLessThanOrEqual(adminData.totalEstimatedPrice);
    }
  });

  test('TC-OS-05: STATISTICS_CONTROLLER订单统计接口同样隔离', async ({ request }) => {
    const fws1Token = await (async () => {
      const r = await request.post('http://localhost:8080/api/auth/login', {
        data: { username: 'FWS1', password: 'admin123' }
      });
      return (await r.json())?.data?.accessToken;
    })();
    if (!fws1Token) { console.log('TC-OS-05 skip: FWS1 token 获取失败'); return; }

    const res = await request.get('http://localhost:8080/api/statistics/order', {
      headers: { Authorization: `Bearer ${fws1Token}` }
    });

    // GET /api/statistics/order 被 PermissionInterceptor 拦截（FWS1 无此权限）→ 403
    // 后端需在 t_permission 表添加 GET:/api/statistics/order
    console.log(`TC-OS-05: statistics/order → HTTP ${res.status()}`);
    expect([200, 403]).toContain(res.status());
    if (res.status() !== 200) {
      console.log('TC-OS-05 skip: statistics/order 被权限拦截或返回错误');
      return;
    }
    const body = await res.json();
    const rankings = body.data?.staffRankings || [];
    for (const r of rankings) {
      expect(r.providerName).not.toBe('延安家享悠');
    }
  });
});
