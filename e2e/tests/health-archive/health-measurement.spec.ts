import { test, expect } from '@playwright/test';

const API_BASE = 'https://wisdomdance.cn/jxy/api';

/**
 * Health Measurement API Tests
 *
 * Prerequisites:
 * - Backend running on http://localhost:8080
 * - MySQL database with test data (elder record with known elderId)
 */
test.describe('Health Measurement API Tests', () => {
  let adminToken: string;
  let testElderId: string;
  let createdMeasurementId: string;

  test.beforeAll(async ({ request }) => {
    // Login as admin to get token
    const loginResponse = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    expect(loginResponse.ok()).toBeTruthy();
    const loginData = await loginResponse.json();
    adminToken = loginData.data.accessToken;

    // Find a test elder ID - we need an existing elder in the database
    // First, get elder list
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
    // Cleanup: delete created measurement if exists
    if (createdMeasurementId && adminToken) {
      await request.delete(`${API_BASE}/elders/measurements/${createdMeasurementId}`, {
        headers: { Authorization: `Bearer ${adminToken}` }
      });
    }
  });

  test('TC-MEAS-001: Add health measurement record', async ({ request }) => {
    const response = await request.post(`${API_BASE}/elders/${testElderId}/measurements`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        measurementType: 'BLOOD_PRESSURE',
        measurementValue: '120/80',
        measuredAt: '2026-04-19T10:00:00',
        remark: 'Test measurement'
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data.measurementId).toBeDefined();
    createdMeasurementId = data.data.measurementId;
  });

  test('TC-MEAS-002: Get measurement history', async ({ request }) => {
    const response = await request.get(`${API_BASE}/elders/${testElderId}/measurements`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { limit: 10 }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(Array.isArray(data.data)).toBeTruthy();
  });

  test('TC-MEAS-003: Get measurement statistics', async ({ request }) => {
    const response = await request.get(`${API_BASE}/elders/${testElderId}/measurements/statistics`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { measurementType: 'BLOOD_PRESSURE' }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    // Statistics should include count, latestValue, etc.
    expect(data.data).toHaveProperty('measurementType');
    expect(data.data).toHaveProperty('count');
  });

  test('TC-MEAS-004: Get all measurement statistics', async ({ request }) => {
    const response = await request.get(`${API_BASE}/elders/${testElderId}/measurements/statistics/all`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(Array.isArray(data.data)).toBeTruthy();
  });

  test('TC-MEAS-005: Delete measurement record', async ({ request }) => {
    // First create a measurement to delete
    const createResponse = await request.post(`${API_BASE}/elders/${testElderId}/measurements`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        measurementType: 'WEIGHT',
        measurementValue: '65',
        measuredAt: '2026-04-19T10:00:00'
      }
    });
    expect(createResponse.ok()).toBeTruthy();
    const createData = await createResponse.json();
    const measurementIdToDelete = createData.data.measurementId;

    // Delete it
    const deleteResponse = await request.delete(`${API_BASE}/elders/measurements/${measurementIdToDelete}`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(deleteResponse.ok()).toBeTruthy();
    const deleteData = await deleteResponse.json();
    expect(deleteData.code).toBe(200);
  });

  test('TC-MEAS-006: Add blood glucose measurement', async ({ request }) => {
    const response = await request.post(`${API_BASE}/elders/${testElderId}/measurements`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        measurementType: 'BLOOD_GLUCOSE',
        measurementValue: '6.5',
        measuredAt: '2026-04-19T11:00:00'
      }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(data.data.measurementId).toBeDefined();
    createdMeasurementId = data.data.measurementId;
  });

  test('TC-MEAS-007: Get latest measurement by type', async ({ request }) => {
    const response = await request.get(`${API_BASE}/elders/${testElderId}/measurements/latest`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      params: { measurementType: 'BLOOD_PRESSURE' }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    // Should return the latest blood pressure measurement
    if (data.data) {
      expect(data.data.measurementType).toBe('BLOOD_PRESSURE');
    }
  });

  test('TC-MEAS-008: Get latest measurements for all types', async ({ request }) => {
    const response = await request.get(`${API_BASE}/elders/${testElderId}/measurements/latest/all`, {
      headers: { Authorization: `Bearer ${adminToken}` }
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(Array.isArray(data.data)).toBeTruthy();
  });

  test('TC-MEAS-009: Add measurement with invalid elder ID should fail', async ({ request }) => {
    const response = await request.post(`${API_BASE}/elders/invalid-elder-id/measurements`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: {
        measurementType: 'BLOOD_PRESSURE',
        measurementValue: '120/80'
      }
    });

    expect(response.ok()).toBeFalsy();
  });

  test('TC-MEAS-010: Batch add measurements', async ({ request }) => {
    const response = await request.post(`${API_BASE}/elders/${testElderId}/measurements/batch`, {
      headers: {
        Authorization: `Bearer ${adminToken}`,
        'Content-Type': 'application/json'
      },
      data: [
        {
          measurementType: 'TEMPERATURE',
          measurementValue: '36.5',
          measuredAt: '2026-04-19T12:00:00'
        },
        {
          measurementType: 'PULSE',
          measurementValue: '72',
          measuredAt: '2026-04-19T12:00:00'
        }
      ]
    });

    expect(response.ok()).toBeTruthy();
    const data = await response.json();
    expect(data.code).toBe(200);
    expect(Array.isArray(data.data)).toBeTruthy();
    expect(data.data.length).toBe(2);
  });
});
