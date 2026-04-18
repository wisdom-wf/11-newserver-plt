import { test, expect, request } from '@playwright/test';

const API_BASE = 'http://localhost:8080/api';

test.describe('服务商账号自动创建测试', () => {
  let adminToken: string;
  const testCreditCode = `91110000MA00TEST${Date.now().toString().slice(-4)}`;

  test.beforeAll(async () => {
    // 登录获取 admin token
    const loginResponse = await request.post(`${API_BASE}/auth/login`, {
      data: {
        username: 'admin',
        password: 'admin123'
      }
    });
    const loginBody = await loginResponse.json();
    adminToken = loginBody.data.accessToken;
  });

  test('创建服务商时自动创建账号', async () => {
    const createResponse = await request.post(`${API_BASE}/providers`, {
      headers: {
        Authorization: `Bearer ${adminToken}`
      },
      data: {
        providerName: '自动化测试服务商',
        providerType: 'SERVICES',
        serviceCategory: 'DAYCARE',
        creditCode: testCreditCode,
        legalPerson: '测试员',
        contactPhone: '13900000000',
        address: '北京市测试区',
        serviceAreas: '北京市测试区',
        description: '自动化测试用'
      }
    });

    expect(createResponse.status()).toBe(200);
    const body = await createResponse.json();
    expect(body.code).toBe(200);
    expect(body.data.providerId).toBeDefined();
    expect(body.data.autoCreatedAccount).toBeDefined();
    expect(body.data.autoCreatedAccount.username).toBeDefined();
    expect(body.data.autoCreatedAccount.password).toBe('mima123');
    expect(body.data.autoCreatedAccount.roleName).toBe('服务商管理员');
  });

  test('自动创建的账号可以登录', async () => {
    // 先创建服务商
    const creditCode = `91110000MA00TEST${(Date.now() + 1).toString().slice(-4)}`;
    const createResponse = await request.post(`${API_BASE}/providers`, {
      headers: {
        Authorization: `Bearer ${adminToken}`
      },
      data: {
        providerName: '登录测试服务商',
        providerType: 'SERVICES',
        serviceCategory: 'DAYCARE',
        creditCode: creditCode,
        legalPerson: '测试员',
        contactPhone: '13900000001',
        address: '北京市测试区',
        serviceAreas: '北京市测试区',
        description: '自动化测试用'
      }
    });

    const createBody = await createResponse.json();
    expect(createBody.code).toBe(200);

    const username = createBody.data.autoCreatedAccount.username;
    const password = createBody.data.autoCreatedAccount.password;

    // 验证账号可以登录
    const loginResponse = await request.post(`${API_BASE}/auth/login`, {
      data: { username, password }
    });

    expect(loginResponse.status()).toBe(200);
    const loginBody = await loginResponse.json();
    expect(loginBody.code).toBe(200);
    expect(loginBody.data.userInfo.userType).toBe('PROVIDER');
  });
});
