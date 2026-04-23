import { test, expect, Page } from '@playwright/test';

/**
 * UI质量审查 - Phase 1
 * 目标：采集核心页面截图，识别UI问题
 */

const API_BASE = 'http://localhost:8080/api';

// 登录辅助函数
async function loginAsAdmin(page: Page) {
  await page.goto('/');
  await page.waitForLoadState('networkidle');
  
  // 如果已登录则跳过
  if (!page.url().includes('login')) {
    return;
  }
  
  await page.fill('input[placeholder="请输入用户名"]', 'admin');
  await page.fill('input[placeholder="请输入密码"]', 'admin123');
  await page.click('button:has-text("确认")');
  await page.waitForURL('**/home', { timeout: 10000 });
}

// 截图并记录UI问题
async function captureAndAnalyze(page: Page, pageName: string, findings: string[]) {
  const path = `e2e/screenshots/phase1-ui-audit/${pageName}.png`;
  await page.screenshot({ path, fullPage: true });
  console.log(`📸 截图已保存: ${path}`);
  
  // 检测常见UI问题
  const issues: string[] = [];
  
  // 1. 检测loading状态残留
  const loading = page.locator('.n-spin, .n-loading-bar');
  if (await loading.isVisible()) {
    issues.push('⚠️ 页面存在loading状态');
  }
  
  // 2. 检测控制台错误
  const consoleErrors: string[] = [];
  page.on('console', msg => {
    if (msg.type() === 'error') consoleErrors.push(msg.text());
  });
  
  // 3. 检测表格空状态
  const emptyState = page.locator('.n-empty');
  if (await emptyState.isVisible()) {
    issues.push('ℹ️ 表格显示空状态');
  }
  
  // 4. 检测是否有表单验证错误残留
  const formErrors = page.locator('.n-form-item-feedback-wrapper .n-form-item-feedback');
  const errorCount = await formErrors.count();
  if (errorCount > 0) {
    issues.push(`⚠️ 表单存在${errorCount}个验证错误`);
  }
  
  if (issues.length > 0) {
    findings.push(`\n【${pageName}】`);
    issues.forEach(i => findings.push(i));
  }
}

test.describe('Phase 1: UI质量审查', () => {
  const findings: string[] = [];
  
  test('1. 首页驾驶舱', async ({ page }) => {
    await loginAsAdmin(page);
    await page.waitForLoadState('networkidle');
    await captureAndAnalyze(page, 'home/dashboard', findings);
    
    // 检测数据异常
    const revenueText = await page.locator('text=/本月营收/').textContent();
    if (revenueText?.includes('1930.3万')) {
      findings.push('⚠️ 营收数据异常: 1930.3万数值过大/小数位异常');
    }
    
    const satisfactionText = await page.locator('text=/满意度/').textContent();
    if (satisfactionText?.includes('0.0%')) {
      findings.push('❌ 满意度为0.0%，疑似空数据未处理');
    }
    
    const ratingText = await page.locator('text=/平均评分/').textContent();
    if (ratingText?.includes('0.0')) {
      findings.push('❌ 平均评分为0.0，疑似空数据未处理');
    }
    
    console.log('驾驶舱审查完成');
  });
  
  test('2. 预约信息管理', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/appointment');
    await page.waitForLoadState('networkidle');
    await captureAndAnalyze(page, 'appointment/list', findings);
    
    // 检测统计卡片
    const statCards = await page.locator('.n-card').count();
    findings.push(`ℹ️ 统计卡片数量: ${statCards}`);
    
    // 检测表格
    const tableRows = await page.locator('.n-data-table-tr').count();
    findings.push(`ℹ️ 表格数据行数: ${tableRows}`);
    
    console.log('预约管理审查完成');
  });
  
  test('3. 老人档案管理', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/business/elder');
    await page.waitForLoadState('networkidle');
    await captureAndAnalyze(page, 'elder/list', findings);
    
    // 检测删除按钮是否有二次确认
    const deleteBtn = page.locator('button:has-text("删除")').first();
    if (await deleteBtn.isVisible()) {
      await deleteBtn.click();
      await page.waitForTimeout(500);
      
      const popconfirm = page.locator('.n-popconfirm');
      if (await popconfirm.isVisible()) {
        findings.push('✅ 删除按钮有二次确认(NPopconfirm)');
      } else {
        findings.push('❌ 删除按钮缺少二次确认');
      }
      // 关闭弹窗
      await page.keyboard.press('Escape');
    }
    
    console.log('老人档案审查完成');
  });
  
  test('4. 订单管理', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/business/order');
    await page.waitForLoadState('networkidle');
    await captureAndAnalyze(page, 'order/list', findings);
    
    console.log('订单管理审查完成');
  });
  
  test('5. 服务人员管理', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/business/staff');
    await page.waitForLoadState('networkidle');
    await captureAndAnalyze(page, 'staff/list', findings);
    
    // 检测状态标签
    const statusTags = await page.locator('.n-tag').count();
    findings.push(`ℹ️ 状态标签数量: ${statusTags}`);
    
    console.log('服务人员审查完成');
  });
  
  test('6. 服务日志', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/business/service-log');
    await page.waitForLoadState('networkidle');
    await captureAndAnalyze(page, 'service-log/list', findings);
    
    console.log('服务日志审查完成');
  });
  
  test('7. 质检管理', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/business/quality');
    await page.waitForLoadState('networkidle');
    await captureAndAnalyze(page, 'quality/list', findings);
    
    console.log('质检管理审查完成');
  });
  
  test('8. 服务评价', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/business/evaluation');
    await page.waitForLoadState('networkidle');
    await captureAndAnalyze(page, 'evaluation/list', findings);
    
    console.log('服务评价审查完成');
  });
  
  test('9. 服务商管理', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/provider');
    await page.waitForLoadState('networkidle');
    await captureAndAnalyze(page, 'provider/list', findings);
    
    console.log('服务商管理审查完成');
  });
  
  test('10. 系统管理-用户管理', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/system/user');
    await page.waitForLoadState('networkidle');
    await captureAndAnalyze(page, 'system/user', findings);
    
    console.log('系统用户管理审查完成');
  });
  
  // 汇总报告
  test.afterAll(async () => {
    console.log('\n========== UI质量问题汇总 ==========');
    findings.forEach(f => console.log(f));
    console.log('===================================');
  });
});
