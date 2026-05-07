import { test, expect, request } from '@playwright/test';

const API_BASE = 'https://wisdomdance.cn/jxy/api';

/**
 * Staff Service Log Link Tests - 服务人员-服务日志关联测试
 *
 * 覆盖最近修改：
 * - commit dd8dc8f: 服务人员服务日志关联查看
 *   - StaffController新增 getServiceLogs API
 *   - StaffService新增 getServiceLogs 接口和实现
 *   - ServiceLogMapper.xml 支持按 staffId 查询服务日志
 */
test.describe('Staff Service Log Link Tests', () => {
  let adminToken: string;
  let testStaffId: string;
  let testProviderId: string;

  test.beforeAll(async ({ request: req }) => {
    // 获取 admin token
    const adminLogin = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(adminLogin.ok()).toBeTruthy();
    const adminBody = await adminLogin.json();
    adminToken = adminBody.data.accessToken;

    // 获取一个测试员工ID（在职状态）
    const staffResponse = await req.get(`${API_BASE}/staff`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: 1, pageSize: 10, status: 'ON_JOB' }
    });
    if (staffResponse.ok()) {
      const staffData = await staffResponse.json();
      if (staffData.data?.records?.length > 0) {
        testStaffId = staffData.data.records[0].staffId;
        testProviderId = staffData.data.records[0].providerId;
      }
    }
  });

  test('TC-STAFF-SL-001: 获取服务人员的服务日志列表', async ({ request: req }) => {
    if (!testStaffId) {
      test.skip();
      return;
    }

    const response = await req.get(`${API_BASE}/staff/${testStaffId}/service-logs`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { limit: 20 }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(Array.isArray(data.data)).toBeTruthy();
  });

  test('TC-STAFF-SL-002: 服务日志关联订单信息', async ({ request: req }) => {
    if (!testStaffId) {
      test.skip();
      return;
    }

    const response = await req.get(`${API_BASE}/staff/${testStaffId}/service-logs`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { limit: 10 }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);

    // 验证返回的服务日志包含订单关联信息
    if (data.data && data.data.length > 0) {
      const log = data.data[0];
      expect(log).toHaveProperty('orderId');
      expect(log).toHaveProperty('serviceContent');
    }
  });

  test('TC-STAFF-SL-003: 服务日志关联老人信息', async ({ request: req }) => {
    if (!testStaffId) {
      test.skip();
      return;
    }

    const response = await req.get(`${API_BASE}/staff/${testStaffId}/service-logs`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { limit: 10 }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);

    // 验证返回的服务日志包含老人信息
    if (data.data && data.data.length > 0) {
      const log = data.data[0];
      expect(log).toHaveProperty('elderId');
    }
  });

  test('TC-STAFF-SL-004: limit参数控制返回数量', async ({ request: req }) => {
    if (!testStaffId) {
      test.skip();
      return;
    }

    // 测试limit=1
    const response1 = await req.get(`${API_BASE}/staff/${testStaffId}/service-logs`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { limit: 1 }
    });

    expect(response1.ok()).toBeTruthy();
    const data1 = await response1.json();
    expect(data1.code).toBe(200);
    if (data1.data.length > 0) {
      expect(data1.data.length).toBeLessThanOrEqual(1);
    }

    // 测试limit=5
    const response5 = await req.get(`${API_BASE}/staff/${testStaffId}/service-logs`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { limit: 5 }
    });

    expect(response5.ok()).toBeTruthy();
    const data5 = await response5.json();
    expect(data5.code).toBe(200);
    if (data5.data.length > 0) {
      expect(data5.data.length).toBeLessThanOrEqual(5);
    }
  });

  test('TC-STAFF-SL-005: 服务人员详情返回服务统计', async ({ request: req }) => {
    if (!testStaffId) {
      test.skip();
      return;
    }

    const response = await req.get(`${API_BASE}/staff/${testStaffId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data).toHaveProperty('staffId');
    expect(data.data).toHaveProperty('staffName');

    // 验证服务统计字段存在
    const staff = data.data;
    if (staff.serviceStats) {
      expect(staff.serviceStats).toHaveProperty('totalCount');
      expect(staff.serviceStats).toHaveProperty('thisMonthCount');
    }
  });

  test('TC-STAFF-SL-006: 服务日志列表按时间倒序', async ({ request: req }) => {
    if (!testStaffId) {
      test.skip();
      return;
    }

    const response = await req.get(`${API_BASE}/staff/${testStaffId}/service-logs`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { limit: 10 }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);

    // 验证服务日志按时间倒序排列
    if (data.data && data.data.length > 1) {
      for (let i = 0; i < data.data.length - 1; i++) {
        const current = new Date(data.data[i].serviceTime || data.data[i].createTime);
        const next = new Date(data.data[i + 1].serviceTime || data.data[i + 1].createTime);
        expect(current.getTime()).toBeGreaterThanOrEqual(next.getTime());
      }
    }
  });

  test('TC-STAFF-SL-007: 不存在的员工ID返回空列表或错误', async ({ request: req }) => {
    const response = await req.get(`${API_BASE}/staff/non-existent-staff-id/service-logs`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { limit: 10 }
    });

    // 应该返回空列表或404
    expect([200, 404].includes(response.status())).toBeTruthy();
    if (response.status() === 200) {
      const data = await response.json();
      expect(data.code).toBe(200);
      expect(Array.isArray(data.data)).toBeTruthy();
    }
  });

  test('TC-STAFF-SL-008: 服务日志包含健康观察字段', async ({ request: req }) => {
    if (!testStaffId) {
      test.skip();
      return;
    }

    const response = await req.get(`${API_BASE}/staff/${testStaffId}/service-logs`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { limit: 10 }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);

    // 验证健康观察字段存在
    if (data.data && data.data.length > 0) {
      const log = data.data[0];
      expect(log).toHaveProperty('healthObservations');
      expect(log).toHaveProperty('medicationGiven');
    }
  });

  test('TC-STAFF-SL-009: 服务人员列表可按staffId筛选', async ({ request: req }) => {
    if (!testStaffId) {
      test.skip();
      return;
    }

    // 通过服务日志查询接口，按staffId筛选
    const response = await req.get(`${API_BASE}/service-log/list`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: {
        page: 1,
        pageSize: 50,
        staffId: testStaffId
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);

    // 验证返回的记录都属于该员工
    if (data.data?.records && data.data.records.length > 0) {
      for (const record of data.data.records) {
        expect(record.staffId).toBe(testStaffId);
      }
    }
  });

  test('TC-STAFF-SL-010: 无token访问员工服务日志应被拒绝', async ({ request: req }) => {
    if (!testStaffId) {
      test.skip();
      return;
    }

    const response = await req.get(`${API_BASE}/staff/${testStaffId}/service-logs`, {
      params: { limit: 10 }
    });

    // 应该返回401未授权
    expect([401, 403].includes(response.status())).toBeTruthy();
  });
});
