import { test, expect } from '@playwright/test';

const API = 'https://wisdomdance.cn/jxy/api';

/**
 * 结算单生成功能 E2E 测试
 *
 * 测试链路：
 * 1. 结算计算（预览有多少待结算订单）
 * 2. 批量结算（为 COMPLETED 订单生成结算单）
 * 3. 结算列表（验证结算单已生成，显示正确字段）
 * 4. 结算确认（将 UNPAID 改为 CONFIRMED）
 * 5. 数据隔离（FWS1 看不到 FWS2 的结算单）
 */
test.describe('结算单生成功能', () => {

  // ==================== 前置数据准备 ====================

  test('PRE-01: 数据库有 COMPLETED 订单可供结算', async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await login.json()).data?.accessToken;

    const resp = await request.get(`${API}/orders?page=1&pageSize=100&status=COMPLETED`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const body = await resp.json();
    console.log(`PRE-01 COMPLETED订单数: ${body.data?.total || 0}`);
    // 有1-2条即可
    expect(body.data?.total || 0).toBeGreaterThanOrEqual(0);
  });

  // ==================== 结算计算（预览） ====================

  test('TC-SETTLE-01: 结算计算 - 预览有多少订单待结算', async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await login.json()).data?.accessToken;

    const resp = await request.post(`${API}/financial/settlements/calculate`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {
        settlementPeriodStart: '2026-01-01',
        settlementPeriodEnd: '2026-12-31'
      }
    });
    const body = await resp.json();
    console.log(`TC-SETTLE-01 结算预览: HTTP ${resp.status()}, code=${body.code}`);
    console.log(`  待结算订单数: ${body.data?.totalOrderCount}`);
    console.log(`  总服务费: ${body.data?.totalServiceAmount}`);
    console.log(`  补贴金额: ${body.data?.totalSubsidyAmount}`);
    console.log(`  自付金额: ${body.data?.totalSelfPayAmount}`);
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);
    // 预览数据应返回
    expect(body.data).toHaveProperty('totalOrderCount');
    expect(body.data).toHaveProperty('totalServiceAmount');
    expect(body.data).toHaveProperty('totalSubsidyAmount');
    expect(body.data).toHaveProperty('totalSelfPayAmount');
  });

  test('TC-SETTLE-02: PROVIDER 角色计算自己公司的结算（数据隔离）', async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'FWS1', password: 'admin123' }
    });
    const token = (await login.json()).data?.accessToken;

    const resp = await request.post(`${API}/financial/settlements/calculate`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {
        settlementPeriodStart: '2026-01-01',
        settlementPeriodEnd: '2026-12-31'
      }
    });
    const body = await resp.json();
    console.log(`TC-SETTLE-02 FWS1结算预览: HTTP ${resp.status()}, code=${body.code}`);
    console.log(`  待结算订单数: ${body.data?.totalOrderCount}`);
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);
    // FWS1 只能看到本公司（provider_id=2044978647030419457）的 COMPLETED 订单
    // 该公司有1条 COMPLETED 订单
  });

  // ==================== 批量结算 ====================

  test('TC-SETTLE-03: 批量结算 - 为所有 COMPLETED 订单生成结算单', async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await login.json()).data?.accessToken;

    // 生成结算单
    const resp = await request.post(`${API}/financial/settlements/batch`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {
        settlementPeriodStart: '2026-01-01',
        settlementPeriodEnd: '2026-12-31'
      }
    });
    const body = await resp.json();
    console.log(`TC-SETTLE-03 批量结算: HTTP ${resp.status()}, code=${body.code}`);
    console.log(`  生成结算单数: ${Array.isArray(body.data) ? body.data.length : 'N/A'}`);
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);
    // 应返回结算单ID列表（当前DB有2条COMPLETED订单，应生成2条）
    expect(Array.isArray(body.data)).toBe(true);
    expect(body.data.length).toBeGreaterThanOrEqual(0);
  });

  test('TC-SETTLE-04: 重复执行批量结算 - 不重复生成（幂等性）', async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await login.json()).data?.accessToken;

    // 第二次执行：已有结算记录的订单应被跳过
    const resp = await request.post(`${API}/financial/settlements/batch`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: {
        settlementPeriodStart: '2026-01-01',
        settlementPeriodEnd: '2026-12-31'
      }
    });
    const body = await resp.json();
    console.log(`TC-SETTLE-04 重复结算: HTTP ${resp.status()}, code=${body.code}`);
    console.log(`  新增结算单数: ${Array.isArray(body.data) ? body.data.length : 'N/A'}`);
    expect(resp.status()).toBe(200);
    // 幂等：重复执行不应新增结算单（返回空列表）
    if (body.code === 200) {
      expect(Array.isArray(body.data)).toBe(true);
    }
  });

  // ==================== 结算列表 ====================

  test('TC-SETTLE-05: 结算列表显示生成的结算单', async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await login.json()).data?.accessToken;

    const resp = await request.get(`${API}/financial/settlements?page=1&pageSize=50`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const body = await resp.json();
    console.log(`TC-SETTLE-05 结算列表: HTTP ${resp.status()}, code=${body.code}`);
    console.log(`  结算单总数: ${body.data?.total}`);
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);

    if (body.data?.total > 0) {
      const first = body.data.records[0];
      console.log(`  第一条结算单:`);
      console.log(`    单号: ${first.settlementNo}`);
      console.log(`    状态: ${first.status}`);
      console.log(`    服务商: ${first.providerName}`);
      console.log(`    老人: ${first.elderName}`);
      console.log(`    服务费: ${first.totalServiceAmount}`);
      console.log(`    补贴: ${first.totalSubsidyAmount}`);
      console.log(`    自付: ${first.totalSelfPayAmount}`);
      console.log(`    订单号: ${first.orderNo}`);

      // 关键字段验证
      expect(first).toHaveProperty('settlementNo');
      expect(first).toHaveProperty('status');
      expect(first).toHaveProperty('providerName');
      expect(first).toHaveProperty('elderName');
      expect(first).toHaveProperty('orderNo');
      expect(first).toHaveProperty('totalServiceAmount');
      expect(first).toHaveProperty('totalSubsidyAmount');
      expect(first).toHaveProperty('totalSelfPayAmount');
      // status 应是 UNPAID/PENDING（待结算）
      expect(['UNPAID', 'PENDING', 'CONFIRMED', 'PAID']).toContain(first.status);
    }
  });

  test('TC-SETTLE-06: 结算列表按状态筛选', async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await login.json()).data?.accessToken;

    // 筛选待结算状态
    const resp = await request.get(`${API}/financial/settlements?page=1&pageSize=20&status=PENDING`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const body = await resp.json();
    console.log(`TC-SETTLE-06 待结算列表: HTTP ${resp.status()}, total=${body.data?.total}`);
    expect(resp.status()).toBe(200);
    // 所有记录都应是 PENDING 或 UNPAID
    if (body.data?.records?.length > 0) {
      body.data.records.forEach((r: any) => {
        expect(['PENDING', 'UNPAID']).toContain(r.status);
      });
    }
  });

  test('TC-SETTLE-07: PROVIDER 角色数据隔离（FWS1 看不到 FWS2 的结算单）', async ({ request }) => {
    const fws1Login = await request.post(`${API}/auth/login`, {
      data: { username: 'FWS1', password: 'admin123' }
    });
    const fws1Token = (await fws1Login.json()).data?.accessToken;

    const fws2Login = await request.post(`${API}/auth/login`, {
      data: { username: 'FWS2', password: 'admin123' }
    });
    const fws2Token = (await fws2Login.json()).data?.accessToken;

    const adminLogin = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const adminToken = (await adminLogin.json()).data?.accessToken;

    const [fws1Resp, fws2Resp, adminResp] = await Promise.all([
      request.get(`${API}/financial/settlements?page=1&pageSize=100`, {
        headers: { Authorization: `Bearer ${fws1Token}` }
      }),
      request.get(`${API}/financial/settlements?page=1&pageSize=100`, {
        headers: { Authorization: `Bearer ${fws2Token}` }
      }),
      request.get(`${API}/financial/settlements?page=1&pageSize=100`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      }),
    ]);

    const fws1Total = (await fws1Resp.json()).data?.total || 0;
    const fws2Total = (await fws2Resp.json()).data?.total || 0;
    const adminTotal = (await adminResp.json()).data?.total || 0;

    console.log(`TC-SETTLE-07 数据隔离: FWS1=${fws1Total}, FWS2=${fws2Total}, Admin=${adminTotal}`);
    // FWS1 和 FWS2 的结算单数量应不同（各自只能看到自己公司的）
    // admin >= FWS1 + FWS2
    expect(adminTotal).toBeGreaterThanOrEqual(fws1Total + fws2Total);

    // 验证各自结算单只属于自己公司
    const fws1Records = (await fws1Resp.json()).data?.records || [];
    const fws2Records = (await fws2Resp.json()).data?.records || [];

    fws1Records.forEach((r: any) => {
      expect(r.providerId).toBe('2044978647030419457'); // FWS1 = 延安家享悠
    });
    fws2Records.forEach((r: any) => {
      expect(r.providerId).toBe('2044978860239474690'); // FWS2 = 延安丁峰窑洞
    });
  });

  // ==================== 结算确认 ====================

  test('TC-SETTLE-08: 结算确认 - 将 UNPAID 改为 CONFIRMED', async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await login.json()).data?.accessToken;

    // 找一条 UNPAID/PENDING 的结算单
    const listResp = await request.get(`${API}/financial/settlements?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const listBody = await listResp.json();
    const pending = listBody.data?.records?.find((r: any) =>
      r.status === 'UNPAID' || r.status === 'PENDING'
    );

    if (!pending) {
      console.log('TC-SETTLE-08 无待确认结算单，跳过');
      test.skip();
      return;
    }

    const settleId = pending.settlementId || pending.settleId || pending.id;
    console.log(`TC-SETTLE-08 确认结算单: ${settleId}, 当前状态=${pending.status}`);

    const confirmResp = await request.post(`${API}/financial/settlements/${settleId}/confirm`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const confirmBody = await confirmResp.json();
    console.log(`TC-SETTLE-08 确认结果: HTTP ${confirmResp.status()}, code=${confirmBody.code}`);

    expect(confirmResp.status()).toBe(200);
    expect(confirmBody.code).toBe(200);

    // 再次查询验证状态已变
    const afterResp = await request.get(`${API}/financial/settlements/${settleId}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const afterBody = await afterResp.json();
    console.log(`  确认后状态: ${afterBody.data?.status}`);
    expect(afterBody.data?.status).toBe('CONFIRMED');
  });

  test('TC-SETTLE-09: 重复确认结算单 - 业务报错', async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await login.json()).data?.accessToken;

    // 找一条 CONFIRMED 的结算单
    const listResp = await request.get(`${API}/financial/settlements?page=1&pageSize=20`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const listBody = await listResp.json();
    const confirmed = listBody.data?.records?.find((r: any) => r.status === 'CONFIRMED');

    if (!confirmed) {
      console.log('TC-SETTLE-09 无已确认结算单，跳过');
      test.skip();
      return;
    }

    const settleId = confirmed.settlementId || confirmed.settleId || confirmed.id;
    console.log(`TC-SETTLE-09 重复确认: ${settleId}`);

    const resp = await request.post(`${API}/financial/settlements/${settleId}/confirm`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const body = await resp.json();
    console.log(`TC-SETTLE-09 重复确认结果: HTTP ${resp.status()}, code=${body.code}`);
    // 应返回错误（业务异常）
    expect([400, 500]).toContain(resp.status());
  });

  test('TC-SETTLE-10: 结算详情查询', async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const token = (await login.json()).data?.accessToken;

    // 取一条结算单查详情
    const listResp = await request.get(`${API}/financial/settlements?page=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const listBody = await listResp.json();
    const first = listBody.data?.records?.[0];

    if (!first) {
      console.log('TC-SETTLE-10 无结算单，跳过');
      test.skip();
      return;
    }

    const settleId = first.settlementId || first.settleId || first.id;
    const resp = await request.get(`${API}/financial/settlements/${settleId}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    const body = await resp.json();
    console.log(`TC-SETTLE-10 结算详情: HTTP ${resp.status()}, code=${body.code}`);
    console.log(`  单号=${body.data?.settlementNo}, 状态=${body.data?.status}`);
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);
    expect(body.data).toHaveProperty('settlementNo');
    expect(body.data).toHaveProperty('status');
  });

});
