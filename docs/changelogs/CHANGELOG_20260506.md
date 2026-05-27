# 变更日志 — 2026-05-06

## 扫码预约功能 + 多项 UI/质量修复

### 🔥 新功能
1. **扫码预约** — 用户扫描二维码在线填写预约信息，管理端自动接收
2. **预约编辑抽屉** — 管理员补充服务类型、预约时间、备注
3. **品牌二维码** — 带标题栏"智慧养老·预约服务"的二维码图片

### 🐛 Bug 修复

#### 预约模块
- 二维码图片不显示（`apiBase` 生产环境为空字符串）
- 二维码 URL 缺域名（`/jxy/appointment-book` → `https://wisdomdance.cn/jxy/appointment-book`）
- 角色不匹配 403（`R_SUPER` → `SUPER_ADMIN`）
- 模态框 URL 显示不美观（已删除）
- 预约列表缺身份证号、地址列
- 编辑预约 DatePicker 兼容"上午/下午"格式
- 移除重复的「取消」按钮

#### 服务商模块
- 创建报"类型不能为空"但前端无提示（加校验规则+错误提示）
- 创建报"address 列不存在"（ALTER TABLE 补齐 4 列）
- 编辑报"audit_status 列不存在"（移除 auditStatus 字段）
- 服务商列表超时 10s（列表查询排除 business_license 大字段）
- 服务商列表只有 ENABLED/DISABLED 两个状态，无审核流程

#### 文件上传
- 资质上传日期格式错误（`LocalDateTime` → `LocalDate`）
- 资质 attachment_url 报 Data too long（varchar→MEDIUMTEXT）
- 营业执照 business_license 报 Data too long（varchar→MEDIUMTEXT）
- 全平台 6 个文件/图片列统一改为 MEDIUMTEXT

#### 前端构建/部署
- NDatePicker 重复导入导致构建失败
- `.env.prod` 缺 `VITE_BASE_URL=/jxy/`
- 部署规范：cp 追加不删除旧 chunk，避免浏览器缓存 404
- Nginx 加 `client_max_body_size 20m` 支持文件上传

### 📁 文件变更

#### 后端（elderly-care-server）
| 文件 | 变更 |
|------|------|
| controller/appointment/AppointmentController.java | +二维码生成/图片/停用 +预约编辑端点 |
| controller/PublicAppointmentController.java | **新建** — 公开预约接口 |
| dto/appointment/AppointmentUpdateDTO.java | **新建** — 预约编辑 DTO |
| dto/appointment/PublicAppointmentSubmitDTO.java | **新建** — 公开预约提交 DTO |
| dto/provider/QualificationCreateDTO.java | LocalDateTime→LocalDate |
| entity/provider/Provider.java | 删除 auditStatus 字段 |
| entity/provider/ProviderQualification.java | LocalDateTime→LocalDate |
| service/appointment/AppointmentService.java | +updateAppointmentInfo |
| service/appointment/impl/AppointmentServiceImpl.java | +getQRCodeImage +submitPublicAppointment +updateAppointmentInfo |
| utils/QRCodeUtil.java | **重写** — 带品牌标识的二维码 |
| vo/appointment/PublicAppointmentVO.java | **新建** |
| vo/provider/QualificationVO.java | LocalDateTime→LocalDate |
| mapper/provider/ProviderMapper.xml | 排除 business_license + 删除 audit_status 映射 |

#### 前端（dingfeng-work）
| 文件 | 变更 |
|------|------|
| .env.prod | +VITE_BASE_URL=/jxy/ +VITE_SERVICE_BASE_URL=/jxy |
| router/routes/index.ts | +预约二维码路由 |
| service/api/appointment.ts | +fetchUpdateAppointment +修复二维码图片URL |
| typings/api/appointment.d.ts | +AppointmentUpdateParams |
| views/appointment/index.vue | 编辑抽屉+二维码弹窗+身份证号列+复制功能+删除取消按钮 |
| views/provider/index.vue | 服务商类型必填+错误提示 |
| views/public/appointment-book/index.vue | **新建** — 公开预约表单 |

#### 部署/运维
| 文件 | 说明 |
|------|------|
| deploy-backend.sh | **新建** — 后端部署脚本 |
| deploy-frontend.sh | **新建** — 前端部署脚本（cp 追加模式）|
| quick-restart.sh | **新建** — 后端重启脚本 |
| Nginx | +client_max_body_size 20m +jxy/public/ 代理 |

#### 数据库 DDL
```sql
-- 服务商表补列
ALTER TABLE t_provider ADD COLUMN address VARCHAR(500);
ALTER TABLE t_provider ADD COLUMN service_category VARCHAR(20);
ALTER TABLE t_provider ADD COLUMN service_areas TEXT;
ALTER TABLE t_provider ADD COLUMN description TEXT;
ALTER TABLE t_provider MODIFY COLUMN business_license MEDIUMTEXT;
ALTER TABLE t_provider MODIFY COLUMN address VARCHAR(1000);

-- 预约表补列
ALTER TABLE appointment ADD COLUMN appointment_token VARCHAR(64);
ALTER TABLE appointment ADD COLUMN token_status ENUM('ACTIVE','DISABLED');
ALTER TABLE appointment ADD COLUMN token_expire_time DATETIME;

-- 全平台文件列升级
ALTER TABLE t_provider_qualification MODIFY COLUMN attachment_url MEDIUMTEXT;
ALTER TABLE t_elder MODIFY COLUMN photo_url MEDIUMTEXT;
ALTER TABLE t_staff MODIFY COLUMN photo_url MEDIUMTEXT;
ALTER TABLE t_staff_qualification MODIFY COLUMN attachment_url MEDIUMTEXT;
ALTER TABLE t_user MODIFY COLUMN avatar MEDIUMTEXT;
```
