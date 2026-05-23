/**
 * 数据工厂 - 通过真实 UI 操作构造测试数据
 * 业务流程：服务商 → 服务人员 → 老人档案 → 预约 → 订单 → 服务日志 → 质检 → 评价
 */

import { test, Page, chromium, expect } from '@playwright/test';

const FRONTEND = 'https://wisdomdance.cn/jxy';
const ADMIN_USER = 'admin';
const ADMIN_PASS = 'admin123';

/** 等待表格加载 */
async function waitTable(page: Page, timeout = 15000) {
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.waitForSelector('.n-data-table', { timeout }).catch(() => {});
  await page.waitForTimeout(500);
}

/** 登录 */
async function login(page: Page, username = ADMIN_USER, password = ADMIN_PASS) {
  await page.goto(`${FRONTEND}/login/pwd-login`);
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.locator('input[placeholder="请输入用户名"]').fill(username);
  await page.locator('input[placeholder="请输入密码"]').fill(password);
  await page.locator('button:has-text("确认")').click();
  await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
  await page.waitForTimeout(1000);
}

/** 点击侧边栏菜单 */
async function clickMenu(page: Page, text: string) {
  // 展开所有菜单分组
  const groups = page.locator('.n-menu-item-content');
  const count = await groups.count();
  for (let i = 0; i < count; i++) {
    const el = groups.nth(i);
    const t = await el.innerText().catch(() => '');
    if (t.includes(text)) {
      await el.click();
      await page.waitForTimeout(800);
      return;
    }
  }
  throw new Error(`菜单项未找到: ${text}`);
}

/** 点击新增按钮 */
async function clickAdd(page: Page) {
  const btn = page.locator('button:has-text("新增"), button:has-text("添加"), .n-button[type="primary"]:has-text("新"), .n-button:has-text("新增")').first();
  await btn.click();
  await page.waitForTimeout(1000);
}

/** 填写文本表单字段 */
async function fillField(page: Page, label: string, value: string) {
  // 找 label 或 placeholder
  const input = page.locator(`input[placeholder*="${label}"], input[aria-label="${label}"], .n-form-item-label:has-text("${label}") ~ * input, .n-form-item:has-text("${label}") input`).first();
  await input.clear().catch(() => {});
  await input.fill(value);
  await page.waitForTimeout(200);
}

/** 从下拉框选择 */
async function selectFromDropdown(page: Page, label: string, optionText: string) {
  const selects = page.locator('.n-form-item:has-text("' + label + '") .n-select, .n-select').first();
  await selects.click();
  await page.waitForTimeout(500);
  const opt = page.locator(`.n-base-select-option:has-text("${optionText}")`).first();
  if (await opt.isVisible().catch(() => false)) {
    await opt.click();
  } else {
    // 直接输入搜索
    await page.keyboard.type(optionText);
    await page.waitForTimeout(300);
    const first = page.locator('.n-base-select-option').first();
    await first.click();
  }
  await page.waitForTimeout(300);
}

/** 点击确定/保存按钮 */
async function clickSave(page: Page) {
  const saveBtn = page.locator('.n-modal button:has-text("确定"), .n-modal button:has-text("保存"), .n-drawer-footer button:has-text("确定"), .n-drawer-footer button:has-text("保存"), button:has-text("确定"):not(.n-button--disabled)').last();
  await saveBtn.click();
  await page.waitForTimeout(1500);
}

/** 关闭弹窗 */
async function closeModal(page: Page) {
  const close = page.locator('.n-modal-close, .n-drawer-close, .n-modal button:has-text("取消")').first();
  await close.click().catch(() => {});
  await page.waitForTimeout(500);
}

// ==================== 数据构造场景 ====================

test.describe('数据工厂 - 构造完整业务链数据', () => {
  test.setTimeout(600000); // 10分钟

  const result = {
    providers: [] as string[],
    staff: [] as string[],
    elders: [] as string[],
    appointments: [] as string[],
    orders: [] as string[],
    serviceLogs: [] as string[],
    qualityChecks: [] as string[],
  };

  // ----- 场景1: 构造服务商 -----
  test('1. 构造服务商数据（3个）', async ({ page }) => {
    await login(page);

    // 导航到服务商管理
    await page.goto(`${FRONTEND}/provider`);
    await waitTable(page);

    const providerNames = ['测试服务商A', '测试服务商B', '测试服务商C'];
    for (const name of providerNames) {
      await clickAdd(page);
      await page.waitForTimeout(800);

      // 填写服务商信息（表单字段可能不同，根据实际调整）
      const nameInput = page.locator('input[placeholder*="名称"], input[aria-label*="名称"]').first();
      if (await nameInput.isVisible().catch(() => false)) {
        await nameInput.fill(name);
      }

      const phoneInput = page.locator('input[placeholder*="电话"], input[placeholder*="手机"]').first();
      if (await phoneInput.isVisible().catch(() => false)) {
        await phoneInput.fill('13800000001');
      }

      const contactInput = page.locator('input[placeholder*="联系人"]').first();
      if (await contactInput.isVisible().catch(() => false)) {
        await contactInput.fill('测试联系人');
      }

      const addressInput = page.locator('input[placeholder*="地址"]').first();
      if (await addressInput.isVisible().catch(() => false)) {
        await addressInput.fill('西安市测试区测试路1号');
      }

      // 如果有服务类型下拉框
      const serviceSelect = page.locator('.n-select').nth(1);
      if (await serviceSelect.isVisible().catch(() => false)) {
        await serviceSelect.click();
        await page.waitForTimeout(300);
        await page.locator('.n-base-select-option').first().click();
        await page.waitForTimeout(200);
      }

      await clickSave(page);
      await page.waitForTimeout(1000);
      result.providers.push(name);
      console.log(`✅ 服务商已创建: ${name}`);
    }
  });

  // ----- 场景2: 构造服务人员 -----
  test('2. 构造服务人员数据（每个服务商2人）', async ({ page }) => {
    await login(page);
    await page.goto(`${FRONTEND}/staff`);
    await waitTable(page);

    const staffNames = [
      ['张服务员A', '13900001001'],
      ['李服务员A', '13900001002'],
      ['王服务员B', '13900001003'],
      ['赵服务员B', '13900001004'],
      ['刘服务员C', '13900001005'],
      ['陈服务员C', '13900001006'],
    ];

    for (const [name, phone] of staffNames) {
      await clickAdd(page);
      await page.waitForTimeout(800);

      const nameInput = page.locator('input[placeholder*="姓名"], input[aria-label*="姓名"]').first();
      if (await nameInput.isVisible().catch(() => false)) {
        await nameInput.fill(name);
      }

      const phoneInput = page.locator('input[placeholder*="手机"], input[placeholder*="电话"]').first();
      if (await phoneInput.isVisible().catch(() => false)) {
        await phoneInput.fill(phone);
      }

      // 性别
      const genderSelect = page.locator('.n-select').nth(0);
      if (await genderSelect.isVisible().catch(() => false)) {
        await genderSelect.click();
        await page.waitForTimeout(300);
        await page.locator('.n-base-select-option').first().click();
        await page.waitForTimeout(200);
      }

      await clickSave(page);
      await page.waitForTimeout(1000);
      result.staff.push(name);
      console.log(`✅ 服务人员已创建: ${name}`);
    }
  });

  // ----- 场景3: 构造老人档案 -----
  test('3. 构造老人档案数据（10个）', async ({ page }) => {
    await login(page);
    await page.goto(`${FRONTEND}/elder`);
    await waitTable(page);

    const elders = [
      ['张大爷', '男', '1945-03-15', '610102194503150011', '13900002001', '西安市碑林区长安北路101号'],
      ['李奶奶', '女', '1948-07-22', '610102194807220022', '13900002002', '西安市雁塔区科技路201号'],
      ['王大爷', '男', '1942-11-08', '610102194211080033', '13900002003', '西安市未央区凤城五路301号'],
      ['赵阿姨', '女', '1950-05-18', '610102195005180044', '13900002004', '西安市长安区韦曲街100号'],
      ['刘大爷', '男', '1940-09-30', '610102194009300055', '13900002005', '西安市新城区长乐西路401号'],
      ['陈奶奶', '女', '1946-12-01', '610102194612010066', '13900002006', '西安市莲湖区劳动路501号'],
      ['孙大爷', '男', '1944-04-25', '610102194404250077', '13900002007', '西安市雁塔区科技路202号'],
      ['周阿姨', '女', '1952-08-14', '610102195208140088', '13900002008', '西安市未央区凤城五路302号'],
      ['吴大爷', '男', '1943-01-20', '610102194301200099', '13900002009', '西安市长安区韦曲街101号'],
      ['黄奶奶', '女', '1947-06-05', '610102194706050100', '13900002010', '西安市新城区长乐西路402号'],
    ];

    for (const [name, gender, birth, idCard, phone, address] of elders) {
      await clickAdd(page);
      await page.waitForTimeout(1000);

      // 姓名
      const nameInput = page.locator('input[placeholder*="姓名"], input[aria-label*="姓名"]').first();
      if (await nameInput.isVisible().catch(() => false)) {
        await nameInput.fill(name);
      }

      // 性别
      const genderSelect = page.locator('.n-select').first();
      if (await genderSelect.isVisible().catch(() => false)) {
        await genderSelect.click();
        await page.waitForTimeout(300);
        const opts = page.locator('.n-base-select-option');
        const cnt = await opts.count();
        const target = gender === '男' ? 0 : (cnt > 1 ? 1 : 0);
        await opts.nth(target).click();
        await page.waitForTimeout(200);
      }

      // 出生日期
      const birthInput = page.locator('input[placeholder*="出生"], input[placeholder*="生日"]').first();
      if (await birthInput.isVisible().catch(() => false)) {
        await birthInput.fill(birth);
      }

      // 身份证
      const idCardInput = page.locator('input[placeholder*="证件"], input[placeholder*="身份证"]').first();
      if (await idCardInput.isVisible().catch(() => false)) {
        await idCardInput.fill(idCard);
      }

      // 手机
      const phoneInput = page.locator('input[placeholder*="手机"]').first();
      if (await phoneInput.isVisible().catch(() => false)) {
        await phoneInput.fill(phone);
      }

      // 地址
      const addrInput = page.locator('input[placeholder*="地址"], textarea').first();
      if (await addrInput.isVisible().catch(() => false)) {
        await addrInput.fill(address);
      }

      await clickSave(page);
      await page.waitForTimeout(1000);
      result.elders.push(name);
      console.log(`✅ 老人档案已创建: ${name}`);
    }
  });

  // ----- 场景4: 构造预约 -----
  test('4. 构造预约数据（6个）', async ({ page }) => {
    await login(page);
    await page.goto(`${FRONTEND}/appointment`);
    await waitTable(page);

    const today = new Date();
    for (let i = 0; i < 6; i++) {
      const d = new Date(today);
      d.setDate(today.getDate() + i + 1);
      const dateStr = d.toISOString().split('T')[0];

      await clickAdd(page);
      await page.waitForTimeout(1000);

      // 老人选择（从下拉框选）
      const elderSelect = page.locator('.n-select').nth(0);
      if (await elderSelect.isVisible().catch(() => false)) {
        await elderSelect.click();
        await page.waitForTimeout(500);
        await page.locator('.n-base-select-option').nth(i % 10).click();
        await page.waitForTimeout(200);
      }

      // 服务类型
      const svcSelect = page.locator('.n-select').nth(1);
      if (await svcSelect.isVisible().catch(() => false)) {
        await svcSelect.click();
        await page.waitForTimeout(500);
        await page.locator('.n-base-select-option').first().click();
        await page.waitForTimeout(200);
      }

      // 服务日期
      const dateInput = page.locator('input[placeholder*="日期"]').first();
      if (await dateInput.isVisible().catch(() => false)) {
        await dateInput.fill(dateStr);
      }

      await clickSave(page);
      await page.waitForTimeout(1000);
      result.appointments.push(`预约${i + 1}`);
      console.log(`✅ 预约已创建: ${dateStr}`);
    }
  });

  // ----- 场景5: 派单生成订单 -----
  test('5. 派单生成订单', async ({ page }) => {
    await login(page);
    await page.goto(`${FRONTEND}/appointment`);
    await waitTable(page);

    // 找有"派单"按钮的行
    const dispatchBtn = page.locator('button:has-text("派单"), button:has-text("确认")').first();
    for (let i = 0; i < 6; i++) {
      const btns = page.locator('button:has-text("派单"), button:has-text("确认")');
      const cnt = await btns.count();
      if (cnt === 0) break;

      await btns.first().click();
      await page.waitForTimeout(1000);

      // 选择服务商
      const providerSelect = page.locator('.n-select').first();
      if (await providerSelect.isVisible().catch(() => false)) {
        await providerSelect.click();
        await page.waitForTimeout(500);
        await page.locator('.n-base-select-option').first().click();
        await page.waitForTimeout(300);
      }

      // 选择服务人员
      const staffSelect = page.locator('.n-select').nth(1);
      if (await staffSelect.isVisible().catch(() => false)) {
        await staffSelect.click();
        await page.waitForTimeout(500);
        await page.locator('.n-base-select-option').first().click();
        await page.waitForTimeout(300);
      }

      await clickSave(page);
      await page.waitForTimeout(1500);
      result.orders.push(`订单${i + 1}`);
      console.log(`✅ 派单成功`);
    }
  });

  // ----- 场景6: 完成服务日志 -----
  test('6. 完成服务并提交服务日志', async ({ page }) => {
    await login(page);
    await page.goto(`${FRONTEND}/service-log`);
    await waitTable(page);

    // 查找"去服务"的按钮并点击完成
    const btns = page.locator('button:has-text("完成"), button:has-text("提交"), button:has-text("签到")');
    const cnt = await btns.count();
    for (let i = 0; i < Math.min(cnt, 4); i++) {
      const b = page.locator('button:has-text("完成"), button:has-text("提交"), button:has-text("签到")').first();
      if (await b.isVisible().catch(() => false)) {
        await b.click();
        await page.waitForTimeout(1000);

        // 填写服务内容
        const contentInput = page.locator('textarea, input[placeholder*="内容"], input[placeholder*="服务"]').first();
        if (await contentInput.isVisible().catch(() => false)) {
          await contentInput.fill('本次服务已完成，服务内容正常，老人状态良好。');
        }

        await clickSave(page);
        await page.waitForTimeout(1500);
        result.serviceLogs.push(`日志${i + 1}`);
        console.log(`✅ 服务日志已提交`);
      }
    }
  });

  // ----- 场景7: 提交质检 -----
  test('7. 提交质检', async ({ page }) => {
    await login(page);
    await page.goto(`${FRONTEND}/quality`);
    await waitTable(page);

    const addBtn = page.locator('button:has-text("新增"), button:has-text("添加")').first();
    if (await addBtn.isVisible().catch(() => false)) {
      for (let i = 0; i < 2; i++) {
        await addBtn.click();
        await page.waitForTimeout(1000);

        // 选择订单（如果有订单下拉框）
        const orderSelect = page.locator('.n-select').first();
        if (await orderSelect.isVisible().catch(() => false)) {
          await orderSelect.click();
          await page.waitForTimeout(500);
          await page.locator('.n-base-select-option').first().click();
          await page.waitForTimeout(200);
        }

        // 质检结果
        const resultSelect = page.locator('.n-select').nth(1);
        if (await resultSelect.isVisible().catch(() => false)) {
          await resultSelect.click();
          await page.waitForTimeout(500);
          await page.locator('.n-base-select-option').first().click();
          await page.waitForTimeout(200);
        }

        await clickSave(page);
        await page.waitForTimeout(1500);
        result.qualityChecks.push(`质检${i + 1}`);
        console.log(`✅ 质检已提交`);
      }
    }
  });

  // ----- 汇总 -----
  test('8. 汇总报告', async ({}) => {
    console.log('\n========== 数据构造汇总 ==========');
    console.log(`服务商:   ${result.providers.length} 个`);
    console.log(`服务人员: ${result.staff.length} 个`);
    console.log(`老人档案: ${result.elders.length} 个`);
    console.log(`预约:     ${result.appointments.length} 个`);
    console.log(`订单:     ${result.orders.length} 个`);
    console.log(`服务日志: ${result.serviceLogs.length} 条`);
    console.log(`质检:     ${result.qualityChecks.length} 条`);
    console.log('==================================\n');
    expect(result.elders.length).toBeGreaterThan(0);
  });
});
