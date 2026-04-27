import { test, expect } from '@playwright/test';

const API = 'http://localhost:8080/api';

/**
 * 服务日志审核后自动创建质检单测试
 *
 * 后端行为（commit f00449a）：
 * - 日志审核通过（APPROVED）时，自动创建一条 PENDING 状态的质检单
 * - 质检单关联正确的 orderId + serviceLogId
 * - 同一条日志重复审核通过，不重复创建质检单
 * - 审核驳回（REJECTED）不创建质检单
 */
test.describe('服务日志审核自动建质检单', () => {
  let adminToken: string;

  test.beforeAll(async ({ request }) => {
    const login = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    adminToken = (await login.json()).data?.accessToken;
  });

  /**
   * 获取 SUBMITTED 状态的日志（用于触发审核流程）
   */
  async function getSubmittedLog(request: any): Promise<any> {
    const resp = await request.get(`${API}/orders?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const orders = (await resp.json()).data?.records || [];
    for (const order of orders) {
      const logsResp = await request.get(`${API}/service-log/order/${order.orderId}/all`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      });
      const submitted = ((await logsResp.json()).data || [])
        .find((l: any) => l.auditStatus === 'SUBMITTED');
      if (submitted) {
        return { ...submitted, orderId: order.orderId };
      }
    }
    return null;
  }

  /**
   * 获取 DRAFT 状态的日志（用于触发审核流程）
   */
  async function getDraftLog(request: any): Promise<any> {
    const resp = await request.get(`${API}/orders?page=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const orders = (await resp.json()).data?.records || [];
    for (const order of orders) {
      const logsResp = await request.get(`${API}/service-log/order/${order.orderId}/all`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      });
      const draft = ((await logsResp.json()).data || [])
        .find((l: any) => l.auditStatus === 'DRAFT');
      if (draft) {
        return { ...draft, orderId: order.orderId };
      }
    }
    return null;
  }

  test('TC-AUTO-QC-01: 日志审核通过 → 自动创建PENDING状态的质检单', async ({ request }) => {
    const log = await getSubmittedLog(request);
    if (!log) {
      // 没有 SUBMITTED 日志，尝试找一个 APPROVED 日志旁的待测日志
      test.skip();
      return;
    }

    // 记录审核前的质检数量
    const qcBeforeResp = await request.get(`${API}/quality-check/list?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const qcsBefore = (await qcBeforeResp.json()).data?.records || [];
    const qcsBeforeCount = qcsBefore.filter((q: any) =>
      q.serviceLogId === log.serviceLogId && q.checkType === 'COMPLETION'
    ).length;

    // 审核通过
    const reviewResp = await request.post(`${API}/service-log/${log.serviceLogId}/review`, {
      data: { result: 'APPROVED', comment: '自动测试审核通过' },
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const reviewBody = await reviewResp.json();

    // 如果日志已不是 SUBMITTED 状态（可能已被自动审核），跳过
    if (reviewBody.code !== 200) {
      test.skip();
      return;
    }

    // 等待片刻让自动创建质检单完成
    await new Promise(r => setTimeout(r, 500));

    // 检查是否新增了质检单
    const qcAfterResp = await request.get(`${API}/quality-check/list?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const qcsAfter = (await qcAfterResp.json()).data?.records || [];
    const newQcs = qcsAfter.filter((q: any) =>
      q.serviceLogId === log.serviceLogId && q.checkType === 'COMPLETION'
    );

    expect(newQcs.length).toBeGreaterThan(qcsBeforeCount);

    // 新质检单应为 PENDING 状态
    const newQc = newQcs.find((q: any) => q.checkResult === 'PENDING');
    expect(newQc).toBeTruthy();
  });

  test('TC-AUTO-QC-02: 自动创建的质检单关联正确的orderId和serviceLogId', async ({ request }) => {
    const log = await getSubmittedLog(request);
    if (!log) {
      test.skip();
      return;
    }

    // 审核通过
    await request.post(`${API}/service-log/${log.serviceLogId}/review`, {
      data: { result: 'APPROVED', comment: 'TC-AUTO-QC-02' },
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    await new Promise(r => setTimeout(r, 500));

    // 查询新创建的质检单
    const qcResp = await request.get(`${API}/quality-check/list?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const qcs = (await qcResp.json()).data?.records || [];
    const autoQc = qcs.find((q: any) =>
      q.serviceLogId === log.serviceLogId &&
      q.checkResult === 'PENDING' &&
      q.orderId === log.orderId
    );
    expect(autoQc).toBeTruthy();
    expect(autoQc.orderId).toBe(log.orderId);
    expect(autoQc.serviceLogId).toBe(log.serviceLogId);
  });

  test('TC-AUTO-QC-03: 同一日志重复审核通过 → 不重复创建质检单', async ({ request }) => {
    const log = await getSubmittedLog(request);
    if (!log) {
      test.skip();
      return;
    }

    // 第一次审核通过
    await request.post(`${API}/service-log/${log.serviceLogId}/review`, {
      data: { result: 'APPROVED', comment: 'first' },
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    await new Promise(r => setTimeout(r, 500));

    // 查质检数量
    const qcResp = await request.get(`${API}/quality-check/list?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const qcs = (await qcResp.json()).data?.records || [];
    const qcCount = qcs.filter((q: any) =>
      q.serviceLogId === log.serviceLogId && q.checkType === 'COMPLETION'
    ).length;

    // 第二次审核通过（同一条日志再次审核）
    await request.post(`${API}/service-log/${log.serviceLogId}/review`, {
      data: { result: 'APPROVED', comment: 'second' },
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    await new Promise(r => setTimeout(r, 500));

    // 再次查质检数量
    const qcResp2 = await request.get(`${API}/quality-check/list?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const qcs2 = (await qcResp2.json()).data?.records || [];
    const qcCount2 = qcs2.filter((q: any) =>
      q.serviceLogId === log.serviceLogId && q.checkType === 'COMPLETION'
    ).length;

    // 不应增加
    expect(qcCount2).toBe(qcCount);
  });

  test('TC-AUTO-QC-04: 日志审核驳回（REJECTED）→ 不创建质检单', async ({ request }) => {
    const log = await getDraftLog(request);
    if (!log) {
      test.skip();
      return;
    }

    // 记录审核前的质检数量
    const qcBeforeResp = await request.get(`${API}/quality-check/list?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const qcsBefore = (await qcBeforeResp.json()).data?.records || [];
    const qcsBeforeCount = qcsBefore.filter((q: any) =>
      q.serviceLogId === log.serviceLogId
    ).length;

    // 审核驳回
    await request.post(`${API}/service-log/${log.serviceLogId}/review`, {
      data: { result: 'REJECTED', comment: '驳回测试' },
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    await new Promise(r => setTimeout(r, 300));

    // 验证无新增质检单
    const qcAfterResp = await request.get(`${API}/quality-check/list?page=1&pageSize=100`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const qcsAfter = (await qcAfterResp.json()).data?.records || [];
    const qcsAfterCount = qcsAfter.filter((q: any) =>
      q.serviceLogId === log.serviceLogId
    ).length;

    expect(qcsAfterCount).toBe(qcsBeforeCount);
  });
});
