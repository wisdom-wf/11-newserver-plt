-- ============================================
-- 补充权限 URL 修正和缺失按钮权限
-- 修正：appointment/quality-check/service-log 的 URL 与后端 Controller 一致
-- 新增：所有缺失的 API 按钮权限
-- ============================================
USE elderly_care;
SET NAMES utf8mb4;

-- ============================================
-- 1. 修正 appointment 列表权限 URL
-- ============================================
-- appointment:list:query 的 URL 应该是 /api/appointment (GET @GetMapping(""))
UPDATE t_permission
SET permission_url = '/api/appointment'
WHERE permission_code = 'appointment:list:query' AND permission_type = 'BUTTON';

-- 实际上后端 GET 列表是 /api/appointment/list，分开处理
UPDATE t_permission
SET permission_url = '/api/appointment/list'
WHERE permission_code = 'appointment:list:query' AND permission_type = 'BUTTON';

-- ============================================
-- 2. 修正 quality-check 列表权限 URL
-- ============================================
UPDATE t_permission
SET permission_url = '/api/quality-check'
WHERE permission_code = 'quality:list:query' AND permission_type = 'BUTTON';

-- ============================================
-- 3. 修正 service-log 列表权限 URL
-- ============================================
UPDATE t_permission
SET permission_url = '/api/service-log'
WHERE permission_code = 'service-log:list:query' AND permission_type = 'BUTTON';

-- ============================================
-- 4. 新增 appointment 缺失的按钮权限
-- ============================================
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
-- 统计
('P500100800000', 'appointment:list:statistics', '预约统计', 'BUTTON', 'P500100000000', '/api/appointment/statistics', 'GET', 8, NULL, 'NORMAL', NOW(), 0),
-- 电话查询
('P500100900000', 'appointment:list:phone', '电话查询', 'BUTTON', 'P500100000000', '/api/appointment/phone', 'GET', 9, NULL, 'NORMAL', NOW(), 0),
-- 时间轴
('P500101000000', 'appointment:list:timeline', '预约时间轴', 'BUTTON', 'P500100000000', '/api/appointment/*/timeline', 'GET', 10, NULL, 'NORMAL', NOW(), 0),
-- 分配/派单（注意与 appointment:list:dispatch 区分）
('P500101100000', 'appointment:list:assign', '分配服务', 'BUTTON', 'P500100000000', '/api/appointment/*/assign', 'PUT', 11, NULL, 'NORMAL', NOW(), 0),
-- 作废
('P500101200000', 'appointment:list:invalidate', '作废预约', 'BUTTON', 'P500100000000', '/api/appointment/*/invalidate', 'PUT', 12, NULL, 'NORMAL', NOW(), 0),
-- 导入
('P500101300000', 'appointment:list:import', '导入预约', 'BUTTON', 'P500100000000', '/api/appointment/import', 'POST', 13, NULL, 'NORMAL', NOW(), 0),
-- 模板下载
('P500101400000', 'appointment:list:template', '下载模板', 'BUTTON', 'P500100000000', '/api/appointment/template', 'GET', 14, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 5. 新增 quality-check 缺失的按钮权限
-- ============================================
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
-- 按订单查质检
('P800100400000', 'quality:list:order', '按订单查询', 'BUTTON', 'P800100000000', '/api/quality-check/order/*', 'GET', 4, NULL, 'NORMAL', NOW(), 0),
-- 整改
('P800100500000', 'quality:list:rectify', '整改', 'BUTTON', 'P800100000000', '/api/quality-check/*/rectify', 'PUT', 5, NULL, 'NORMAL', NOW(), 0),
-- 复核
('P800100600000', 'quality:list:recheck', '复核', 'BUTTON', 'P800100000000', '/api/quality-check/*/recheck', 'PUT', 6, NULL, 'NORMAL', NOW(), 0),
-- 统计
('P800100700000', 'quality:list:statistics', '质检统计', 'BUTTON', 'P800100000000', '/api/quality-check/statistics', 'GET', 7, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 6. 新增 service-log 缺失的按钮权限
-- ============================================
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
-- 按订单查日志
('P700100400000', 'service-log:list:order', '按订单查询', 'BUTTON', 'P700100000000', '/api/service-log/order/*', 'GET', 4, NULL, 'NORMAL', NOW(), 0),
-- 异常
('P700100500000', 'service-log:list:anomaly', '标记异常', 'BUTTON', 'P700100000000', '/api/service-log/*/anomaly', 'PUT', 5, NULL, 'NORMAL', NOW(), 0),
-- 提交审核
('P700100600000', 'service-log:list:submitReview', '提交审核', 'BUTTON', 'P700100000000', '/api/service-log/*/submit-review', 'PUT', 6, NULL, 'NORMAL', NOW(), 0),
-- 统计
('P700100700000', 'service-log:list:statistics', '服务统计', 'BUTTON', 'P700100000000', '/api/service-log/statistics', 'GET', 7, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 7. 新增 evaluation 缺失的按钮权限
-- ============================================
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
-- 按订单查评价
('P900100300000', 'evaluation:list:order', '按订单查询', 'BUTTON', 'P900100000000', '/api/evaluations/order/*', 'GET', 3, NULL, 'NORMAL', NOW(), 0),
-- 统计
('P900100300001', 'evaluation:list:statistics', '评价统计', 'BUTTON', 'P900100000000', '/api/evaluations/statistics', 'GET', 3, NULL, 'NORMAL', NOW(), 0),
-- 满意度查询
('P900100400000', 'evaluation:list:feedback', '满意度查询', 'BUTTON', 'P900100000000', '/api/evaluations/feedback', 'GET', 4, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 8. 新增 orders 缺失的按钮权限
-- ============================================
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
-- 统计
('P600100600000', 'order:list:statistics', '订单统计', 'BUTTON', 'P600100000000', '/api/orders/statistics', 'GET', 6, NULL, 'NORMAL', NOW(), 0),
-- 订单服务记录
('P600100700000', 'order:list:serviceRecords', '服务记录', 'BUTTON', 'P600100000000', '/api/orders/service-records', 'GET', 7, NULL, 'NORMAL', NOW(), 0),
-- 按预约查订单
('P600100800000', 'order:list:byAppointment', '按预约查订单', 'BUTTON', 'P600100000000', '/api/orders/appointment/*', 'GET', 8, NULL, 'NORMAL', NOW(), 0),
-- 接收订单
('P600100900000', 'order:list:receive', '接收订单', 'BUTTON', 'P600100000000', '/api/orders/*/receive', 'PUT', 9, NULL, 'NORMAL', NOW(), 0),
-- 开始服务
('P600101000000', 'order:list:start', '开始服务', 'BUTTON', 'P600100000000', '/api/orders/*/start', 'PUT', 10, NULL, 'NORMAL', NOW(), 0),
-- 完成订单
('P600101100000', 'order:list:complete', '完成订单', 'BUTTON', 'P600100000000', '/api/orders/*/complete', 'PUT', 11, NULL, 'NORMAL', NOW(), 0),
-- 拒绝订单
('P600101200000', 'order:list:reject', '拒绝订单', 'BUTTON', 'P600100000000', '/api/orders/*/reject', 'PUT', 12, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 9. 新增 provider 缺失的按钮权限
-- ============================================
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
-- 服务商统计
('P200100600000', 'provider:list:statistics', '服务商统计', 'BUTTON', 'P200100000000', '/api/statistics/provider', 'GET', 6, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 10. 新增 elder 缺失的按钮权限
-- ============================================
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
-- 老人统计
('P400100500000', 'elder:list:statistics', '老人统计', 'BUTTON', 'P400100000000', '/api/statistics/elder', 'GET', 5, NULL, 'NORMAL', NOW(), 0),
-- 老人下拉选项
('P400100600000', 'elder:list:options', '老人下拉', 'BUTTON', 'P400100000000', '/api/elders/options', 'GET', 6, NULL, 'NORMAL', NOW(), 0),
-- 身份证查询老人
('P400100700000', 'elder:list:idCard', '身份证查询', 'BUTTON', 'P400100000000', '/api/elders/idCard/*', 'GET', 7, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 11. 新增 staff 缺失的按钮权限
-- ============================================
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
-- 员工统计
('P300100500000', 'staff:list:statistics', '员工统计', 'BUTTON', 'P300100000000', '/api/statistics/staff', 'GET', 5, NULL, 'NORMAL', NOW(), 0),
-- 员工下拉
('P300100600000', 'staff:list:select', '员工下拉', 'BUTTON', 'P300100000000', '/api/staff/select', 'GET', 6, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 12. 新增 financial 缺失的按钮权限
-- ============================================
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
-- 财务统计
('PA00104000000', 'financial:price:statistics', '财务统计', 'BUTTON', 'PA00100000000', '/api/statistics/financial', 'GET', 4, NULL, 'NORMAL', NOW(), 0),
-- 结算计算
('PA00203000000', 'financial:settlement:calculate', '结算计算', 'BUTTON', 'PA00200000000', '/api/financial/settlements/calculate', 'POST', 3, NULL, 'NORMAL', NOW(), 0),
-- 批量结算
('PA00204000000', 'financial:settlement:batch', '批量结算', 'BUTTON', 'PA00200000000', '/api/financial/settlements/batch', 'POST', 4, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 13. 新增 cockpit 所有子端点权限（除了已添加的4个）
-- ============================================
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('PC00100500000', 'cockpit:overview:serviceDistribution', '服务分布', 'BUTTON', 'PC00100000000', '/api/cockpit/serviceDistribution', 'GET', 5, NULL, 'NORMAL', NOW(), 0),
('PC00100600000', 'cockpit:overview:areaDistribution', '区域分布', 'BUTTON', 'PC00100000000', '/api/cockpit/areaDistribution', 'GET', 6, NULL, 'NORMAL', NOW(), 0),
('PC00100700000', 'cockpit:overview:satisfactionDistribution', '满意度分布', 'BUTTON', 'PC00100000000', '/api/cockpit/satisfactionDistribution', 'GET', 7, NULL, 'NORMAL', NOW(), 0),
('PC00100800000', 'cockpit:overview:qualityDistribution', '质量分布', 'BUTTON', 'PC00100000000', '/api/cockpit/qualityDistribution', 'GET', 8, NULL, 'NORMAL', NOW(), 0),
('PC00100900000', 'cockpit:overview:financialTrend', '财务趋势', 'BUTTON', 'PC00100000000', '/api/cockpit/financialTrend', 'GET', 9, NULL, 'NORMAL', NOW(), 0),
('PC00101000000', 'cockpit:overview:ageDistribution', '年龄分布', 'BUTTON', 'PC00100000000', '/api/cockpit/ageDistribution', 'GET', 10, NULL, 'NORMAL', NOW(), 0),
('PC00101100000', 'cockpit:overview:careLevelDistribution', '护理等级分布', 'BUTTON', 'PC00100000000', '/api/cockpit/careLevelDistribution', 'GET', 11, NULL, 'NORMAL', NOW(), 0),
('PC00101200000', 'cockpit:overview:realtimeOrders', '实时订单', 'BUTTON', 'PC00100000000', '/api/cockpit/realtimeOrders', 'GET', 12, NULL, 'NORMAL', NOW(), 0),
('PC00101300000', 'cockpit:overview:warnings', '预警信息', 'BUTTON', 'PC00100000000', '/api/cockpit/warnings', 'GET', 13, NULL, 'NORMAL', NOW(), 0),
('PC00101400000', 'cockpit:overview:heatMapData', '热力图数据', 'BUTTON', 'PC00100000000', '/api/cockpit/heatMapData', 'GET', 14, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 14. 分配新增权限给超级管理员 R001
-- ============================================
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time)
SELECT CONCAT('RP1_', permission_id), 'R001', permission_id, NOW()
FROM t_permission
WHERE permission_code IN (
  'appointment:list:statistics', 'appointment:list:phone', 'appointment:list:timeline',
  'appointment:list:assign', 'appointment:list:invalidate', 'appointment:list:import',
  'appointment:list:template',
  'quality:list:order', 'quality:list:rectify', 'quality:list:recheck', 'quality:list:statistics',
  'service-log:list:order', 'service-log:list:anomaly', 'service-log:list:submitReview', 'service-log:list:statistics',
  'evaluation:list:order', 'evaluation:list:statistics', 'evaluation:list:feedback',
  'order:list:statistics', 'order:list:serviceRecords', 'order:list:byAppointment',
  'order:list:receive', 'order:list:start', 'order:list:complete', 'order:list:reject',
  'provider:list:statistics',
  'elder:list:statistics', 'elder:list:options', 'elder:list:idCard',
  'staff:list:statistics', 'staff:list:select',
  'financial:price:statistics', 'financial:settlement:calculate', 'financial:settlement:batch',
  'cockpit:overview:serviceDistribution', 'cockpit:overview:areaDistribution',
  'cockpit:overview:satisfactionDistribution', 'cockpit:overview:qualityDistribution',
  'cockpit:overview:financialTrend', 'cockpit:overview:ageDistribution',
  'cockpit:overview:careLevelDistribution', 'cockpit:overview:realtimeOrders',
  'cockpit:overview:warnings', 'cockpit:overview:heatMapData'
);

SELECT '权限URL修正和补充完成!' AS result;
