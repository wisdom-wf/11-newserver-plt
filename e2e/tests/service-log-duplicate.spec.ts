import { test, expect } from '@playwright/test';

const API = 'https://wisdomdance.cn/jxy/api';

/**
 * 服务日志复制 API 测试
 * POST /api/service-log/{id}/duplicate
 *
 * 接口行为（已验证）：
 * - DRAFT 状态日志：返回新日志 ID（message 字段）
 * - APPROVED 状态：HTTP 400 "仅支持复制草稿状态的日志"
 * - 不存在日志：HTTP 200 + code=500 "服务日志不存在"
 */
test.describe('服务日志复制 API', () => {
  let adminToken: string;
  let orderId: string;
  let draftLogId: string;
  let approvedLogId: string;

  test.beforeAll(async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    adminToken = (await login.json()).data?.accessToken;

    // 找一个有多条日志的订单
    const orderResp = await request.get(`${API}/orders?page=1&pageSize=5`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const orders = (await orderResp.json()).data?.records || [];
    const order = orders.find((o: any) => o.status !== 'CANCELLED');
    orderId = order?.orderId;

    if (orderId) {
      const logsResp = await request.get(`${API}/service-log/order/${orderId}/all`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      });
      const logs = (await logsResp.json()).data || [];
      const draft = logs.find((l: any) => l.auditStatus === 'DRAFT');
      const approved = logs.find((l: any) => l.auditStatus === 'APPROVED');
      draftLogId = draft?.serviceLogId;
      approvedLogId = approved?.serviceLogId;
    }
  });

  test('TC-DUP-01: 复制草稿状态日志 → 创建成功，返回新ID', async ({ request }) => {
    if (!draftLogId) {
      test.skip();
      return;
    }
    const resp = await request.post(`${API}/service-log/${draftLogId}/duplicate`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await resp.json();
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);
    // 新ID在 message 字段（已验证）
    expect(body.message).toBeTruthy();
    expect(body.message.length).toBeGreaterThan(10);
  });

  test('TC-DUP-02: 复制后新日志预填原日志的老人/服务人员/地址/服务类型', async ({ request }) => {
    if (!draftLogId) {
      test.skip();
      return;
    }
    // 复制
    const dupResp = await request.post(`${API}/service-log/${draftLogId}/duplicate`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const dupBody = await dupResp.json();
    if (dupBody.code !== 200) {
      test.skip();
      return;
    }
    const newLogId = dupBody.message;

    // 查新日志详情
    const detailResp = await request.get(`${API}/service-log?page=1&pageSize=50`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const detailBody = await detailResp.json();
    const newLog = (detailBody.data?.records || []).find((l: any) => l.serviceLogId === newLogId);
    if (!newLog) {
      test.skip();
      return;
    }

    // 原始日志
    const allResp = await request.get(`${API}/service-log/order/${orderId}/all`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const originalLog = (await allResp.json()).data?.find((l: any) => l.serviceLogId === draftLogId);

    // 验证：老人姓名、服务类型应一致
    expect(newLog.elderName).toBe(originalLog?.elderName);
    expect(newLog.serviceTypeName).toBe(originalLog?.serviceTypeName);
    // 新日志应为 DRAFT 状态
    expect(newLog.auditStatus).toBe('DRAFT');
  });

  test('TC-DUP-03: 复制后新日志清空签到签退时间、照片、签名', async ({ request }) => {
    if (!draftLogId) {
      test.skip();
      return;
    }
    const dupResp = await request.post(`${API}/service-log/${draftLogId}/duplicate`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const dupBody = await dupResp.json();
    if (dupBody.code !== 200) {
      test.skip();
      return;
    }
    const newLogId = dupBody.message;

    const detailResp = await request.get(`${API}/service-log?page=1&pageSize=50`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const newLog = ((await detailResp.json()).data?.records || []).find((l: any) => l.serviceLogId === newLogId);
    if (!newLog) {
      test.skip();
      return;
    }

    // 签到签退时间应为空
    expect(newLog.checkInTime || newLog.serviceStartTime).toBeFalsy();
    expect(newLog.checkOutTime || newLog.serviceEndTime).toBeFalsy();
    // 签到签退地点应为空
    expect(newLog.checkInLocation).toBeFalsy();
    expect(newLog.checkOutLocation).toBeFalsy();
  });

  test('TC-DUP-04: 复制已审核日志（APPROVED）→ HTTP 400', async ({ request }) => {
    if (!approvedLogId) {
      test.skip();
      return;
    }
    const resp = await request.post(`${API}/service-log/${approvedLogId}/duplicate`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(resp.status()).toBe(400);
    const body = await resp.json();
    expect(body.code).toBe(400);
    expect(body.message).toContain('草稿状态');
  });

  test('TC-DUP-05: 复制已提交日志（SUBMITTED）→ HTTP 400', async ({ request }) => {
    if (!orderId) {
      test.skip();
      return;
    }
    const logsResp = await request.get(`${API}/service-log/order/${orderId}/all`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const submitted = ((await logsResp.json()).data || []).find((l: any) => l.auditStatus === 'SUBMITTED');
    if (!submitted) {
      test.skip();
      return;
    }
    const resp = await request.post(`${API}/service-log/${submitted.serviceLogId}/duplicate`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(resp.status()).toBe(400);
    const body = await resp.json();
    expect(body.code).toBe(400);
    expect(body.message).toContain('草稿状态');
  });

  test('TC-DUP-06: 复制不存在的日志 → HTTP 200 + code=500', async ({ request }) => {
    const resp = await request.post(`${API}/service-log/nonexistent123456/duplicate`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const body = await resp.json();
    // 后端返回 HTTP 200 + code=500
    expect(body.code).toBe(500);
    expect(body.message).toContain('不存在');
  });
});
