-- 为统计类接口添加 PROVIDER/STAFF 角色权限（PermissionInterceptor 需要）
-- 路由：/api/statistics/order GET -> 订单统计（已有 order:list:statistics 但路由不同）

-- 1. 订单统计（statistics/order）
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, is_deleted)
VALUES ('P600100600001', 'statistics:order:list', '订单统计查询', 'BUTTON', 'P600100000000', '/api/statistics/order', 'GET', 1, NULL, 'NORMAL', NOW(), 0);

-- 2. 财务统计（statistics/financial）
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, is_deleted)
VALUES ('PA00104000001', 'statistics:financial:list', '财务统计查询', 'BUTTON', 'PA00100000000', '/api/statistics/financial', 'GET', 1, NULL, 'NORMAL', NOW(), 0);

-- 3. 服务质量统计（statistics/quality）
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, is_deleted)
VALUES ('P800100700001', 'statistics:quality:list', '质量统计查询', 'BUTTON', 'P800100000000', '/api/statistics/quality', 'GET', 1, NULL, 'NORMAL', NOW(), 0);

-- 4. 老人统计（statistics/elder）
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, is_deleted)
VALUES ('P400100500001', 'statistics:elder:list', '老人统计查询', 'BUTTON', 'P400100500000', '/api/statistics/elder', 'GET', 1, NULL, 'NORMAL', NOW(), 0);

-- 5. 服务人员统计（statistics/staff）
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, is_deleted)
VALUES ('P300100500001', 'statistics:staff:list', '员工统计查询', 'BUTTON', 'P300100500000', '/api/statistics/staff', 'GET', 1, NULL, 'NORMAL', NOW(), 0);

-- 6. 服务商统计（statistics/provider）
INSERT IGNORE INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, is_deleted)
VALUES ('P200100600001', 'statistics:provider:list', '服务商统计查询', 'BUTTON', 'P200100600000', '/api/statistics/provider', 'GET', 1, NULL, 'NORMAL', NOW(), 0);

-- 将以上权限分配给 R004（PROVIDER_ADMIN）和 R005（STAFF）
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time)
SELECT CONCAT('RP', UNIX_TIMESTAMP()*1000 + seq), 'R004', 'P600100600001', NOW()
FROM (SELECT 1 AS seq UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) t
WHERE NOT EXISTS (SELECT 1 FROM t_role_permission WHERE permission_id = 'P600100600001');

-- R004
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 1), 'R004', 'P600100600001', NOW());
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 2), 'R004', 'PA00104000001', NOW());
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 3), 'R004', 'P800100700001', NOW());
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 4), 'R004', 'P400100500001', NOW());
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 5), 'R004', 'P300100500001', NOW());
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 6), 'R004', 'P200100600001', NOW());

-- R005
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 7), 'R005', 'P600100600001', NOW());
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 8), 'R005', 'PA00104000001', NOW());
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 9), 'R005', 'P800100700001', NOW());
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 10), 'R005', 'P400100500001', NOW());
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 11), 'R005', 'P300100500001', NOW());
INSERT IGNORE INTO t_role_permission (role_permission_id, role_id, permission_id, create_time) VALUES (CONCAT('RP', UNIX_TIMESTAMP()*1000 + 12), 'R005', 'P200100600001', NOW());

-- 确认结果
SELECT 'R004角色统计权限' AS info, COUNT(*) AS cnt FROM t_role_permission WHERE role_id = 'R004' AND permission_id LIKE 'P%001%';
SELECT 'R005角色统计权限' AS info, COUNT(*) AS cnt FROM t_role_permission WHERE role_id = 'R005' AND permission_id LIKE 'P%001%';
