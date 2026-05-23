/**
 * 基础数据构造 - 服务商 + 服务人员 + 老人档案
 * 通过真实 UI 操作构造
 */
import { test, expect } from '@playwright/test';

const FRONTEND = 'https://wisdomdance.cn/jxy';
const API = 'https://wisdomdance.cn/jxy/api';

// ====== 辅助函数 ======

/** 等待表格加载 */
async function waitTable(page: any, timeout = 15000) {
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.waitForSelector('.n-data-table, .n-empty', { timeout }).catch(() => {});
  await page.waitForTimeout(600);
}

/** 登录 */
async function login(page: any, username = 'admin', password = 'admin123') {
  await page.goto(`${FRONTEND}/login/pwd-login`);
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.locator('input[placeholder="请输入用户名"]').fill(username);
  await page.locator('input[placeholder="请输入密码"]').fill(password);
  await page.locator('button:has-text("确认")').click();
  await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
  await page.waitForTimeout(1500);
}

/** 点击新增按钮 */
async function clickAdd(page: any) {
  const btn = page.locator('.n-button:has-text("新增"), .n-button:has-text("添加"), button:has-text("新增")').first();
  await btn.click();
  await page.waitForTimeout(1000);
}

/** 选择下拉框选项 */
async function selectOption(page: any, selectIndex: number, optionIndex = 0) {
  const selects = page.locator('.n-select');
  const count = await selects.count();
  if (selectIndex >= count) return;
  await selects.nth(selectIndex).click();
  await page.waitForTimeout(400);
  const opts = page.locator('.n-base-select-option');
  const optCount = await opts.count();
  if (optCount > 0) {
    await opts.nth(Math.min(optionIndex, optCount - 1)).click();
  }
  await page.waitForTimeout(300);
}

/** 点击确定/保存 */
async function clickSave(page: any) {
  const btn = page.locator('.n-modal button:has-text("确定"), .n-drawer-footer button:has-text("确定"), .n-drawer-footer button:has-text("保存"), .n-modal button:has-text("保存")').last();
  await btn.click();
  await page.waitForTimeout(1500);
}

/** 填写输入框 */
async function fill(page: any, placeholder: string, value: string) {
  const input = page.locator(`input[placeholder*="${placeholder}"], input[aria-label*="${placeholder}"]`).first();
  if (await input.isVisible().catch(() => false)) {
    await input.clear();
    await input.fill(value);
    await page.waitForTimeout(200);
  }
}

// ====== 场景1: 构造服务商 ======

test('场景1: 创建服务商（3个）', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/provider`);
  await waitTable(page);

  const providers = [
    { name: '测试服务商A', phone: '13800001111', contact: '张经理', address: '西安市碑林区长安北路100号' },
    { name: '测试服务商B', phone: '13800002222', contact: '李经理', address: '西安市雁塔区科技路200号' },
    { name: '测试服务商C', phone: '13800003333', contact: '王经理', address: '西安市未央区凤城五路300号' },
  ];

  for (const p of providers) {
    await clickAdd(page);
    await page.waitForTimeout(800);

    await fill(page, '名称', p.name);
    await fill(page, '电话', p.phone);
    await fill(page, '联系人', p.contact);
    await fill(page, '地址', p.address);

    // 服务类型
    const selects = page.locator('.n-select');
    if (await selects.first().isVisible().catch(() => false)) {
      await selects.first().click({ force: true });
      await page.waitForTimeout(300);
      await page.locator('.n-base-select-option').first().click();
      await page.waitForTimeout(200);
    }

    await clickSave(page);
    await page.waitForTimeout(1000);
    console.log(`✅ 服务商: ${p.name}`);
  }

  const rows = page.locator('.n-data-table tbody tr');
  expect(await rows.count()).toBeGreaterThanOrEqual(3);
});

// ====== 场景2: 构造服务人员 ======

test('场景2: 创建服务人员（6个，每服务商2人）', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/business/staff`);
  await waitTable(page);

  const staffList = [
    { name: '张服务员', phone: '13900001001', gender: 0 },
    { name: '李服务员', phone: '13900001002', gender: 1 },
    { name: '王服务员', phone: '13900001003', gender: 0 },
    { name: '赵服务员', phone: '13900001004', gender: 1 },
    { name: '刘服务员', phone: '13900001005', gender: 0 },
    { name: '陈服务员', phone: '13900001006', gender: 1 },
  ];

  for (const s of staffList) {
    await clickAdd(page);
    await page.waitForTimeout(800);

    await fill(page, '姓名', s.name);
    await fill(page, '手机', s.phone);

    // 性别
    const selects = page.locator('.n-select');
    if (await selects.first().isVisible().catch(() => false)) {
      await selects.first().click({ force: true });
      await page.waitForTimeout(300);
      const opts = page.locator('.n-base-select-option');
      await opts.nth(s.gender).click();
      await page.waitForTimeout(200);
    }

    await clickSave(page);
    await page.waitForTimeout(1000);
    console.log(`✅ 服务人员: ${s.name}`);
  }

  const rows = page.locator('.n-data-table tbody tr');
  expect(await rows.count()).toBeGreaterThanOrEqual(6);
});

// ====== 场景3: 构造老人档案 ======

test('场景3: 创建老人档案（10个）', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/business/elder`);
  await waitTable(page);

  const elders = [
    { name: '张大爷', gender: 0, birth: '1945-03-15', idCard: '610102194503150011', phone: '13900002001', address: '西安市碑林区长安北路101号' },
    { name: '李奶奶', gender: 1, birth: '1948-07-22', idCard: '610102194807220022', phone: '13900002002', address: '西安市雁塔区科技路201号' },
    { name: '王大爷', gender: 0, birth: '1942-11-08', idCard: '610102194211080033', phone: '13900002003', address: '西安市未央区凤城五路301号' },
    { name: '赵阿姨', gender: 1, birth: '1950-05-18', idCard: '610102195005180044', phone: '13900002004', address: '西安市长安区韦曲街100号' },
    { name: '刘大爷', gender: 0, birth: '1940-09-30', idCard: '610102194009300055', phone: '13900002005', address: '西安市新城区长乐西路401号' },
    { name: '陈奶奶', gender: 1, birth: '1946-12-01', idCard: '610102194612010066', phone: '13900002006', address: '西安市莲湖区劳动路501号' },
    { name: '孙大爷', gender: 0, birth: '1944-04-25', idCard: '610102194404250077', phone: '13900002007', address: '西安市雁塔区科技路202号' },
    { name: '周阿姨', gender: 1, birth: '1952-08-14', idCard: '610102195208140088', phone: '13900002008', address: '西安市未央区凤城五路302号' },
    { name: '吴大爷', gender: 0, birth: '1943-01-20', idCard: '610102194301200099', phone: '13900002009', address: '西安市长安区韦曲街101号' },
    { name: '黄奶奶', gender: 1, birth: '1947-06-05', idCard: '610102194706050100', phone: '13900002010', address: '西安市新城区长乐西路402号' },
  ];

  for (const e of elders) {
    await clickAdd(page);
    await page.waitForTimeout(1000);

    await fill(page, '姓名', e.name);

    // 性别
    const selects = page.locator('.n-select');
    if (await selects.first().isVisible().catch(() => false)) {
      await selects.first().click({ force: true });
      await page.waitForTimeout(300);
      await page.locator('.n-base-select-option').nth(e.gender).click();
      await page.waitForTimeout(200);
    }

    // 出生日期
    await fill(page, '出生', e.birth);
    // 身份证
    await fill(page, '证件', e.idCard);
    // 手机
    await fill(page, '手机', e.phone);
    // 地址
    const addrInput = page.locator('input[placeholder*="地址"], textarea').first();
    if (await addrInput.isVisible().catch(() => false)) {
      await addrInput.fill(e.address);
    }

    await clickSave(page);
    await page.waitForTimeout(1000);
    console.log(`✅ 老人: ${e.name}`);
  }

  const rows = page.locator('.n-data-table tbody tr');
  expect(await rows.count()).toBeGreaterThanOrEqual(10);
});

// ====== 场景4: 派单（预约→订单）======

test('场景4: 派单生成订单（前10个预约）', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/appointment`);
  await waitTable(page);

  // 查找有"确认"/"派单"按钮的行
  const btnSel = 'button:has-text("确认"), button:has-text("派单"), button:has-text("处理")';
  let dispatched = 0;

  for (let retry = 0; retry < 3 && dispatched < 10; retry++) {
    const btns = page.locator(btnSel);
    const count = await btns.count();
    if (count === 0) break;

    for (let i = 0; i < count && dispatched < 10; i++) {
      const btn = page.locator(btnSel).first();
      if (!await btn.isVisible().catch(() => false)) break;

      await btn.click();
      await page.waitForTimeout(1000);

      // 选择服务商
      const selects = page.locator('.n-modal .n-select');
      if (await selects.first().isVisible().catch(() => false)) {
        await selects.first().click({ force: true });
        await page.waitForTimeout(400);
        await page.locator('.n-base-select-option').first().click();
        await page.waitForTimeout(200);
      }

      // 选择服务人员（如果有）
      if (await selects.nth(1).isVisible().catch(() => false)) {
        await selects.nth(1).click({ force: true });
        await page.waitForTimeout(400);
        await page.locator('.n-base-select-option').first().click();
        await page.waitForTimeout(200);
      }

      // 确认
      const confirmBtn = page.locator('.n-modal button:has-text("确定")').last();
      if (await confirmBtn.isVisible().catch(() => false)) {
        await confirmBtn.click();
        await page.waitForTimeout(1500);
        dispatched++;
        console.log(`✅ 派单成功 #${dispatched}`);
      }

      // 刷新列表
      await page.goto(`${FRONTEND}/appointment`);
      await waitTable(page);
    }
  }

  console.log(`共派单: ${dispatched}`);
  expect(dispatched).toBeGreaterThan(0);
});

// ====== 场景5: 完成服务日志 ======

test('场景5: 完成服务日志（前5个订单）', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/business/service-log`);
  await waitTable(page);

  const btnSel = 'button:has-text("完成"), button:has-text("签到"), button:has-text("提交服务")';
  let completed = 0;

  for (let i = 0; i < 5; i++) {
    const btns = page.locator(btnSel);
    if (await btns.count() === 0) break;

    const btn = btns.first();
    if (!await btn.isVisible().catch(() => false)) break;

    await btn.click();
    await page.waitForTimeout(800);

    // 填写服务内容
    const textarea = page.locator('textarea').first();
    if (await textarea.isVisible().catch(() => false)) {
      await textarea.fill('本次服务已完成，服务内容正常，老人状态良好，配合度良好。');
    }

    await clickSave(page);
    await page.waitForTimeout(1500);
    completed++;
    console.log(`✅ 服务日志完成 #${completed}`);

    await page.goto(`${FRONTEND}/business/service-log`);
    await waitTable(page);
  }

  console.log(`共完成服务日志: ${completed}`);
});

// ====== 场景6: 提交质检 ======

test('场景6: 提交质检（2条）', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/business/quality`);
  await waitTable(page);

  const addBtn = page.locator('button:has-text("新增"), button:has-text("添加")').first();
  if (!await addBtn.isVisible().catch(() => false)) {
    console.log('质检新增按钮不可见');
    return;
  }

  for (let i = 0; i < 2; i++) {
    await addBtn.click();
    await page.waitForTimeout(800);

    // 选择订单
    const selects = page.locator('.n-select');
    if (await selects.first().isVisible().catch(() => false)) {
      await selects.first().click({ force: true });
      await page.waitForTimeout(400);
      const opts = page.locator('.n-base-select-option');
      if (await opts.count() > 0) {
        await opts.first().click();
        await page.waitForTimeout(200);
      }
    }

    // 选择质检结果
    if (await selects.nth(1).isVisible().catch(() => false)) {
      await selects.nth(1).click({ force: true });
      await page.waitForTimeout(400);
      await page.locator('.n-base-select-option').first().click();
      await page.waitForTimeout(200);
    }

    await clickSave(page);
    await page.waitForTimeout(1500);
    console.log(`✅ 质检 #${i + 1} 已提交`);

    await page.goto(`${FRONTEND}/business/quality`);
    await waitTable(page);
  }
});

// ====== 场景7: 提交满意度评价 ======

test('场景7: 提交满意度评价（2条）', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/business/evaluation`);
  await waitTable(page);

  const addBtn = page.locator('button:has-text("新增"), button:has-text("添加")').first();
  if (!await addBtn.isVisible().catch(() => false)) {
    console.log('评价新增按钮不可见');
    return;
  }

  for (let i = 0; i < 2; i++) {
    await addBtn.click();
    await page.waitForTimeout(800);

    // 选择订单
    const selects = page.locator('.n-select');
    if (await selects.first().isVisible().catch(() => false)) {
      await selects.first().click({ force: true });
      await page.waitForTimeout(400);
      const opts = page.locator('.n-base-select-option');
      if (await opts.count() > 0) {
        await opts.first().click();
        await page.waitForTimeout(200);
      }
    }

    await clickSave(page);
    await page.waitForTimeout(1500);
    console.log(`✅ 评价 #${i + 1} 已提交`);

    await page.goto(`${FRONTEND}/business/evaluation`);
    await waitTable(page);
  }
});
