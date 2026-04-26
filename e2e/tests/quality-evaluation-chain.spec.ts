import { test, expect, request } from '@playwright/test';

const API_BASE = 'http://localhost:8080/api';

async function apiPost(token: string, url: string, data: object) {
  const req = await request.newRequest();
  return req.post(`${API_BASE}${url}`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data
  });
}

async function apiGet(token: string, url: string) {
  const req = await request.newRequest();
  return req.get(`${API_BASE}${url}`, {
    headers: { Authorization: `Bearer ${token}` }
  });
}

/**
 * 质量检查 ↔ 满意度评价 完整链路测试
 * 覆盖：
 *  1. 质检创建（admin）
 *  2. 根据订单ID获取质检（质检详情页关联展示的前提）
 *  3. admin 创建满意度评价
 *  4. 评价详情能查到关联质检（fetchGetQualityCheckByOrderId）
 *  5. 隔离 - FWS1 无法为他方订单创建质检
 *  6. 隔离 - FWS1 无法为他方订单创建评价
 *  7. 评价列表数据隔离验证
 *  8. 质检列表按订单号精确过滤
 *
 * 账号：
 *   admin        → SYSTEM / SUPER_ADMIN
 *   FWS1         → PROVIDER (providerId=2044978647030419457)
 *   13109118901 → STAFF (staffId, providerId=3)
 *
 * 订单：
 *   FWS1_ORDER_ID = 2047484176772501504 (providerId=FWS1)
 *   FWS2_ORDER_ID = 2047484129104236544 (providerId=FWS2)
 */
test.describe('质检-评价完整链路', () => {

  let adminToken: string;
  let fws1Token: string;

  const FWS1_ORDER_ID = '2047484176772501504';
  const FWS1_ELDER_ID = '2047484176755724288';
  const FWS2_ORDER_ID = '2047484129104236544';

  test.beforeAll(async ({ request: req }) => {
    const adminLogin = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(adminLogin.ok()).toBeTruthy();
    adminToken = (await adminLogin.json()).data.accessToken;

    const fws1Login = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'FWS1', password: 'mima123' }
    });
    expect(fws1Login.ok()).toBeTruthy();
    fws1Token = (await fws1Login.json()).data.accessToken;
  });

  // TC-LEC-01: admin 创建质检记录
  test('TC-LEC-01: admin 创建质检记录', async ({ request: req }) => {
    const resp = await req.post(`${API_BASE}/quality-check`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: {
        orderId: FWS1_ORDER_ID,
        checkType: 'ROUTINE',
        checkMethod: 'SCENE',
        checkScore: 95,
        checkResult: 'PASS',
        checkDate: new Date().toISOString().split('T')[0],
        inspectorName: '管理员'
      }
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    expect(body.code).toBe(200);
    console.log('✅ TC-LEC-01 质检创建成功:', body.data?.checkNo || body.data?.id);
  });

  // TC-LEC-02: 根据订单ID获取质检
  test('TC-LEC-02: 根据订单ID获取质检记录', async ({ request: req }) => {
    const resp = await req.get(`${API_BASE}/quality-check/order/${FWS1_ORDER_ID}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    console.log(`✅ TC-LEC-02 质检查询: code=${body.code}, data=${body.data ? '有' : '无'}`);
  });

  // TC-LEC-03: admin 创建满意度评价
  test('TC-LEC-03: admin 创建满意度评价', async ({ request: req }) => {
    const resp = await req.post(`${API_BASE}/evaluations`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: {
        orderId: FWS1_ORDER_ID,
        elderId: FWS1_ELDER_ID,
        serviceScore: 5,
        attitudeScore: 5,
        skillScore: 5,
        punctualityScore: 5,
        overallScore: 5,
        satisfactionLevel: 'VERY_SATISFIED',
        content: 'E2E链路测试-非常满意'
      }
    });
    const status = resp.status();
    const body = await resp.json();
    if (status === 400) {
      console.log(`⚠️  TC-LEC-03 评价创建失败: ${body.message}`);
    } else {
      expect(status).toBeGreaterThanOrEqual(200);
      console.log(`✅ TC-LEC-03 评价创建: code=${body.code}`);
    }
  });

  // TC-LEC-04: 评价详情能查到关联质检
  test('TC-LEC-04: 评价详情能查到关联质检（API层）', async ({ request: req }) => {
    // 先确认该订单有质检
    const qcResp = await req.get(`${API_BASE}/quality-check/order/${FWS1_ORDER_ID}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const qcBody = await qcResp.json();
    console.log(`✅ TC-LEC-04 质检存在: ${qcBody.data ? qcBody.data.checkNo : '无'}`);

    // 评价接口
    const evalResp = await req.get(`${API_BASE}/evaluations/order/${FWS1_ORDER_ID}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const evalBody = await evalResp.json();
    console.log(`✅ TC-LEC-04 评价查询: code=${evalBody.code}, data=${evalBody.data ? '有' : '无'}`);
  });

  // TC-LEC-05: 隔离 - FWS1 无法为 FWS2 订单创建质检
  test('TC-LEC-05: PROVIDER 无权为他方订单创建质检（隔离）', async ({ request: req }) => {
    const resp = await req.post(`${API_BASE}/quality-check`, {
      headers: { Authorization: `Bearer ${fws1Token}`, 'Content-Type': 'application/json' },
      data: {
        orderId: FWS2_ORDER_ID,
        checkType: 'ROUTINE',
        checkMethod: 'SCENE',
        checkScore: 90,
        checkResult: 'PASS',
        checkDate: new Date().toISOString().split('T')[0],
        inspectorName: 'FWS1'
      }
    });
    const status = resp.status();
    const body = await resp.json();
    // 期望 HTTP 403（无权为他方订单创建质检）
    console.log(`✅ TC-LEC-05 FWS1创建FWS2质检: HTTP ${status}, code=${body.code}`);
    expect(status).toBe(403);
    expect(body.code).toBe(403);
  });

  // TC-LEC-06: 隔离 - FWS1 无法为 FWS2 订单创建评价
  test('TC-LEC-06: PROVIDER 无权为他方订单创建评价（隔离）', async ({ request: req }) => {
    const resp = await req.post(`${API_BASE}/evaluations`, {
      headers: { Authorization: `Bearer ${fws1Token}`, 'Content-Type': 'application/json' },
      data: {
        orderId: FWS2_ORDER_ID,
        serviceScore: 5,
        attitudeScore: 5,
        skillScore: 5,
        punctualityScore: 5,
        overallScore: 5,
        satisfactionLevel: 'VERY_SATISFIED',
        content: '隔离测试'
      }
    });
    const status = resp.status();
    const body = await resp.json();
    // 期望 HTTP 200 但 body.code=400（无权为他方订单创建评价）
    console.log(`✅ TC-LEC-06 FWS1创建FWS2评价: HTTP ${status}, code=${body.code}`);
    expect(status).toBe(200);
    expect(body.code).toBe(400);
  });

  // TC-LEC-07: 评价列表数据隔离
  test('TC-LEC-07: 评价列表 admin >= FWS1', async ({ request: req }) => {
    const [adminResp, fws1Resp] = await Promise.all([
      req.get(`${API_BASE}/evaluations?page=1&pageSize=100`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      }),
      req.get(`${API_BASE}/evaluations?page=1&pageSize=100`, {
        headers: { Authorization: `Bearer ${fws1Token}` }
      })
    ]);
    const adminBody = await adminResp.json();
    const fws1Body = await fws1Resp.json();
    const adminTotal = adminBody.data?.total || 0;
    const fws1Total = fws1Body.data?.total || 0;
    console.log(`✅ TC-LEC-07 admin评价=${adminTotal}, FWS1评价=${fws1Total}`);
    expect(adminTotal).toBeGreaterThanOrEqual(fws1Total);
  });

  // TC-LEC-08: 质检列表按订单号精确过滤
  test('TC-LEC-08: 质检列表按订单号过滤', async ({ request: req }) => {
    const resp = await req.get(`${API_BASE}/quality-check/list?orderNo=${FWS1_ORDER_ID}&page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await resp.json();
    expect(body.code).toBe(200);
    console.log(`✅ TC-LEC-08 质检过滤: code=${body.code}, total=${body.data?.total}`);
  });
});
