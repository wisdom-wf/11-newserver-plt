import { test, expect, Page } from '@playwright/test';

/**
 * Phase 2: UE交互质量测试
 * 重点验证 Phase 1 发现的交互问题
 */

const API_BASE = 'https://wisdomdance.cn/jxy/api';

async function loginAsAdmin(page: Page) {
  await page.goto('/');
  await page.waitForLoadState('networkidle');
  
  if (!page.url().includes('login')) {
    return;
  }
  
  await page.fill('input[placeholder="请输入用户名"]', 'admin');
  await page.fill('input[placeholder="请输入密码"]', 'admin123');
  await page.click('button:has-text("确认")');
  await page.waitForURL('**/home', { timeout: 10000 });
}

// UE问题记录
const ueFindings: string[] = [];

test.describe('Phase 2: UE交互质量测试', () => {
  
  test('1. 表单提交loading状态检测', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/business/elder');
    await page.waitForLoadState('networkidle');
    
    // 点击新增按钮
    const addBtn = page.locator('button:has-text("新增")');
    await addBtn.click();
    await page.waitForTimeout(500);
    
    // 检查是否有Drawer打开
    const drawer = page.locator('.n-drawer');
    const drawerVisible = await drawer.isVisible();
    console.log('新增Drawer打开:', drawerVisible);
    
    if (drawerVisible) {
      // 查找提交按钮
      const submitBtn = page.locator('.n-drawer button:has-text("提交")');
      
      if (await submitBtn.isVisible()) {
        // 点击提交（不填数据，触发验证）
        await submitBtn.click();
        await page.waitForTimeout(1000);
        
        // 检查是否有loading状态
        const loadingBtn = page.locator('.n-button--loading');
        const hasLoading = await loadingBtn.count() > 0;
        console.log('提交时按钮loading状态:', hasLoading);
        
        if (!hasLoading) {
          ueFindings.push('❌ UE问题: 表单提交时按钮无loading状态反馈');
        } else {
          ueFindings.push('✅ 表单提交有loading状态');
        }
      }
    }
    
    // 关闭drawer
    await page.keyboard.press('Escape');
  });
  
  test('2. 删除操作二次确认检测', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/business/elder');
    await page.waitForLoadState('networkidle');
    
    // 查找删除按钮
    const deleteBtn = page.locator('button:has-text("删除")').first();
    
    if (await deleteBtn.isVisible()) {
      await deleteBtn.click();
      await page.waitForTimeout(500);
      
      // 检查是否有NPopconfirm确认框
      const popconfirm = page.locator('.n-popconfirm');
      const isConfirmVisible = await popconfirm.isVisible();
      
      console.log('删除按钮显示二次确认:', isConfirmVisible);
      
      if (isConfirmVisible) {
        ueFindings.push('✅ 删除操作有二次确认(NPopconfirm)');
      } else {
        ueFindings.push('❌ UE问题: 删除操作缺少二次确认');
      }
    } else {
      ueFindings.push('ℹ️ 老人档案列表无删除按钮(可能无数据)');
    }
  });
  
  test('3. 空数据状态展示检测', async ({ page }) => {
    await loginAsAdmin(page);
    
    // 访问服务评价页面（已知无数据）
    await page.goto('/business/evaluation');
    await page.waitForLoadState('networkidle');
    
    // 检查是否有空状态提示
    const emptyState = page.locator('.n-empty, text=/暂无数据/, text=/无数据/');
    const hasEmptyState = await emptyState.isVisible().catch(() => false);
    
    console.log('空数据页面有空状态提示:', hasEmptyState);
    
    if (hasEmptyState) {
      ueFindings.push('✅ 空数据页面显示友好提示');
    } else {
      // 检查是否显示"0"或"null"这类不友好的值
      const pageContent = await page.textContent('body');
      if (pageContent?.includes('0.0') || pageContent?.includes('null')) {
        ueFindings.push('❌ UE问题: 空数据显示不友好的值(如0.0/null)');
      } else {
        ueFindings.push('ℹ️ 服务评价页面无明显空状态问题');
      }
    }
  });
  
  test('4. 首页驾驶舱数据异常检测', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/home');
    await page.waitForLoadState('networkidle');
    
    // 检测满意度0.0%
    const satisfactionText = await page.textContent('body');
    
    if (satisfactionText?.includes('满意度') && satisfactionText?.includes('0.0%')) {
      ueFindings.push('❌ 数据问题: 满意度显示0.0%而非"暂无数据"');
    }
    
    if (satisfactionText?.includes('平均评分') && satisfactionText?.includes('0.0')) {
      ueFindings.push('❌ 数据问题: 平均评分显示0.0而非"暂无数据"');
    }
    
    if (satisfactionText?.includes('1930.3万')) {
      ueFindings.push('❌ 数据问题: 本月营收显示1930.3万，数据可能异常');
    }
    
    console.log('驾驶舱数据异常检测完成');
  });
  
  test('5. 订单管理删除确认', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/business/order');
    await page.waitForLoadState('networkidle');
    
    // 查找删除按钮
    const deleteBtn = page.locator('button:has-text("删除")').first();
    
    if (await deleteBtn.isVisible()) {
      await deleteBtn.click();
      await page.waitForTimeout(500);
      
      const popconfirm = page.locator('.n-popconfirm');
      const isConfirmVisible = await popconfirm.isVisible();
      
      if (isConfirmVisible) {
        ueFindings.push('✅ 订单管理删除有二次确认');
      } else {
        ueFindings.push('❌ UE问题: 订单管理删除缺少二次确认');
      }
    } else {
      ueFindings.push('ℹ️ 订单列表无删除按钮');
    }
  });
  
  test('6. 服务人员删除确认', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/business/staff');
    await page.waitForLoadState('networkidle');
    
    const deleteBtn = page.locator('button:has-text("删除")').first();
    
    if (await deleteBtn.isVisible()) {
      await deleteBtn.click();
      await page.waitForTimeout(500);
      
      const popconfirm = page.locator('.n-popconfirm');
      const isConfirmVisible = await popconfirm.isVisible();
      
      if (isConfirmVisible) {
        ueFindings.push('✅ 服务人员删除有二次确认');
      } else {
        ueFindings.push('❌ UE问题: 服务人员删除缺少二次确认');
      }
    } else {
      ueFindings.push('ℹ️ 服务人员列表无删除按钮');
    }
  });
  
  test('7. 服务商管理删除确认', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/provider');
    await page.waitForLoadState('networkidle');
    
    const deleteBtn = page.locator('button:has-text("删除")').first();
    
    if (await deleteBtn.isVisible()) {
      await deleteBtn.click();
      await page.waitForTimeout(500);
      
      const popconfirm = page.locator('.n-popconfirm');
      const isConfirmVisible = await popconfirm.isVisible();
      
      if (isConfirmVisible) {
        ueFindings.push('✅ 服务商管理删除有二次确认');
      } else {
        ueFindings.push('❌ UE问题: 服务商管理删除缺少二次确认');
      }
    } else {
      ueFindings.push('ℹ️ 服务商列表无删除按钮');
    }
  });
  
  test('8. 预约管理删除确认', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/appointment');
    await page.waitForLoadState('networkidle');
    
    const deleteBtn = page.locator('button:has-text("删除")').first();
    
    if (await deleteBtn.isVisible()) {
      await deleteBtn.click();
      await page.waitForTimeout(500);
      
      const popconfirm = page.locator('.n-popconfirm');
      const isConfirmVisible = await popconfirm.isVisible();
      
      if (isConfirmVisible) {
        ueFindings.push('✅ 预约管理删除有二次确认');
      } else {
        ueFindings.push('❌ UE问题: 预约管理删除缺少二次确认');
      }
    } else {
      ueFindings.push('ℹ️ 预约列表无删除按钮');
    }
  });
  
  test('9. 服务日志删除确认', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/business/service-log');
    await page.waitForLoadState('networkidle');
    
    const deleteBtn = page.locator('button:has-text("删除")').first();
    
    if (await deleteBtn.isVisible()) {
      await deleteBtn.click();
      await page.waitForTimeout(500);
      
      const popconfirm = page.locator('.n-popconfirm');
      const isConfirmVisible = await popconfirm.isVisible();
      
      if (isConfirmVisible) {
        ueFindings.push('✅ 服务日志删除有二次确认');
      } else {
        ueFindings.push('❌ UE问题: 服务日志删除缺少二次确认');
      }
    } else {
      ueFindings.push('ℹ️ 服务日志列表无删除按钮');
    }
  });
  
  // 汇总报告
  test.afterAll(async () => {
    console.log('\n========== Phase 2 UE交互问题汇总 ==========');
    ueFindings.forEach(f => console.log(f));
    console.log('============================================');
  });
});
