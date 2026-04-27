import { test, expect } from '@playwright/test';

const API_BASE = 'http://localhost:8080/api';
// 18位有效统一社会信用代码前缀（参考数据库真实数据）
const CREDIT_PREFIX = '91110000MA00ABCD';

test.describe('服务商账号自动创建测试', () => {
  let adminToken: string;

  test.beforeAll(async ({ request }) => {
    const loginResponse = await request.post(`${API_BASE}/auth/login`, {
      data: { username: 'admin', password: 'admin123' }
    });
    const loginBody = await loginResponse.json();
    adminToken = loginBody.data.accessToken;
  });

  test('创建服务商时自动创建账号', async ({ request }) => {
    // 18位有效格式: 2字符+6数字+10字符
    const creditCode = CREDIT_PREFIX + '11';
    const createResponse = await request.post(`${API_BASE}/providers`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: {
        providerName: '自动化测试服务商',
        providerType: 'TECH_SERVICE',
        serviceCategory: 'HOME_CARE',
        creditCode,
        legalPerson: '测试员',
        contactPhone: '13900000000',
        address: '北京市测试区',
        serviceAreas: '北京市测试区',
        description: '自动化测试用'
      }
    });

    const body = await createResponse.json();
    expect(createResponse.status()).toBe(200);
    expect(body.code).toBe(200);
    expect(body.data.providerId).toBeDefined();
    expect(body.data.autoCreatedAccount).toBeDefined();
    expect(body.data.autoCreatedAccount.username).toBeDefined();
    expect(body.data.autoCreatedAccount.roleName).toBe('服务商管理员');
  });

  test('自动创建的账号可以登录', async ({ request }) => {
    // 另一个18位有效格式
    const creditCode = CREDIT_PREFIX + '22';
    const createResponse = await request.post(`${API_BASE}/providers`, {
      headers: { Authorization: `Bearer ${adminToken}` },
      data: {
        providerName: '登录测试服务商',
        providerType: 'TECH_SERVICE',
        serviceCategory: 'HOME_CARE',
        creditCode,
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
    expect(username).toBeDefined();

    // 用默认密码验证账号可以登录
    const loginResponse = await request.post(`${API_BASE}/auth/login`, {
      data: { username, password: 'mima123' }
    });
    const loginBody = await loginResponse.json();
    expect(loginBody.code).toBe(200);
    expect(loginBody.data.userInfo.userType).toBe('PROVIDER');
  });
});
