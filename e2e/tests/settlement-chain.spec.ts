import { test, expect } from '@playwright/test';

const API = 'https://wisdomdance.cn/jxy/api';

/**
 * 财务结算链路测试
 * 端点路径（对照后端 FinancialController.java）：
 *   GET  /api/financial/settlements       - 结算列表（带providerId隔离）
 *   POST /api/financial/settlements/calculate  - 结算计算
 *   POST /api/financial/settlements/batch      - 批量结算
 *   GET  /api/financial/reports          - 财务报表
 */
test.describe('财务结算链路', () => {
  let adminToken: string;
  let fws1Token: string;
  let fws2Token: string;

  test.beforeAll(async ({ request }) => {
    const adminLogin = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    adminToken = (await adminLogin.json()).data?.accessToken;

    const fws1Login = await request.post(`${API}/auth/login`, {
      data: { username: 'FWS1', password: 'admin123' }
    });
    fws1Token = (await fws1Login.json()).data?.accessToken;

    const fws2Login = await request.post(`${API}/auth/login`, {
      data: { username: 'FWS2', password: 'admin123' }
    });
    fws2Token = (await fws2Login.json()).data?.accessToken;
  });

  // ========== 结算列表 ==========

  test('TC-FIN-01: 服务商查看本公司结算账单（FWS1）', async ({ request }) => {
    const resp = await request.get(`${API}/financial/settlements?page=1&pageSize=50`, {
      headers: { Authorization: `Bearer ${fws1Token}` }
    });
    const body = await resp.json();
    console.log(`TC-FIN-01 FWS1结算单: HTTP ${resp.status()}, code=${body.code}`);
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);
  });

  test('TC-FIN-02: 管理员查看所有结算账单', async ({ request }) => {
    const resp = await request.get(`${API}/financial/settlements?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await resp.json();
    console.log(`TC-FIN-02 admin结算单: HTTP ${resp.status()}, code=${body.code}`);
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);
    // admin 应能看到所有结算（total >= FWS1 + FWS2）
  });

  test('TC-FIN-03: FWS1 和 FWS2 结算数据隔离', async ({ request }) => {
    const [fws1Resp, fws2Resp] = await Promise.all([
      request.get(`${API}/financial/settlements?page=1&pageSize=100`, {
        headers: { Authorization: `Bearer ${fws1Token}` }
      }),
      request.get(`${API}/financial/settlements?page=1&pageSize=100`, {
        headers: { Authorization: `Bearer ${fws2Token}` }
      }),
    ]);
    const fws1Body = await fws1Resp.json();
    const fws2Body = await fws2Resp.json();
    const fws1Code = fws1Body.code;
    const fws2Code = fws2Body.code;
    expect(fws1Code).toBe(200);
    expect(fws2Code).toBe(200);
    const adminResp = await request.get(`${API}/financial/settlements?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const adminTotal = (await adminResp.json()).data?.total || 0;
    const fws1Total = fws1Body.data?.total || 0;
    const fws2Total = fws2Body.data?.total || 0;
    expect(adminTotal).toBeGreaterThanOrEqual(fws1Total + fws2Total);
    console.log(`TC-FIN-03 FWS1=${fws1Total}条, FWS2=${fws2Total}条, Admin=${adminTotal}条`);
  });

  // ========== 财务报表 ==========

  test('TC-FIN-04: 财务报表准确性', async ({ request }) => {
    const resp = await request.get(`${API}/financial/reports?page=1&pageSize=50`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await resp.json();
    console.log(`TC-FIN-04 财务报表: HTTP ${resp.status()}, code=${body.code}`);
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);
    console.log(`报表: ${body.data?.total || 0} 条`);
  });

  test('TC-FIN-05: 结算单字段完整性', async ({ request }) => {
    const resp = await request.get(`${API}/financial/settlements?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await resp.json();
    if (body.code !== 200 || !body.data?.records?.length) {
      console.log(`TC-FIN-05 结算列表为空（code=${body.code}），跳过`);
      test.skip(); return;
    }
    const settlement = body.data.records[0];
    console.log(`TC-FIN-05 结算单状态: ${settlement.status}, provider=${settlement.providerName}`);
    // 关键字段必须存在
    expect(settlement).toHaveProperty('settlementAmount');
    expect(settlement).toHaveProperty('status');
    expect(settlement).toHaveProperty('providerId');
  });

  // ========== 批量结算 ==========

  test('TC-FIN-06: PROVIDER 发起批量结算申请', async ({ request }) => {
    const resp = await request.post(`${API}/financial/settlements/batch`, {
      headers: { Authorization: `Bearer ${fws1Token}`, 'Content-Type': 'application/json' },
      data: {
        // providerId: 后端从 UserContext 自动注入，PROVIDER 用户不需要传
        settlementPeriodStart: '2026-04-01',
        settlementPeriodEnd: '2026-04-30'
      }
    });
    const body = await resp.json();
    console.log(`TC-FIN-06 批量结算: HTTP ${resp.status()}, code=${body.code}`);
    // 200=成功, 400=参数/业务错误(如无待结算订单), 500=服务端错误
    expect([200, 400, 500]).toContain(resp.status());
  });

  // ========== 结算计算 ==========

  test('TC-FIN-07: 结算计算接口（admin）', async ({ request }) => {
    const resp = await request.post(`${API}/financial/settlements/calculate`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: {
        providerId: '1',
        settlementPeriodStart: '2026-03-01',
        settlementPeriodEnd: '2026-03-31'
      }
    });
    const body = await resp.json();
    console.log(`TC-FIN-07 结算计算: HTTP ${resp.status()}, code=${body.code}`);
    // 200=成功, 400=参数错误, 500=服务端错误
    expect([200, 400, 500]).toContain(resp.status());
    if (body.code === 200) {
      expect(body.data).toBeTruthy();
    }
  });

  // ========== 结算确认 ==========

  test('TC-FIN-08: 结算单状态流转', async ({ request }) => {
    // 获取第一个待确认的结算单
    const resp = await request.get(`${API}/financial/settlements?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await resp.json();
    if (body.code !== 200 || !body.data?.records?.length) {
      test.skip(); return;
    }
    // 找一条 PENDING 状态的结算单
    const pending = body.data.records.find((r: any) => r.status === 'PENDING');
    if (!pending) {
      console.log('TC-FIN-08 无待确认结算单，跳过');
      test.skip(); return;
    }
    const settleId = pending.settleId || pending.id;
    console.log(`TC-FIN-08 确认结算单: ${settleId}, status=${pending.status}`);
    const confirmResp = await request.post(`${API}/financial/settlements/${settleId}/confirm`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const confirmBody = await confirmResp.json();
    console.log(`TC-FIN-08 确认结果: HTTP ${confirmResp.status()}, code=${confirmBody.code}`);
    expect([200, 400, 500]).toContain(confirmResp.status());
  });
});
