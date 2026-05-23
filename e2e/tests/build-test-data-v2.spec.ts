/**
 * 精确版数据构造 - 基于真实表单 DOM 结构和 Vue 组件方法
 */
import { test, expect } from '@playwright/test';

const FRONTEND = 'https://wisdomdance.cn/jxy';
const API = 'https://wisdomdance.cn/jxy/api';

async function login(page: any) {
  await page.goto(`${FRONTEND}/login/pwd-login`);
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.locator('input[placeholder="请输入用户名"]').fill('admin');
  await page.locator('input[placeholder="请输入密码"]').fill('admin123');
  await page.locator('button:has-text("确认")').click();
  await page.waitForURL(/home|dashboard/, { timeout: 15000 }).catch(() => {});
  await page.waitForTimeout(2000);
}

/** 等待页面稳定 */
async function waitStable(page: any, timeout = 15000) {
  await page.waitForLoadState('networkidle').catch(() => {});
  await page.waitForSelector('.n-layout, .n-page-container', { timeout }).catch(() => {});
  await page.waitForTimeout(1500);
}

// ====== 场景1: 创建服务商 ======

test('场景1: 创建服务商（3个）', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/provider`);
  await waitStable(page);

  const providers = [
    { name: '延安养老服务A', creditCode: '91610113MA7JQ00001', type: 'ELDER_CARE', legalPerson: '张经理', phone: '13800001111', address: '陕西省延安市宝塔区服务路1号' },
    { name: '延安养老服务B', creditCode: '91610113MA7JQ00002', type: 'HOME_CARE', legalPerson: '李经理', phone: '13800002222', address: '陕西省延安市宝塔区服务路2号' },
    { name: '延安养老服务C', creditCode: '91610113MA7JQ00003', type: 'TECH_SERVICE', legalPerson: '王经理', phone: '13800003333', address: '陕西省延安市宝塔区服务路3号' },
  ];

  for (const p of providers) {
    // 点击新增按钮（TableHeaderOperation 里的）
    const addBtn = page.locator('button:has-text("新增"), .n-button:has-text("新增")').first();
    await addBtn.waitFor({ state: 'visible', timeout: 10000 });
    await addBtn.click({ force: true });
    await page.waitForTimeout(1500);

    // 在 drawer 内填写表单
    const drawer = page.locator('.n-drawer');
    await drawer.waitFor({ state: 'visible', timeout: 5000 });

    // 填写名称
    const nameInput = drawer.locator('input[placeholder="请输入服务商名称"]');
    await nameInput.waitFor({ state: 'visible', timeout: 5000 });
    await nameInput.clear();
    await nameInput.fill(p.name);

    // 填写信用代码
    const creditInput = drawer.locator('input[placeholder="请输入统一社会信用代码"]');
    await creditInput.clear();
    await creditInput.fill(p.creditCode);

    // 填写法人
    const legalInput = drawer.locator('input[placeholder="请输入法人姓名"]');
    await legalInput.clear();
    await legalInput.fill(p.legalPerson);

    // 填写电话
    const phoneInput = drawer.locator('input[placeholder="请输入联系电话"]');
    await phoneInput.clear();
    await phoneInput.fill(p.phone);

    // 填写地址
    const addrInput = drawer.locator('input[placeholder="请输入地址"]');
    if (await addrInput.isVisible()) {
      await addrInput.clear();
      await addrInput.fill(p.address);
    }

    // 服务类别下拉（服务类别默认 HOME_CARE，直接跳过）
    // 服务商类型下拉 - 点击 placeholder "请选择服务商类型"
    const typeSelect = drawer.locator('.n-form-item:has([placeholder="请选择服务商类型"]) .n-select').first();
    if (await typeSelect.isVisible()) {
      await typeSelect.click({ force: true });
      await page.waitForTimeout(500);
      // 选对应类型
      const opt = drawer.locator(`.n-base-select-option:has-text("${p.type === 'ELDER_CARE' ? '养老服务' : p.type === 'HOME_CARE' ? '家政服务' : '网络科技服务'}")`).first();
      if (await opt.isVisible()) {
        await opt.click({ force: true });
      }
      await page.waitForTimeout(300);
    }

    await page.waitForTimeout(500);

    // 点确定
    const confirmBtn = drawer.locator('.n-drawer-footer .n-button:has-text("确认"), .n-drawer-footer .n-space .n-button:last-child').last();
    if (await confirmBtn.isVisible()) {
      await confirmBtn.click({ force: true });
    } else {
      await page.locator('.n-drawer button:has-text("确认")').last().click({ force: true });
    }

    await page.waitForTimeout(2000);
    console.log(`✅ 服务商: ${p.name}`);
  }

  // 验证
  const rows = page.locator('.n-data-table tbody tr');
  expect(await rows.count()).toBeGreaterThanOrEqual(3);
});

// ====== 场景2: 创建服务人员 ======

test('场景2: 创建服务人员（6个）', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/business/staff`);
  await waitStable(page);

  const staffList = [
    { name: '张服务员A', phone: '13900001001', gender: '男' },
    { name: '李服务员B', phone: '13900001002', gender: '女' },
    { name: '王服务员C', phone: '13900001003', gender: '男' },
    { name: '赵服务员D', phone: '13900001004', gender: '女' },
    { name: '刘服务员E', phone: '13900001005', gender: '男' },
    { name: '陈服务员F', phone: '13900001006', gender: '女' },
  ];

  for (const s of staffList) {
    const addBtn = page.locator('button:has-text("新增"), .n-button:has-text("新增")').first();
    await addBtn.waitFor({ state: 'visible', timeout: 10000 });
    await addBtn.click({ force: true });
    await page.waitForTimeout(1500);

    const drawer = page.locator('.n-drawer');
    await drawer.waitFor({ state: 'visible', timeout: 5000 });

    // 找姓名输入框
    const nameInput = drawer.locator('input[placeholder*="姓名"], input[aria-label*="姓名"]').first();
    if (await nameInput.isVisible()) {
      await nameInput.clear();
      await nameInput.fill(s.name);
    }

    // 找手机输入框
    const phoneInput = drawer.locator('input[placeholder*="手机"], input[placeholder*="电话"], input[aria-label*="手机"]').first();
    if (await phoneInput.isVisible()) {
      await phoneInput.clear();
      await phoneInput.fill(s.phone);
    }

    await page.waitForTimeout(500);

    // 提交
    const confirmBtn = drawer.locator('.n-drawer-footer .n-button:last-child, .n-drawer button:has-text("确认")').last();
    if (await confirmBtn.isVisible()) {
      await confirmBtn.click({ force: true });
    }

    await page.waitForTimeout(2000);
    console.log(`✅ 服务人员: ${s.name}`);
  }

  const rows = page.locator('.n-data-table tbody tr');
  expect(await rows.count()).toBeGreaterThanOrEqual(6);
});

// ====== 场景3: 创建老人档案 ======

test('场景3: 创建老人档案（10个）', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/business/elder`);
  await waitStable(page);

  const elders = [
    { name: '张大爷', idCard: '612601194503150011', phone: '13900002001', address: '陕西省延安市宝塔区东惠苑1号', gender: '男' },
    { name: '李奶奶', idCard: '612601194807220022', phone: '13900002002', address: '陕西省延安市宝塔区农昌园2号', gender: '女' },
    { name: '王大爷', idCard: '612601194211080033', phone: '13900002003', address: '陕西省延安市宝塔区新区杨家岭北苑3号', gender: '男' },
    { name: '赵阿姨', idCard: '612601195005180044', phone: '13900002004', address: '陕西省延安市宝塔区东苑路4号', gender: '女' },
    { name: '刘大爷', idCard: '612601194009300055', phone: '13900002005', address: '陕西省延安市宝塔区服务路5号', gender: '男' },
    { name: '陈奶奶', idCard: '612601194612010066', phone: '13900002006', address: '陕西省延安市宝塔区长安路6号', gender: '女' },
    { name: '孙大爷', idCard: '612601194404250077', phone: '13900002007', address: '陕西省延安市宝塔区科技路7号', gender: '男' },
    { name: '周阿姨', idCard: '612601195208140088', phone: '13900002008', address: '陕西省延安市宝塔区凤城路8号', gender: '女' },
    { name: '吴大爷', idCard: '612601194301200099', phone: '13900002009', address: '陕西省延安市宝塔区长乐路9号', gender: '男' },
    { name: '黄奶奶', idCard: '612601194706050100', phone: '13900002010', address: '陕西省延安市宝塔区劳动路10号', gender: '女' },
  ];

  for (const e of elders) {
    const addBtn = page.locator('button:has-text("新增"), .n-button:has-text("新增")').first();
    await addBtn.waitFor({ state: 'visible', timeout: 10000 });
    await addBtn.click({ force: true });
    await page.waitForTimeout(1500);

    const drawer = page.locator('.n-drawer');
    await drawer.waitFor({ state: 'visible', timeout: 5000 });

    // 姓名
    const nameInput = drawer.locator('input[placeholder*="姓名"]').first();
    if (await nameInput.isVisible()) {
      await nameInput.clear();
      await nameInput.fill(e.name);
    }

    // 身份证
    const idInput = drawer.locator('input[placeholder*="证件"], input[placeholder*="身份"]').first();
    if (await idInput.isVisible()) {
      await idInput.clear();
      await idInput.fill(e.idCard);
    }

    // 手机
    const phoneInput = drawer.locator('input[placeholder*="手机"], input[placeholder*="电话"]').first();
    if (await phoneInput.isVisible()) {
      await phoneInput.clear();
      await phoneInput.fill(e.phone);
    }

    // 地址
    const addrInput = drawer.locator('input[placeholder*="地址"]').first();
    if (await addrInput.isVisible()) {
      await addrInput.clear();
      await addrInput.fill(e.address);
    }

    await page.waitForTimeout(500);

    // 提交
    const confirmBtn = drawer.locator('.n-drawer-footer .n-button:last-child, .n-drawer button:has-text("确认")').last();
    if (await confirmBtn.isVisible()) {
      await confirmBtn.click({ force: true });
    }

    await page.waitForTimeout(2000);
    console.log(`✅ 老人: ${e.name}`);
  }

  const rows = page.locator('.n-data-table tbody tr');
  expect(await rows.count()).toBeGreaterThanOrEqual(10);
});

// ====== 场景4: 派单（前10个预约）======

test('场景4: 派单生成订单', async ({ page }) => {
  await login(page);
  await page.goto(`${FRONTEND}/appointment`);
  await waitStable(page);

  let dispatched = 0;
  for (let i = 0; i < 10 && dispatched < 10; i++) {
    const confirmBtn = page.locator('.n-data-table button:has-text("确认"), .n-data-table button:has-text("派单"), tbody button:has-text("确认")').first();
    if (!await confirmBtn.isVisible().catch(() => false)) break;

    await confirmBtn.click({ force: true });
    await page.waitForTimeout(1500);

    // 选择服务商下拉
    const providerSelect = page.locator('.n-modal .n-select, .n-drawer .n-select').first();
    if (await providerSelect.isVisible().catch(() => false)) {
      await providerSelect.click({ force: true });
      await page.waitForTimeout(500);
      const opt = page.locator('.n-base-select-option').first();
      if (await opt.isVisible().catch(() => false)) {
        await opt.click({ force: true });
      }
      await page.waitForTimeout(300);
    }

    // 点确定
    const confirm = page.locator('.n-modal button:has-text("确定"), .n-drawer button:has-text("确定")').last();
    if (await confirm.isVisible().catch(() => false)) {
      await confirm.click({ force: true });
      await page.waitForTimeout(2000);
      dispatched++;
      console.log(`✅ 派单 #${dispatched}`);
    }

    // 刷新
    await page.goto(`${FRONTEND}/appointment`);
    await waitStable(page);
  }

  expect(dispatched).toBeGreaterThan(0);
});
