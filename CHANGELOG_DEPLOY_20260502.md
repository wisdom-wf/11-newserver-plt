# 部署变更记录 — 2026-05-02/03

## 部署目标

- **服务器**: 43.153.213.134 (Ubuntu 24.04)
- **域名**: https://wisdomdance.cn/jxy/
- **后端**: Spring Boot on port 8080 (systemd-run)
- **前端**: Vue3 SPA (Nginx 静态文件)
- **数据库**: MySQL 8.0.45 (elderly_care)

---

## 一、代码变更 (11 commits: feb08a9 → de9db75)

### 1.1 新增功能

| 文件 | 说明 |
|------|------|
| `PublicSurveyController.java` | 公开评价问卷端点 (GET/POST /public/survey)，无需JWT认证 |
| `JwtAuthenticationInterceptor.java` | EXCLUDE_PATHS 加 `/public/` 白名单 |
| `ServiceEvaluationServiceImpl.java` | surveyUrl 改为 `/jxy/public/survey?token=` |
| `pom.xml` | 新增 zxing-core + zxing-javase 3.5.3（二维码依赖） |
| `dingfeng-work/.env.prod` | VITE_BASE_URL=/jxy/ + VITE_SERVICE_BASE_URL=/jxy |
| `dingfeng-work/evaluation.ts` | 评价API改用 `/public/survey` 公开端点 |

### 1.2 Mapper XML 修复

| 文件 | 修复内容 |
|------|----------|
| `AppointmentMapper.xml` | 列名统一适配DB实际列，改用 SELECT * |
| `ServiceLogMapper.xml` | 表名 t_service_log→service_log，BaseResultMap 全面重写 |
| `ServiceEvaluationMapper.xml` | 3个查询恢复 token/reply/联表字段 |
| `StaffQualificationMapper.xml` | expire_date→expiry_date，删不存在列 |
| `StaffScheduleMapper.xml` | 删 create_by/update_by/update_time |
| `StaffWorkRecordMapper.xml` | id→work_record_id |

### 1.3 生产 SQL 整合

| 文件 | 说明 |
|------|------|
| `sql/production/00_schema.sql` | 从运行中服务器导出的权威DDL（891行，37张表） |
| `sql/production/01_init.sql` | 表结构+种子数据（补provider_id/staff_id/area_id/rating列、admin角色分配） |
| `sql/production/02_area_data.sql` | 延安市区域数据 |
| `sql/production/03_menu_permissions.sql` | 菜单+角色菜单关联 |
| `sql/production/04_permissions.sql` | 权限数据+角色权限关联+admin角色分配 |
| `sql/production/README.sql` | 部署说明 |

---

## 二、服务器配置变更

### 2.1 Nginx (wisdomdance.conf)

```nginx
# 新增：公开API代理（不鉴权）
location /jxy/public/ {
    proxy_pass http://localhost:8080/public/;
    limit_except GET POST { deny all; }
    limit_req zone=public_api burst=20 nodelay;
    add_header Referrer-Policy "no-referrer" always;
}

# 新增：鉴权API代理
location /jxy/api/ {
    proxy_pass http://localhost:8080/api/;
}

# 新增：前端SPA
location /jxy/ {
    root /var/www;
    try_files $uri $uri/ /jxy/index.html;
}
```

nginx.conf 新增：
- `limit_req_zone $binary_remote_addr zone=public_api:10m rate=10r/s;`

### 2.2 数据库

- 创建 `elderly_care` 数据库
- 执行 00_schema.sql → 01_init.sql → 02_area_data.sql → 03_menu_permissions.sql → 04_permissions.sql
- 手动补列（01_init.sql 不完整时）：
  - t_user: provider_id, staff_id, area_id
  - t_provider: rating, rating_count
  - appointment: order_id, order_no
  - t_service_evaluation: evaluation_token, token_status, token_expire_time 等11列
  - service_log: sign_in_time, sign_out_time, medication_given 等14列
  - t_settlement: order_no, provider_name 等15列
  - t_staff: provider_name, nation 等11列
  - t_elder: provider_id, provider_name, area_id
- 重置 admin 密码为 admin123
- 分配 admin→R001 (SUPER_ADMIN) 角色

### 2.3 后端部署

```
jar: /opt/jxy/elderly-care-server-1.0.0.jar
启动: systemd-run --user --unit=jxy-backend -- java -Xms256m -Xmx512m -jar /opt/jxy/elderly-care-server-1.0.0.jar
Java: OpenJDK 17.0.18
MySQL: localhost:3306/elderly_care (root/root)
```

### 2.4 前端部署

```
路径: /var/www/jxy/
来源: dingfeng-work/dist/ (npm run build --mode prod)
```

---

## 三、踩坑记录

| # | 问题 | 根因 | 修复 |
|---|------|------|------|
| 1 | URL `/jxy/api/api/auth/login` | VITE_SERVICE_BASE_URL 设了 /jxy/api，前端自带 /api/ 前缀 | 改为 /jxy |
| 2 | Nginx 代理返回 400 | Python 写配置时 $ 被转义为 \\$ | Python 脚本修复 |
| 3 | 前端缓存旧 JS | Nginx 无 HTML no-cache 头 | 加 Cache-Control |
| 4 | MySQL 密码不对 + 无库 | 服务器首次部署 | 重置密码 + CREATE DB |
| 5 | bcrypt hash 和密码对不上 | 01_init.sql 的 hash 值错误 | 手动更新 |
| 6 | admin 登录后全部 403 | t_user_role 无记录 | INSERT UR001(U001→R001) |
| 7 | 驾驶舱 500 | t_provider 缺 rating 列 | ALTER TABLE |
| 8 | 预约列表 500 | appointment 缺 order_id/order_no | ALTER TABLE |
| 9 | 多表 500 | Entity 字段多于 DDL | 批量 ALTER + 导出 00_schema.sql |
| 10 | jar 启动失败 | 本地 Java 21 vs 服务器 17 | pom.xml target=17 |
| 11 | Nginx alias+try_files 500 | 嵌套 location 块冲突 | 改用 root + 移除嵌套 |
| 12 | SPA 刷新 404 | alias + fallback 路径拼接错误 | 改 root /var/www |
| 13 | 后端端口被占 | 旧 nohup 进程未清理 | kill 旧进程 + 重启 |
| 14 | Mapper XML 列名不匹配 | refactor 改了列名没改 DB | 统一 Mapper 适配 DB |

---

## 四、部署执行顺序（标准化）

```
# 1. 数据库
mysql -uroot -p -e "CREATE DATABASE elderly_care DEFAULT CHARACTER SET utf8mb4"
mysql -uroot -p elderly_care < sql/production/00_schema.sql
mysql -uroot -p elderly_care < sql/production/01_init.sql
mysql -uroot -p elderly_care < sql/production/02_area_data.sql
mysql -uroot -p elderly_care < sql/production/03_menu_permissions.sql
mysql -uroot -p elderly_care < sql/production/04_permissions.sql

# 2. 后端（本地编译）
cd elderly-care-server && mvn clean package -DskipTests
# 上传 jar 到服务器
# 启动: systemd-run --user --unit=jxy-backend -- java -Xms256m -Xmx512m -jar elderly-care-server-1.0.0.jar

# 3. 前端（本地编译）
cd dingfeng-work && npm run build --mode prod
# 上传 dist/ 到 /var/www/jxy/

# 4. Nginx
nginx -t && nginx -s reload

# 5. 验证
curl -sk https://wisdomdance.cn/jxy/
curl -sk -X POST https://wisdomdance.cn/jxy/api/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"admin123"}'
```

---

## 五、默认账户

| 用户 | 密码 | 角色 | 类型 |
|------|------|------|------|
| admin | admin123 | SUPER_ADMIN (R001) | SYSTEM |

---

## 六、文件变更统计

```
 44 files changed, 2620 insertions(+), 635 deletions(-)
 后端新增: PublicSurveyController.java
 后端修改: JwtAuthenticationInterceptor, ServiceEvaluationServiceImpl, UserMapper, pom.xml
 前端修改: evaluation.ts, .env.prod
 SQL新增: 00_schema.sql, 01~04_init.sql, README.sql (production/)
 SQL归档: 20+ 旧SQL移至 archive/
 Mapper修复: 7个XML
```
