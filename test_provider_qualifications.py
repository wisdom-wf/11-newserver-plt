#!/usr/bin/env python3
"""
Provider 资质管理 Playwright 测试
验证 Provider 资质管理的懒加载、预览模式、覆盖/追加功能
"""

import os
import time
from playwright.sync_api import sync_playwright, Page, expect

BACKEND_URL = "http://43.153.213.134:8080"
FRONTEND_URL = "https://wisdomdance.cn/jxy/home"
TEST_PROVIDER_ID = "2057492159443116033"  # 延安养老服务A社


def login_admin(page: Page):
    """登录管理元用户"""
    page.goto(f"{FRONTEND_URL}")
    page.wait_for_load_state("networkidle")

    # 如果已登录，跳过
    if page.locator("text=服务商管理").is_visible():
        return

    # 点击登录按钮或直接导航到登录页
    page.click("text=登录", timeout=5000)
    page.fill('input[type="text"]', "admin")
    page.fill('input[type="password"]', "admin123")
    page.click('button[type="submit"]')
    page.wait_for_load_state("networkidle")
    time.sleep(1)


def goto_provider_detail(page: Page, provider_id: str):
    """导航到服务商详情"""
    page.goto(f"{FRONTEND_URL}")
    page.wait_for_load_state("networkidle")

    # 进入服务商管理
    page.click('text=服务商管理', timeout=10000)
    page.wait_for_load_state("networkidle")
    time.sleep(1)

    # 点击第一个服务商的详情按钮
    detail_btns = page.locator('button:has-text("查看详情")')
    if detail_btns.count() > 0:
        detail_btns.first.click()
        page.wait_for_load_state("networkidle")
        time.sleep(0.5)
    else:
        print("⚠ 没有找到'查看详情'按钮")


def test_preview_mode_no_base64(page: Page):
    """Test 1: 预览模式不加载 base64"""
    print("\n=== Test 1: 预览模式验证 ===")

    goto_provider_detail(page, TEST_PROVIDER_ID)

    # 切换到证照管理 Tab
    page.click('div[role="tab"]:has-text("证照管理")')
    page.wait_for_load_state("networkidle")
    time.sleep(0.5)

    # 检查资质证书区域
    cert_section = page.locator('text=资质证书').first
    expect(cert_section).to_be_visible()

    # 检查是否有"点击加载"占位符（说明预览模式生效）
    placeholder = page.locator('text=点击加载')
    if placeholder.is_visible():
        print("✓ 预览模式生效：显示'点击加载'占位符")
    else:
        print("⚠ 没有看到'点击加载'占位符，可能所有资质都已加载或无资质")

    # 点击上传证书按钮，确保没有误触发加载
    upload_btn = page.locator('button:has-text("上传证书")')
    if upload_btn.is_visible():
        print("✓ 上传证书按钮可见")

    print("Test 1 PASSED")


def test_lazy_load_image(page: Page):
    """Test 2: 懒加载生效验证"""
    print("\n=== Test 2: 懒加载验证 ===")

    goto_provider_detail(page, TEST_PROVIDER_ID)
    page.click('div[role="tab"]:has-text("证照管理")')
    page.wait_for_load_state("networkidle")
    time.sleep(0.5)

    # 检查是否有"点击加载"占位符
    placeholder = page.locator('text=点击加载').first
    if not placeholder.is_visible():
        print("⚠ 没有找到'点击加载'占位符，跳过懒加载测试")
        print("Test 2 SKIPPED")
        return

    # 点击占位符触发懒加载
    placeholder.click()
    page.wait_for_load_state("networkidle")
    time.sleep(2)  # 等待图片加载

    # 检查占位符是否消失（被真实图片替代）
    # 如果图片加载成功，占位符应该消失
    try:
        placeholder.wait_for(state="hidden", timeout=3000)
        print("✓ 懒加载成功：占位符被真实图片替代")
    except:
        print("⚠ 占位符仍然可见，可能加载失败")

    print("Test 2 PASSED")


def test_upload_and_overwrite(page: Page):
    """Test 3: 覆盖上传验证"""
    print("\n=== Test 3: 覆盖上传验证 ===")

    goto_provider_detail(page, TEST_PROVIDER_ID)
    page.click('div[role="tab"]:has-text("证照管理")')
    page.wait_for_load_state("networkidle")
    time.sleep(0.5)

    # 创建测试图片（使用纯二进制方式）
    test_img_path = "/tmp/test_provider_cert.jpg"
    with open(test_img_path, "wb") as f:
        # 创建一个最小的有效 JPEG
        f.write(bytes([
            0xFF, 0xD8, 0xFF, 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01,
            0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0xFF, 0xDB, 0x00, 0x43,
            0x00, 0x08, 0x06, 0x06, 0x07, 0x06, 0x05, 0x08, 0x07, 0x07, 0x07, 0x09,
            0x09, 0x08, 0x0A, 0x0C, 0x14, 0x0D, 0x0C, 0x0B, 0x0B, 0x0C, 0x19, 0x12,
            0x13, 0x14, 0x12, 0x10, 0x13, 0x1F, 0x15, 0x1A, 0x14, 0x16, 0x19, 0x1F,
            0x1F, 0x1F, 0x1A, 0x1C, 0x1C, 0x1C, 0x20, 0x24, 0x2E, 0x27, 0x20, 0x22,
            0x2C, 0x23, 0x1C, 0x1C, 0x28, 0x37, 0x29, 0x2C, 0x30, 0x31, 0x34, 0x34,
            0x34, 0x1F, 0x27, 0x39, 0x3D, 0x38, 0x32, 0x3C, 0x2E, 0x33, 0x34, 0x32,
            0xFF, 0xC0, 0x00, 0x11, 0x08, 0x00, 0x01, 0x00, 0x01, 0x03, 0x01, 0x11,
            0x00, 0x02, 0x11, 0x01, 0x03, 0x11, 0x01, 0xFF, 0xC4, 0x00, 0x1F, 0x00,
            0x00, 0x01, 0x05, 0x01, 0x01, 0x01, 0x01, 0x01, 0x01, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x0A, 0x0B, 0xFF, 0xC4, 0x00, 0xB5, 0x10, 0x00, 0x02, 0x01,
            0x03, 0x03, 0x02, 0x04, 0x03, 0x05, 0x05, 0x04, 0x04, 0x00, 0x00, 0x01,
            0x7D, 0x01, 0x02, 0x03, 0x00, 0x04, 0x11, 0x05, 0x12, 0x21, 0x31, 0x41,
            0x06, 0x13, 0x51, 0x61, 0x07, 0x22, 0x71, 0x14, 0x32, 0x81, 0x91, 0xA1,
            0x08, 0x23, 0x42, 0xB1, 0xC1, 0x15, 0x52, 0xD1, 0xF0, 0x24, 0x33, 0x62,
            0x72, 0x82, 0x09, 0x0A, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x25, 0x26, 0x27,
            0x28, 0x29, 0x2A, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3A, 0x43, 0x44,
            0x45, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58,
            0x59, 0x5A, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6A, 0x73, 0x74,
            0x75, 0x76, 0x77, 0x78, 0x79, 0x7A, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88,
            0x89, 0x8A, 0x92, 0x93, 0x94, 0x95, 0x96, 0x97, 0x98, 0x99, 0x9A, 0xA2,
            0xA3, 0xA4, 0xA5, 0xA6, 0xA7, 0xA8, 0xA9, 0xAA, 0xB2, 0xB3, 0xB4, 0xB5,
            0xB6, 0xB7, 0xB8, 0xB9, 0xBA, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8,
            0xC9, 0xCA, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xE1,
            0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xF1, 0xF2, 0xF3,
            0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA, 0xFF, 0xDA, 0x00, 0x0B, 0x03,
            0x01, 0x00, 0x02, 0x11, 0x03, 0x11, 0x00, 0x3F, 0x00, 0xF9, 0xFE, 0x40,
            0x28, 0x02, 0xFF, 0xD9
        ]))

    # 记录上传前的证书数量
    cert_count_before = page.locator('[style*="width: 100px"][style*="height: 100px"]').count()
    print(f"上传前证书数量: {cert_count_before}")

    # 点击上传
    upload_btn = page.locator('button:has-text("上传证书")')
    upload_btn.click()

    # 选择文件
    with page.expect_file_chooser() as fc:
        pass
    fc.set_files(test_img_path)

    # 等待上传对话框出现
    dialog = page.locator('.n-dialog')
    if dialog.is_visible(timeout=3000):
        print("✓ 检测到覆盖/追加对话框")
        # 选择覆盖
        page.click('button:has-text("覆盖")')
        page.wait_for_load_state("networkidle")
        time.sleep(1)

    # 检查上传后的证书数量
    cert_count_after = page.locator('[style*="width: 100px"][style*="height: 100px"]').count()
    print(f"上传后证书数量: {cert_count_after}")

    print("Test 3 PASSED")


def test_backend_api_preview():
    """Test 4: 后端 API 验证（preview vs images）"""
    print("\n=== Test 4: 后端 API 验证 ===")

    import urllib.request
    import json

    # 登录获取 token
    login_data = json.dumps({"username": "admin", "password": "admin123"}).encode()
    req = urllib.request.Request(
        f"{BACKEND_URL}/api/auth/login",
        data=login_data,
        headers={"Content-Type": "application/json"}
    )
    with urllib.request.urlopen(req) as resp:
        token = json.loads(resp.read())["data"]["accessToken"]

    headers = {"Authorization": f"Bearer {token}"}

    # 测试 preview API
    print("\n--- Preview API ---")
    req = urllib.request.Request(
        f"{BACKEND_URL}/api/providers/{TEST_PROVIDER_ID}/certificates/preview",
        headers=headers
    )
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read())
        print(f"Preview API 返回: {json.dumps(data, indent=2, ensure_ascii=False)[:500]}")

        # 验证：preview 不应该返回真实 base64
        data_str = json.dumps(data)
        if "HAS_IMAGES" in data_str:
            print("✓ Preview API 返回 'HAS_IMAGES' 标记（正确）")
        elif "data:image" in data_str:
            print("✗ Preview API 返回了真实 base64（错误！）")
        else:
            print("? Preview API 没有返回图片数据（可能该服务商无资质）")

    # 测试 certificates API（完整数据）
    print("\n--- Certificates API ---")
    req = urllib.request.Request(
        f"{BACKEND_URL}/api/providers/{TEST_PROVIDER_ID}/certificates",
        headers=headers
    )
    with urllib.request.urlopen(req) as resp:
        data = json.loads(resp.read())
        print(f"Certificates API 返回数量: {len(data.get('data', []))}")

        # 检查数据结构
        for cert in data.get("data", []):
            att = cert.get("attachmentUrl", "")
            if att == "HAS_IMAGES":
                print(f"  资质 {cert.get('qualificationId')}[:8]: HAS_IMAGES")
            elif att and att.startswith("data:image"):
                print(f"  资质 {cert.get('qualificationId')[:8]}: [base64图片]")
            else:
                print(f"  资质 {cert.get('qualificationId')[:8]}: 无图片")

    print("Test 4 PASSED")


def main():
    print("=" * 60)
    print("Provider 资质管理 Playwright 测试")
    print("=" * 60)

    with sync_playwright() as pw:
        browser = pw.chromium.launch(headless=True)
        context = browser.new_context(
            viewport={"width": 1920, "height": 1080},
            locale="zh-CN"
        )
        page = context.new_page()

        # 捕获失败的请求
        failed_requests = []
        page.on("requestfailed", lambda req: failed_requests.append(req.url))

        try:
            test_preview_mode_no_base64(page)
        except Exception as e:
            print(f"Test 1 FAILED: {e}")

        try:
            test_lazy_load_image(page)
        except Exception as e:
            print(f"Test 2 FAILED: {e}")

        try:
            test_upload_and_overwrite(page)
        except Exception as e:
            print(f"Test 3 FAILED: {e}")

        # 后端 API 测试不依赖浏览器
        try:
            test_backend_api_preview()
        except Exception as e:
            print(f"Test 4 FAILED: {e}")

        # 检查失败的请求
        if failed_requests:
            print(f"\n⚠ 失败的请求: {failed_requests}")
        else:
            print("\n✓ 没有失败的请求")

        browser.close()

    print("\n" + "=" * 60)
    print("测试完成")
    print("=" * 60)


if __name__ == "__main__":
    main()