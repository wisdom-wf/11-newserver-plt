-- ============================================================
-- 财务模块权限补全 (permission_id 格式: P + 12位数字)
-- 由于 permission_id 是 varchar 主键，不能用自增，只能手动指定
-- 策略：每条 INSERT IGNORE 独立执行，一条失败不影响其他
-- ============================================================

-- Step 1: 插入服务定价权限
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900200000000', '服务定价查询', '/api/financial/prices', 'GET', 'BUTTON', 10, 1, NOW(), NOW());
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900200100000', '服务定价新增', '/api/financial/prices', 'POST', 'BUTTON', 11, 1, NOW(), NOW());
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900200200000', '服务定价修改', '/api/financial/prices/{priceId}', 'PUT', 'BUTTON', 12, 1, NOW(), NOW());
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900200300000', '服务定价删除', '/api/financial/prices/{priceId}', 'DELETE', 'BUTTON', 13, 1, NOW(), NOW());

-- Step 2: 插入结算权限
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900300000000', '结算列表', '/api/financial/settlements', 'GET', 'BUTTON', 20, 1, NOW(), NOW());
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900300100000', '结算详情', '/api/financial/settlements/{id}', 'GET', 'BUTTON', 21, 1, NOW(), NOW());
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900300200000', '结算计算', '/api/financial/settlements/calculate', 'POST', 'BUTTON', 22, 1, NOW(), NOW());
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900300300000', '批量结算', '/api/financial/settlements/batch', 'POST', 'BUTTON', 23, 1, NOW(), NOW());
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900300400000', '结算确认', '/api/financial/settlements/{id}/confirm', 'POST', 'BUTTON', 24, 1, NOW(), NOW());

-- Step 3: 插入退款权限
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900400000000', '退款列表', '/api/financial/refunds', 'GET', 'BUTTON', 30, 1, NOW(), NOW());
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900400100000', '退款申请', '/api/financial/refunds', 'POST', 'BUTTON', 31, 1, NOW(), NOW());
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900400200000', '退款审核', '/api/financial/refunds/{id}/audit', 'PUT', 'BUTTON', 32, 1, NOW(), NOW());

-- Step 4: 插入报表权限
INSERT IGNORE INTO t_permission (permission_id, permission_name, permission_url, permission_method, permission_type, sort_order, status, create_time, update_time) VALUES ('P900500000000', '财务报表', '/api/financial/reports', 'GET', 'BUTTON', 40, 1, NOW(), NOW());

-- Step 5: 给 R002(市级管理员) 分配查询权限
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R002', 'P900300000000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R002', 'P900300100000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R002', 'P900400000000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R002', 'P900500000000');

-- Step 6: 给 R003(区县管理员) 分配查询权限
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R003', 'P900300000000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R003', 'P900300100000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R003', 'P900400000000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R003', 'P900500000000');

-- Step 7: 给 R004(服务商管理员) 分配完整财务权限
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900200000000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900200100000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900200200000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900200300000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900300000000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900300100000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900300200000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900300300000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900300400000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900400000000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900400100000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R004', 'P900500000000');

-- Step 8: 给 R005(服务人员) 分配结算查询权限
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R005', 'P900300000000');
INSERT IGNORE INTO t_role_permission (role_id, permission_id) VALUES ('R005', 'P900300100000');
