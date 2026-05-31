import { test, expect } from '@playwright/test';

const API = 'https://wisdomdance.cn/jxy/api';

test.describe('服务人员资质上传测试', () => {
  let adminToken: string;
  let staffId: string;

  test.beforeAll(async ({ request }) => {
    const loginResp = await request.post(`${API}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const loginBody = await loginResp.json();
    adminToken = loginBody.data?.accessToken;
    console.log('Token获取成功:', adminToken ? '是' : '否');

    // 获取一个服务人员ID
    const staffResp = await request.get(`${API}/staff?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const staffData = await staffResp.json();
    const records = staffData.data?.records || [];
    if (records.length > 0) {
      staffId = records[0].staffId;
      console.log('获取到服务人员ID:', staffId);
    }
  });

  test('TC-QUAL-01: 添加健康证资质', async ({ request }) => {
    if (!adminToken || !staffId) {
      test.skip();
      return;
    }

    // 先获取现有的资质列表
    const beforeResp = await request.get(`${API}/staff/${staffId}/qualifications`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    const beforeData = await beforeResp.json();
    console.log('当前资质数量:', beforeData.data?.length || 0);

    // 添加一个健康证资质（用测试图片的base64）
    const testBase64 = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';

    const addResp = await request.post(`${API}/staff/${staffId}/qualifications`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        qualificationType: 1,
        qualificationName: '健康证',
        certificateUrls: testBase64
      }
    });

    const addBody = await addResp.json();
    console.log('添加资质响应:', JSON.stringify(addBody));

    expect(addBody.code).toBe(200);
    console.log('✅ TC-QUAL-01 添加健康证资质成功');
  });

  test('TC-QUAL-02: 获取资质列表验证', async ({ request }) => {
    if (!adminToken || !staffId) {
      test.skip();
      return;
    }

    const resp = await request.get(`${API}/staff/${staffId}/qualifications`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    const body = await resp.json();
    console.log('资质列表响应:', JSON.stringify(body));

    expect(body.code).toBe(200);
    expect(body.data).toBeDefined();
    console.log('✅ TC-QUAL-02 获取资质列表成功，当前数量:', body.data?.length || 0);
  });
});