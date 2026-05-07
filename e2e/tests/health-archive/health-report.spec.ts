import { test, expect } from '@playwright/test';

const API_BASE = 'https://wisdomdance.cn/jxy/api';

/**
 * Health Report API Tests
 *
 * Prerequisites:
 * - Backend running on http://localhost:8080
 * - MySQL database with test data (elder record with known elderId)
 */
test.describe('Health Report API Tests', () => {
  let adminToken: string;
  let testElderId: string;
  let createdReportId: string;

  test.beforeAll(async ({ request }) => {
    // Login as admin to get token
    const loginResponse = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(loginResponse.ok()).toBeTruthy();
    const loginData = await loginResponse.json();
    adminToken = loginData.data.accessToken;

    // Find a test elder ID
    const elderResponse = await request.get(`${API_BASE}/elders?current=1&pageSize=1`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });
    expect(elderResponse.ok()).toBeTruthy();
    const elderData = await elderResponse.json();
    if (elderData.data && elderData.data.records && elderData.data.records.length > 0) {
      testElderId = elderData.data.records[0].elderId;
    } else {
      throw new Error('No test elder found in database. Please ensure database has test data.');
    }
  });

  test.afterAll(async ({ request }) => {
    // Cleanup: delete created report if exists
    if (createdReportId && adminToken) {
      await request.delete(`${API_BASE}/elders/health-reports/${createdReportId}`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      });
    }
  });

  test('TC-REPT-001: Generate monthly health report', async ({ request }) => {
    const response = await request.post(`${API_BASE}/elders/${testElderId}/health-reports`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        reportType: 'MONTHLY',
        startDate: '2026-03-01',
        endDate: '2026-03-31',
        title: '月度健康报告测试'
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data.reportId).toBeDefined();
    expect(data.data.reportNo).toBeDefined();
    createdReportId = data.data.reportId;
  });

  test('TC-REPT-002: Generate quarterly health report', async ({ request }) => {
    const response = await request.post(`${API_BASE}/elders/${testElderId}/health-reports`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        reportType: 'QUARTERLY',
        startDate: '2026-01-01',
        endDate: '2026-03-31'
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data.reportId).toBeDefined();
  });

  test('TC-REPT-003: Generate yearly health report', async ({ request }) => {
    const response = await request.post(`${API_BASE}/elders/${testElderId}/health-reports`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        reportType: 'YEARLY',
        startDate: '2025-01-01',
        endDate: '2025-12-31'
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data.reportId).toBeDefined();
  });

  test('TC-REPT-004: Get health report list', async ({ request }) => {
    const response = await request.get(`${API_BASE}/elders/${testElderId}/health-reports`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(Array.isArray(data.data)).toBeTruthy();
  });

  test('TC-REPT-005: Get health report by ID', async ({ request }) => {
    // First create a report
    const createResponse = await request.post(`${API_BASE}/elders/${testElderId}/health-reports`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        reportType: 'MONTHLY',
        startDate: '2026-04-01',
        endDate: '2026-04-30'
      }
    });
    expect(createResponse.ok()).toBeTruthy();
    const createData = await createResponse.json();
    const reportId = createData.data.reportId;

    // Get it by ID
    const response = await request.get(`${API_BASE}/elders/health-reports/${reportId}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data.reportId).toBe(reportId);
  });

  test('TC-REPT-006: Download health report PDF', async ({ request }) => {
    // First create a report
    const createResponse = await request.post(`${API_BASE}/elders/${testElderId}/health-reports`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        reportType: 'MONTHLY',
        startDate: '2026-04-01',
        endDate: '2026-04-30'
      }
    });
    expect(createResponse.ok()).toBeTruthy();
    const createData = await createResponse.json();
    const reportId = createData.data.reportId;

    // Download PDF
    const response = await request.get(`${API_BASE}/elders/health-reports/${reportId}/pdf`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(response.ok()).toBeTruthy();
    // PDF should be binary data
    const buffer = await response.body();
    expect(buffer.length).toBeGreaterThan(0);
    // Check PDF magic number
    expect(buffer.toString('utf8', 0, 4)).toBe('%PDF');
  });

  test('TC-REPT-007: Delete health report', async ({ request }) => {
    // First create a report to delete
    const createResponse = await request.post(`${API_BASE}/elders/${testElderId}/health-reports`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        reportType: 'SPECIAL',
        startDate: '2026-04-01',
        endDate: '2026-04-30'
      }
    });
    expect(createResponse.ok()).toBeTruthy();
    const createData = await createResponse.json();
    const reportIdToDelete = createData.data.reportId;

    // Delete it
    const deleteResponse = await request.delete(`${API_BASE}/elders/health-reports/${reportIdToDelete}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(deleteResponse.ok()).toBeTruthy();
    const deleteData = await deleteResponse.json();
    expect(deleteData.code).toBe(200);
  });

  test('TC-REPT-008: Generate special health report', async ({ request }) => {
    const response = await request.post(`${API_BASE}/elders/${testElderId}/health-reports`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        reportType: 'SPECIAL',
        startDate: '2026-04-01',
        endDate: '2026-04-30',
        title: '专项健康报告'
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data.reportId).toBeDefined();
    expect(data.data.reportType).toBe('SPECIAL');
  });

  test('TC-REPT-009: Get report with invalid ID should fail', async ({ request }) => {
    const response = await request.get(`${API_BASE}/elders/health-reports/invalid-report-id`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(response.ok()).toBeFalsy();
  });

  test('TC-REPT-010: Generate report with invalid elder ID should fail', async ({ request }) => {
    const response = await request.post(`${API_BASE}/elders/invalid-elder-id/health-reports`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        reportType: 'MONTHLY',
        startDate: '2026-04-01',
        endDate: '2026-04-30'
      }
    });

    expect(response.ok()).toBeFalsy();
  });
});
