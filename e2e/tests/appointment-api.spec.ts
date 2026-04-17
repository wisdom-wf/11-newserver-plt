import { test, expect } from '@playwright/test';

/**
 * 预约确认API测试
 * 直接测试后端API，验证修复后不会返回错误
 */

test.describe('预约确认API测试', () => {
  const BASE_URL = 'http://localhost:8080';
  
  test('统计接口应该正常返回', async ({ request }) => {
    // 测试统计接口
    const response = await request.get(`${BASE_URL}/api/appointment/statistics`);
    
    console.log('Statistics API Response:', response.status());
    
    // 验证接口返回200
    expect(response.status()).toBe(200);
    
    // 验证返回数据结构
    const data = await response.json();
    console.log('Statistics Data:', JSON.stringify(data, null, 2));
    
    // 验证返回码
    expect(data.code).toMatch(/^(0000|200)$/);
  });

  test('预约列表接口应该正常返回', async ({ request }) => {
    // 测试预约列表接口
    const response = await request.get(`${BASE_URL}/api/appointment/list`, {
      params: {
        current: 1,
        pageSize: 10
      }
    });
    
    console.log('Appointment List API Response:', response.status());
    
    // 验证接口返回200
    expect(response.status()).toBe(200);
    
    // 验证返回数据结构
    const data = await response.json();
    console.log('List Data:', JSON.stringify(data, null, 2));
    
    // 验证返回码
    expect(data.code).toMatch(/^(0000|200)$/);
  });
});
