# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: appointment-api.spec.ts >> 预约确认API测试 >> 统计接口应该正常返回
- Location: e2e/tests/appointment-api.spec.ts:11:3

# Error details

```
Error: expect(received).toBe(expected) // Object.is equality

Expected: 200
Received: 401
```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test';
  2  | 
  3  | /**
  4  |  * 预约确认API测试
  5  |  * 直接测试后端API，验证修复后不会返回错误
  6  |  */
  7  | 
  8  | test.describe('预约确认API测试', () => {
  9  |   const BASE_URL = 'http://localhost:8080';
  10 |   
  11 |   test('统计接口应该正常返回', async ({ request }) => {
  12 |     // 测试统计接口
  13 |     const response = await request.get(`${BASE_URL}/api/appointment/statistics`);
  14 |     
  15 |     console.log('Statistics API Response:', response.status());
  16 |     
  17 |     // 验证接口返回200
> 18 |     expect(response.status()).toBe(200);
     |                               ^ Error: expect(received).toBe(expected) // Object.is equality
  19 |     
  20 |     // 验证返回数据结构
  21 |     const data = await response.json();
  22 |     console.log('Statistics Data:', JSON.stringify(data, null, 2));
  23 |     
  24 |     // 验证返回码
  25 |     expect(data.code).toMatch(/^(0000|200)$/);
  26 |   });
  27 | 
  28 |   test('预约列表接口应该正常返回', async ({ request }) => {
  29 |     // 测试预约列表接口
  30 |     const response = await request.get(`${BASE_URL}/api/appointment/list`, {
  31 |       params: {
  32 |         current: 1,
  33 |         pageSize: 10
  34 |       }
  35 |     });
  36 |     
  37 |     console.log('Appointment List API Response:', response.status());
  38 |     
  39 |     // 验证接口返回200
  40 |     expect(response.status()).toBe(200);
  41 |     
  42 |     // 验证返回数据结构
  43 |     const data = await response.json();
  44 |     console.log('List Data:', JSON.stringify(data, null, 2));
  45 |     
  46 |     // 验证返回码
  47 |     expect(data.code).toMatch(/^(0000|200)$/);
  48 |   });
  49 | });
  50 | 
```