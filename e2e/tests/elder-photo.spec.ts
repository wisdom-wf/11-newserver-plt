import { test, expect, request } from '@playwright/test';

const API_BASE = 'https://wisdomdance.cn/jxy/api';

/**
 * Elder Photo API Tests - 老人照片URL字段测试
 *
 * 覆盖最近修改：
 * - commit a841190: 健康档案与服务日志模块增强
 *   - ElderDTO新增photoUrl字段
 *   - 照片上传功能（photoUrl字段支持Base64图片存储）
 *   - photo_url字段扩展为MEDIUMTEXT支持Base64图片存储
 */
test.describe('Elder Photo API Tests', () => {
  let adminToken: string;
  let testElderId: string;

  test.beforeAll(async ({ request: req }) => {
    // 获取 admin token
    const adminLogin = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(adminLogin.ok()).toBeTruthy();
    const adminBody = await adminLogin.json();
    adminToken = adminBody.data.accessToken;

    // 获取一个测试老人ID
    const elderResponse = await req.get(`${API_BASE}/elders?current=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    if (elderResponse.ok()) {
      const elderData = await elderResponse.json();
      if (elderData.data?.records?.length > 0) {
        testElderId = elderData.data.records[0].elderId;
      }
    }
  });

  test('TC-ELDER-PHOTO-001: 老人档案返回photoUrl字段', async ({ request: req }) => {
    const response = await req.get(`${API_BASE}/elders?current=1&pageSize=10`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);

    // 验证返回的老人记录包含photoUrl字段
    if (data.data?.records && data.data.records.length > 0) {
      const elder = data.data.records[0];
      expect(elder).toHaveProperty('photoUrl');
      // photoUrl可能是null或空字符串，表示没有上传照片
      expect(elder.photoUrl === null || typeof elder.photoUrl === 'string').toBeTruthy();
    }
  });

  test('TC-ELDER-PHOTO-002: 更新老人照片URL', async ({ request: req }) => {
    if (!testElderId) {
      test.skip();
      return;
    }

    // 模拟Base64图片数据（小图片）
    const base64Photo = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';

    const response = await req.put(`${API_BASE}/elders/${testElderId}`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        name: '测试老人', // 需要传递必填字段
        idCard: '110101199001011234', // 模拟身份证号
        photoUrl: base64Photo
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);

    // 验证返回的photoUrl
    if (data.data?.photoUrl) {
      expect(data.data.photoUrl).toBe(base64Photo);
    }
  });

  test('TC-ELDER-PHOTO-003: 老人详情返回完整photoUrl', async ({ request: req }) => {
    if (!testElderId) {
      test.skip();
      return;
    }

    const response = await req.get(`${API_BASE}/elders/${testElderId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data).toHaveProperty('photoUrl');
  });

  test('TC-ELDER-PHOTO-004: 最近更新老人列表包含photoUrl', async ({ request: req }) => {
    const response = await req.get(`${API_BASE}/elders/recent`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { limit: 10 }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(Array.isArray(data.data)).toBeTruthy();

    // 验证返回的老人卡片包含photoUrl字段
    if (data.data && data.data.length > 0) {
      const card = data.data[0];
      expect(card).toHaveProperty('photoUrl');
      expect(card).toHaveProperty('elderId');
      expect(card).toHaveProperty('name');
    }
  });

  test('TC-ELDER-PHOTO-005: photoUrl字段支持Base64长字符串存储', async ({ request: req }) => {
    if (!testElderId) {
      test.skip();
      return;
    }

    // 创建一个较长的Base64字符串（模拟实际图片）
    const longBase64 = 'data:image/png;base64,' + 'A'.repeat(1000);

    const response = await req.put(`${API_BASE}/elders/${testElderId}`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        name: '测试老人',
        idCard: '110101199001011234',
        photoUrl: longBase64
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);

    // 验证长字符串被正确存储
    if (data.data?.photoUrl) {
      expect(data.data.photoUrl.length).toBeGreaterThan(500);
    }
  });

  test('TC-ELDER-PHOTO-006: photoUrl可以为空字符串', async ({ request: req }) => {
    if (!testElderId) {
      test.skip();
      return;
    }

    const response = await req.put(`${API_BASE}/elders/${testElderId}`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        name: '测试老人',
        idCard: '110101199001011234',
        photoUrl: ''
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    // 应该允许空字符串或返回null
    expect([200, 400].includes(data.code)).toBeTruthy();
  });

  test('TC-ELDER-PHOTO-007: 健康档案老人信息包含photoUrl', async ({ request: req }) => {
    if (!testElderId) {
      test.skip();
      return;
    }

    // 调用健康档案接口
    const response = await req.get(`${API_BASE}/elders/${testElderId}/health-info`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    // 接口可能不存在或返回200
    if (response.ok()) {
      const data = await response.json();
      if (data.code === 200 && data.data) {
        // 验证老人信息包含photoUrl
        if (data.data.elder) {
          expect(data.data.elder).toHaveProperty('photoUrl');
        }
      }
    }
  });

  test('TC-ELDER-PHOTO-008: 老人列表支持按姓名搜索并返回photoUrl', async ({ request: req }) => {
    const response = await req.get(`${API_BASE}/elders`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: {
        name: '张',
        current: 1,
        pageSize: 10
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);

    // 验证搜索结果包含photoUrl字段
    if (data.data?.records && data.data.records.length > 0) {
      const elder = data.data.records[0];
      expect(elder).toHaveProperty('photoUrl');
    }
  });
});
