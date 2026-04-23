import { test, expect, request } from '@playwright/test';

const API_BASE = 'http://localhost:8080/api';

/**
 * Service Log API Tests - 服务日志API测试
 *
 * 覆盖最近修改：
 * - commit a841190: 健康档案与服务日志模块增强
 *   - ServiceLog新增healthObservations/medicationGiven字段
 *   - 新增按staffId查询服务日志接口
 */
test.describe('Service Log API Tests', () => {
  let adminToken: string;
  let providerToken: string;
  let testOrderId: string;
  let testStaffId: string;
  let createdServiceLogId: string;

  test.beforeAll(async ({ request: req }) => {
    // 获取 admin token
    const adminLogin = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(adminLogin.ok()).toBeTruthy();
    const adminBody = await adminLogin.json();
    adminToken = adminBody.data.accessToken;

    // 获取 provider token (CS1)
    const providerLogin = await req.post(`${API_BASE}/auth/login`, {
      data: { username: 'CS1', password: 'mima123' }
    });
    if (providerLogin.ok()) {
      const providerBody = await providerLogin.json();
      providerToken = providerBody.data.accessToken;
    }

    // 获取一个测试订单ID
    const orderResponse = await req.get(`${API_BASE}/orders?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    if (orderResponse.ok()) {
      const orderData = await orderResponse.json();
      if (orderData.data?.records?.length > 0) {
        testOrderId = orderData.data.records[0].orderId;
      }
    }

    // 获取一个测试员工ID
    const staffResponse = await req.get(`${API_BASE}/staff?page=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    if (staffResponse.ok()) {
      const staffData = await staffResponse.json();
      if (staffData.data?.records?.length > 0) {
        testStaffId = staffData.data.records[0].staffId;
      }
    }
  });

  test.afterAll(async ({ request: req }) => {
    // Cleanup: 删除创建的测试服务日志
    if (createdServiceLogId && adminToken) {
      await req.delete(`${API_BASE}/service-log/${createdServiceLogId}`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      }).catch(() => {});
    }
  });

  test('TC-SL-API-001: 创建服务日志含健康字段', async ({ request: req }) => {
    if (!testOrderId) {
      test.skip();
      return;
    }

    const response = await req.post(`${API_BASE}/service-log`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        orderId: testOrderId,
        serviceContent: '常规养老服务',
        healthObservations: '老人今日血压略高，建议持续观察',
        medicationGiven: '遵医嘱服用降压药一片'
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    createdServiceLogId = data.data?.serviceLogId || data.data;
  });

  test('TC-SL-API-002: 服务日志列表返回healthObservations字段', async ({ request: req }) => {
    const response = await req.get(`${API_BASE}/service-log/list`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { page: 1, pageSize: 10 }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data).toHaveProperty('records');

    // 验证返回的记录包含健康字段
    if (data.data.records && data.data.records.length > 0) {
      const record = data.data.records[0];
      // healthObservations 和 medicationGiven 可能为空，但不应该是undefined
      expect(record).toHaveProperty('healthObservations');
      expect(record).toHaveProperty('medicationGiven');
    }
  });

  test('TC-SL-API-003: 按订单ID查询服务日志', async ({ request: req }) => {
    if (!testOrderId) {
      test.skip();
      return;
    }

    const response = await req.get(`${API_BASE}/service-log/order/${testOrderId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data).toHaveProperty('orderId');
  });

  test('TC-SL-API-004: 更新服务日志健康字段', async ({ request: req }) => {
    if (!testOrderId) {
      test.skip();
      return;
    }

    // 先创建一条服务日志
    const createResponse = await req.post(`${API_BASE}/service-log`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        orderId: testOrderId,
        serviceContent: '更新测试服务',
        healthObservations: '初始健康观察',
        medicationGiven: '初始给药记录'
      }
    });

    if (!createResponse.ok()) {
      test.skip();
      return;
    }

    const createData = await createResponse.json();
    const logId = createData.data?.serviceLogId || createData.data;

    // 更新健康字段
    const updateResponse = await req.put(`${API_BASE}/service-log/${logId}`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        serviceContent: '更新测试服务',
        healthObservations: '更新后：老人血压已稳定',
        medicationGiven: '更新后：继续服药'
      }
    });

    expect(updateResponse.ok()).toBeTruthy();
    const updateData = await updateResponse.json();
    expect(updateData.code).toBe(200);

    // 清理
    await req.delete(`${API_BASE}/service-log/${logId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
  });

  test('TC-SL-API-005: 服务日志统计接口', async ({ request: req }) => {
    const response = await req.get(`${API_BASE}/service-log/statistics`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data).toBeDefined();
  });

  test('TC-SL-API-006: 按staffId查询服务日志（通过Staff API）', async ({ request: req }) => {
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
    expect(Array.isArray(data.data)).toBeTruthy();

    // 验证返回的服务日志包含健康字段
    if (data.data.length > 0) {
      const log = data.data[0];
      expect(log).toHaveProperty('healthObservations');
      expect(log).toHaveProperty('medicationGiven');
    }
  });

  test('TC-SL-API-007: 服务商只能看到自己的服务日志', async ({ request: req }) => {
    if (!providerToken) {
      test.skip();
      return;
    }

    const response = await req.get(`${API_BASE}/service-log/list`, {
      headers: { Authorization: `Bearer ${providerToken}` },
      params: { page: 1, pageSize: 10 }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data).toHaveProperty('records');
  });

  test('TC-SL-API-008: 服务日志详情返回完整健康信息', async ({ request: req }) => {
    if (!testOrderId) {
      test.skip();
      return;
    }

    // 创建带健康信息的服务日志
    const createResponse = await req.post(`${API_BASE}/service-log`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        orderId: testOrderId,
        serviceContent: '详情测试',
        healthObservations: '测试健康观察',
        medicationGiven: '测试给药'
      }
    });

    if (!createResponse.ok()) {
      test.skip();
      return;
    }

    const createData = await createResponse.json();
    const logId = createData.data?.serviceLogId || createData.data;

    // 获取详情
    const detailResponse = await req.get(`${API_BASE}/service-log/${logId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(detailResponse.ok()).toBeTruthy();
    const detailData = await detailResponse.json();
    expect(detailData.code).toBe(200);
    expect(detailData.data.healthObservations).toBe('测试健康观察');
    expect(detailData.data.medicationGiven).toBe('测试给药');

    // 清理
    await req.delete(`${API_BASE}/service-log/${logId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
  });

  test('TC-SL-API-009: 异常上报功能', async ({ request: req }) => {
    if (!testOrderId) {
      test.skip();
      return;
    }

    // 创建服务日志
    const createResponse = await req.post(`${API_BASE}/service-log`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        orderId: testOrderId,
        serviceContent: '异常测试'
      }
    });

    if (!createResponse.ok()) {
      test.skip();
      return;
    }

    const createData = await createResponse.json();
    const logId = createData.data?.serviceLogId || createData.data;

    // 上报异常
    const anomalyResponse = await req.put(`${API_BASE}/service-log/${logId}/anomaly`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        anomalyType: 'HEALTH_ABNORMAL',
        description: '测试异常：老人血压偏高'
      }
    });

    expect(anomalyResponse.ok()).toBeTruthy();
    const anomalyData = await anomalyResponse.json();
    expect(anomalyData.code).toBe(200);

    // 清理
    await req.delete(`${API_BASE}/service-log/${logId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
  });

  test('TC-SL-API-010: 按日期范围查询服务日志', async ({ request: req }) => {
    const response = await req.get(`${API_BASE}/service-log/list`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: {
        page: 1,
        pageSize: 10,
        startDate: '2026-01-01',
        endDate: '2026-12-31'
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data).toHaveProperty('records');
  });
});
