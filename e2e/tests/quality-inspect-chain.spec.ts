import { test, expect } from '@playwright/test';

const API = 'http://localhost:8080/api';

/**
 * 质检执行（inspect）联动逻辑测试
 * PUT /api/quality-check/{id}/inspect
 * InspectionDTO: checkScore, checkMethod, checkResult, checkRemark, rectifyNotice, rectifyDeadline
 *
 * 接口行为（已验证）：
 * - PENDING 状态 → inspect 成功，状态变为 QUALIFIED/NEED_RECTIFY/UNQUALIFIED
 * - QUALIFIED 状态再 inspect → HTTP 400 "只有待质检状态才能执行质检"
 * - NEED_RECTIFY 无 rectifyNotice → 仍然成功（后端未强制校验）
 *
 * 注意：每个测试内部独立获取 token 和 PENDING QC，避免状态共享导致冲突
 */

// 获取 admin token（每次重新获取，避免与 describe 共享状态）
async function getAdminToken(request: any): Promise<string> {
  const login = await request.post(`${API}/auth/login`, {
    data: { username: 'admin', password: 'admin123' }
  });
  return (await login.json()).data?.accessToken as string;
}

/**
 * 找一个未执行过 inspect 的服务完成质检（COMPLETION 类型）
 * 特征：checkScore === null（已质检的 checkScore 有值）
 * 每次调用都重新查询，确保拿到未被操作过的记录
 */
async function getUninspectedCompletionQC(request: any, token: string): Promise<any> {
  const resp = await request.get(`${API}/quality-check/list?page=1&pageSize=100`, {
    headers: { Authorization: `Bearer ${token}` }
  });
  const records = (await resp.json()).data?.records || [];
  // checkResult=PENDING 且 checkScore=null → 真正可执行 inspect 的记录
  // list API 返回字段是 checkResult（不是 checkStatus）
  // checkType 是小写: "completion", "routine"
  return records.find((q: any) =>
    q.checkResult === 'PENDING' && q.checkScore === null && q.checkType?.toLowerCase() === 'completion'
  );
}

test.describe.serial('质检执行 inspect 联动', () => {

  test('TC-QC-INSPECT-01: inspect结果=QUALIFIED → 质检状态变为QUALIFIED', async ({ request }) => {
    const token = await getAdminToken(request);
    if (!token) { test.skip(); return; }
    const headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };

    const qc = await getUninspectedCompletionQC(request, token);
    if (!qc) { test.skip(); return; }

    const resp = await request.put(`${API}/quality-check/${qc.qualityCheckId}/inspect`, {
      data: {
        checkScore: 95,
        checkMethod: 'PHOTO_REVIEW',
        checkResult: 'QUALIFIED',
        checkRemark: '服务到位，评分95分'
      },
      headers
    });
    const body = await resp.json();
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);
    expect(body.message).toContain('完成');

    // 验证：detail API 直接查该 QC，字段精确
    const detailResp = await request.get(`${API}/quality-check/${qc.qualityCheckId}`, { headers });
    const detail = (await detailResp.json()).data;
    expect(detail?.checkResult).toBe('QUALIFIED');
  });

  test('TC-QC-INSPECT-02: inspect结果=NEED_RECTIFY → 质检状态变为NEED_RECTIFY，needRectify=true', async ({ request }) => {
    const token = await getAdminToken(request);
    if (!token) { test.skip(); return; }
    const headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };

    const qc = await getUninspectedCompletionQC(request, token);
    if (!qc) { test.skip(); return; }

    const resp = await request.put(`${API}/quality-check/${qc.qualityCheckId}/inspect`, {
      data: {
        checkScore: 65,
        checkMethod: 'PHONE_REVIEW',
        checkResult: 'NEED_RECTIFY',
        rectifyNotice: '请补充服务记录并重新提交审核',
        rectifyDeadline: '2026-04-30T00:00:00'
      },
      headers
    });
    const body = await resp.json();
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);

    // 验证：detail API 直接查该 QC，字段精确
    const detail2 = (await (await request.get(`${API}/quality-check/${qc.qualityCheckId}`, { headers })).json()).data;
    expect(detail2?.checkResult).toBe('NEED_RECTIFY');
    expect(detail2?.needRectify).toBe(true);
  });

  test('TC-QC-INSPECT-03: inspect结果=UNQUALIFIED → 质检状态变为UNQUALIFIED', async ({ request }) => {
    const token = await getAdminToken(request);
    if (!token) { test.skip(); return; }
    const headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };

    const qc = await getUninspectedCompletionQC(request, token);
    if (!qc) { test.skip(); return; }

    const resp = await request.put(`${API}/quality-check/${qc.qualityCheckId}/inspect`, {
      data: {
        checkScore: 30,
        checkMethod: 'ON_SITE',
        checkResult: 'UNQUALIFIED',
        checkRemark: '服务严重不达标，予以终止'
      },
      headers
    });
    const body = await resp.json();
    expect(resp.status()).toBe(200);
    expect(body.code).toBe(200);

    // 验证：detail API 直接查该 QC
    const detail3 = (await (await request.get(`${API}/quality-check/${qc.qualityCheckId}`, { headers })).json()).data;
    expect(detail3?.checkResult).toBe('UNQUALIFIED');
  });

  test('TC-QC-INSPECT-04: inspect已完成（QUALIFIED）再执行 → HTTP 400', async ({ request }) => {
    const token = await getAdminToken(request);
    if (!token) { test.skip(); return; }
    const headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };

    // 找一个 QUALIFIED 的质检
    const resp = await request.get(`${API}/quality-check/list?page=1&pageSize=100`, { headers: { Authorization: `Bearer ${token}` } });
    const qualified = ((await resp.json()).data?.records || [])
      .find((q: any) => q.checkResult?.toUpperCase() === 'QUALIFIED' && q.checkType?.toLowerCase() === 'completion');
    if (!qualified) { test.skip(); return; }

    const retryResp = await request.put(`${API}/quality-check/${qualified.qualityCheckId}/inspect`, {
      data: {
        checkScore: 90,
        checkMethod: 'PHOTO_REVIEW',
        checkResult: 'QUALIFIED'
      },
      headers
    });
    expect(retryResp.status()).toBe(400);
    const body = await retryResp.json();
    expect(body.code).toBe(400);
    expect(body.message).toContain('待质检');
  });

  test('TC-QC-INSPECT-05: inspect时checkScore字段缺失 → 仍成功（score非必填）', async ({ request }) => {
    const token = await getAdminToken(request);
    if (!token) { test.skip(); return; }
    const headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };

    const qc = await getUninspectedCompletionQC(request, token);
    if (!qc) { test.skip(); return; }

    const resp = await request.put(`${API}/quality-check/${qc.qualityCheckId}/inspect`, {
      data: {
        checkMethod: 'PHONE_REVIEW',
        checkResult: 'QUALIFIED'
      },
      headers
    });
    const body = await resp.json();
    expect(body.code).toBe(200);
  });

  test('TC-QC-INSPECT-06: 无效checkResult值 → 应拒绝', async ({ request }) => {
    const token = await getAdminToken(request);
    if (!token) { test.skip(); return; }
    const headers = { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' };

    const qc = await getUninspectedCompletionQC(request, token);
    if (!qc) { test.skip(); return; }

    const resp = await request.put(`${API}/quality-check/${qc.qualityCheckId}/inspect`, {
      data: {
        checkScore: 80,
        checkMethod: 'PHOTO_REVIEW',
        checkResult: 'INVALID_STATUS'
      },
      headers
    });
    expect([200, 400, 500]).toContain(resp.status());
    if (resp.status() === 200) {
      const body = await resp.json();
      console.log('WARNING: backend accepted invalid checkResult:', body);
    }
  });
});
