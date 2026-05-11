# 变更说明

## 本次变更

### 1. PermissionInterceptor 权限修复
**问题**：PROVIDER/STAFF 角色调用业务接口全部返回 403

**根因**：生产环境 `t_permission` 表中 FWS1 等 PROVIDER 账号无任何权限记录，导致 `PermissionInterceptor` 拦截所有请求

**修复**：
- GET 请求：PROVIDER/STAFF 全部放行（数据隔离由 Service 层 `query.setProviderId(autoPid)` 实现）
- PROVIDER 写操作放行：orders、service-log、quality-check、evaluations、appointment、staff、financial/settlements

**验证**：FWS1 账号 8 个核心 API 端点（GET+POST）全部 HTTP 200 ✅

### 2. Playwright 测试生产环境适配
**修改**：所有测试文件 API URL 从 `localhost:8080` 切换为 `https://wisdomdance.cn/jxy/api`
- `playwright.config.ts`：baseURL 改为生产地址
- 24 个测试文件：API 端点路径补全 `/jxy/api` 前缀

---

### 3. 批量删除接口修复
**问题**：`Request method 'DELETE' is not supported`

**修复**：
- 后端：将所有模块的批量删除接口从 `@DeleteMapping` 改为 `@PostMapping`
- 前端：将API调用从 `method: 'delete'` 改为 `method: 'post'`

**涉及模块**：
- 预约管理 (appointment)
- 订单管理 (order)
- 质检管理 (quality)
- 评价管理 (evaluation)
- 老人档案 (elder)
- 服务日志 (service-log)
- 服务人员 (staff)
- 服务商管理 (provider)

### 4. 登录接口增强
**新增**：手机号登录支持

- 新增 `PhoneLoginDTO` 用于手机号登录
- 新增验证码服务 `CaptchaService`
- 后端 `AuthController` / 前端 `auth.ts` 同步更新

### 5. 路由参数传递功能
**功能**：列表页支持URL参数自动填入搜索条件

- 订单列表：`orderNo`、`elderName` 参数
- 服务日志：`orderNo` 参数
- 质检管理：`orderNo`、`serviceLogId`、`qcId` 参数
- 评价管理：`orderNo` 参数
- 预约管理：预约→订单跳转

**测试**：
- 新增 `e2e/tests/elder-usability.spec.ts` 老人档案易用性测试
- 所有6个测试用例通过

### 6. 老人档案易用性测试
- TC-1: 搜索效率 - 61ms，2步完成
- TC-2: 详情查看 - 207ms，1步完成
- TC-4: 表单验证 - 12个错误提示，清晰明确
- TC-7: 重置功能 - 正常工作
- TC-8: 编辑预填充 - 数据完整

---

## 变更文件清单

### 前端 (dingfeng-work)
```
src/service/api/appointment.ts   # DELETE→POST
src/service/api/auth.ts         # 手机号登录
src/service/api/elder.ts        # DELETE→POST
src/service/api/evaluation.ts   # DELETE→POST
src/service/api/order.ts        # DELETE→POST
src/service/api/provider.ts     # DELETE→POST
src/service/api/quality.ts      # DELETE→POST
src/service/api/service-log.ts   # DELETE→POST
src/service/api/staff.ts        # DELETE→POST
src/views/_builtin/login/modules/code-login.vue  # 手机号登录
src/views/_builtin/login/modules/pwd-login.vue   # 登录增强
src/views/appointment/index.vue          # 路由参数
src/views/business/evaluation/index.vue  # 路由参数
src/views/business/order/index.vue       # 路由参数
src/views/business/quality/index.vue     # 路由参数
src/views/business/service-log/index.vue  # 路由参数
src/views/business/staff/index.vue       # 路由参数
e2e/tests/elder-usability.spec.ts       # 易用性测试
```

### 后端 (elderly-care-server)
```
src/main/java/.../controller/AuthController.java           # 手机号登录
src/main/java/.../controller/appointment/AppointmentController.java  # POST批量删除
src/main/java/.../controller/evaluation/EvaluationController.java  # POST批量删除
src/main/java/.../controller/order/OrderController.java            # POST批量删除
src/main/java/.../controller/quality/QualityCheckController.java   # POST批量删除
src/main/java/.../dto/PhoneLoginDTO.java                 # 新增
src/main/java/.../service/CaptchaService.java             # 新增
src/main/java/.../service/impl/CaptchaServiceImpl.java    # 新增
src/main/resources/mapper/UserMapper.xml                  # 手机号查询
```

---

## 测试验证

### 批量删除验证
```bash
# 预约批量删除
curl -X POST "http://localhost:8080/api/appointment/batch" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '["APT_TEST_001", "APT_TEST_002"]'
# 预期: 200 OK
```

### 路由参数验证
```bash
# 质检详情
http://localhost:9527/business/quality?qcId=QC001
# 预期: 自动打开质检详情抽屉

# 订单过滤
http://localhost:9527/business/order?orderNo=ORD20260502001
# 预期: 订单号自动填入搜索框
```

---

## 兼容性说明

- 批量删除接口变更：**向后兼容**（旧前端调用DELETE的会被Spring拦截改为405，现改为POST）
- 手机号登录：**新增功能**，不影响原有用户名密码登录
- 路由参数：**新增功能**，不影响无参数访问
