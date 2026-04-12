-- 菜单权限数据初始化脚本
USE elderly_care;
SET NAMES utf8mb4;

-- 删除旧数据
DELETE FROM t_role_permission WHERE role_id = 'R001';
DELETE FROM t_user_role WHERE user_id = 'U001';
DELETE FROM t_permission WHERE permission_id LIKE 'P%';

-- ============================================
-- 1. 系统管理模块 (system)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('P100000000000', 'system', '系统管理', 'MENU', '0', NULL, NULL, 1, 'ri/settings-3-line', 'NORMAL', NOW(), 0),
('P100100000000', 'system:user', '用户管理', 'MENU', 'P100000000000', '/system/user', NULL, 1, 'ri/user-settings-line', 'NORMAL', NOW(), 0),
('P100100100000', 'system:user:list', '用户列表', 'BUTTON', 'P100100000000', '/api/system/users', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P100100200000', 'system:user:add', '新增用户', 'BUTTON', 'P100100000000', '/api/system/users', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('P100100300000', 'system:user:edit', '编辑用户', 'BUTTON', 'P100100000000', '/api/system/users', 'PUT', 3, NULL, 'NORMAL', NOW(), 0),
('P100100400000', 'system:user:delete', '删除用户', 'BUTTON', 'P100100000000', '/api/system/users', 'DELETE', 4, NULL, 'NORMAL', NOW(), 0),
('P100100500000', 'system:user:assignRoles', '分配角色', 'BUTTON', 'P100100000000', '/api/system/users/*/roles', 'PUT', 5, NULL, 'NORMAL', NOW(), 0),
('P100200000000', 'system:role', '角色管理', 'MENU', 'P100000000000', '/system/role', NULL, 2, 'ri/user-star-line', 'NORMAL', NOW(), 0),
('P100200100000', 'system:role:list', '角色列表', 'BUTTON', 'P100200000000', '/api/system/roles', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P100200200000', 'system:role:add', '新增角色', 'BUTTON', 'P100200000000', '/api/system/roles', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('P100200300000', 'system:role:edit', '编辑角色', 'BUTTON', 'P100200000000', '/api/system/roles', 'PUT', 3, NULL, 'NORMAL', NOW(), 0),
('P100200400000', 'system:role:delete', '删除角色', 'BUTTON', 'P100200000000', '/api/system/roles', 'DELETE', 4, NULL, 'NORMAL', NOW(), 0),
('P100200500000', 'system:role:assignPerms', '分配权限', 'BUTTON', 'P100200000000', '/api/system/roles/*/permissions', 'PUT', 5, NULL, 'NORMAL', NOW(), 0),
('P100300000000', 'system:menu', '菜单管理', 'MENU', 'P100000000000', '/system/menu', NULL, 3, 'ri/menu-line', 'NORMAL', NOW(), 0),
('P100300100000', 'system:menu:list', '菜单列表', 'BUTTON', 'P100300000000', '/api/system/permissions/tree', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P100300200000', 'system:menu:add', '新增菜单', 'BUTTON', 'P100300000000', '/api/system/permissions', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('P100300300000', 'system:menu:edit', '编辑菜单', 'BUTTON', 'P100300000000', '/api/system/permissions', 'PUT', 3, NULL, 'NORMAL', NOW(), 0),
('P100300400000', 'system:menu:delete', '删除菜单', 'BUTTON', 'P100300000000', '/api/system/permissions', 'DELETE', 4, NULL, 'NORMAL', NOW(), 0),
('P100400000000', 'system:area', '区域管理', 'MENU', 'P100000000000', '/system/area', NULL, 4, 'ri/map-pin-line', 'NORMAL', NOW(), 0),
('P100400100000', 'system:area:list', '区域列表', 'BUTTON', 'P100400000000', '/api/config/areas/tree', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P100400200000', 'system:area:add', '新增区域', 'BUTTON', 'P100400000000', '/api/config/areas', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('P100400300000', 'system:area:edit', '编辑区域', 'BUTTON', 'P100400000000', '/api/config/areas', 'PUT', 3, NULL, 'NORMAL', NOW(), 0),
('P100400400000', 'system:area:delete', '删除区域', 'BUTTON', 'P100400000000', '/api/config/areas', 'DELETE', 4, NULL, 'NORMAL', NOW(), 0),
('P100500000000', 'system:dict', '字典管理', 'MENU', 'P100000000000', '/system/dict', NULL, 5, 'ri/book-open-line', 'NORMAL', NOW(), 0),
('P100600000000', 'system:log', '操作日志', 'MENU', 'P100000000000', '/system/log', NULL, 6, 'ri-file-list-3-line', 'NORMAL', NOW(), 0);

-- ============================================
-- 2. 服务商管理 (provider)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('P200000000000', 'provider', '服务商管理', 'MENU', '0', NULL, NULL, 2, 'ri-briefcase-line', 'NORMAL', NOW(), 0),
('P200100000000', 'provider:list', '服务商列表', 'MENU', 'P200000000000', '/provider', NULL, 1, NULL, 'NORMAL', NOW(), 0),
('P200100100000', 'provider:list:query', '查询列表', 'BUTTON', 'P200100000000', '/api/providers', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P200100200000', 'provider:list:add', '新增服务商', 'BUTTON', 'P200100000000', '/api/providers', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('P200100300000', 'provider:list:edit', '编辑服务商', 'BUTTON', 'P200100000000', '/api/providers', 'PUT', 3, NULL, 'NORMAL', NOW(), 0),
('P200100400000', 'provider:list:delete', '删除服务商', 'BUTTON', 'P200100000000', '/api/providers', 'DELETE', 4, NULL, 'NORMAL', NOW(), 0),
('P200100500000', 'provider:list:audit', '审核服务商', 'BUTTON', 'P200100000000', '/api/providers/*/audit', 'PUT', 5, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 3. 服务人员管理 (staff)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('P300000000000', 'staff', '服务人员管理', 'MENU', '0', NULL, NULL, 3, 'ri-team-line', 'NORMAL', NOW(), 0),
('P300100000000', 'staff:list', '服务人员列表', 'MENU', 'P300000000000', '/staff', NULL, 1, NULL, 'NORMAL', NOW(), 0),
('P300100100000', 'staff:list:query', '查询列表', 'BUTTON', 'P300100000000', '/api/staff', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P300100200000', 'staff:list:add', '新增人员', 'BUTTON', 'P300100000000', '/api/staff', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('P300100300000', 'staff:list:edit', '编辑人员', 'BUTTON', 'P300100000000', '/api/staff', 'PUT', 3, NULL, 'NORMAL', NOW(), 0),
('P300100400000', 'staff:list:delete', '删除人员', 'BUTTON', 'P300100000000', '/api/staff', 'DELETE', 4, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 4. 老人客户管理 (elder)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('P400000000000', 'elder', '老人客户管理', 'MENU', '0', NULL, NULL, 4, 'ri-parent-line', 'NORMAL', NOW(), 0),
('P400100000000', 'elder:list', '老人列表', 'MENU', 'P400000000000', '/elder', NULL, 1, NULL, 'NORMAL', NOW(), 0),
('P400100100000', 'elder:list:query', '查询列表', 'BUTTON', 'P400100000000', '/api/elders', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P400100200000', 'elder:list:add', '新增老人', 'BUTTON', 'P400100000000', '/api/elders', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('P400100300000', 'elder:list:edit', '编辑老人', 'BUTTON', 'P400100000000', '/api/elders', 'PUT', 3, NULL, 'NORMAL', NOW(), 0),
('P400100400000', 'elder:list:delete', '删除老人', 'BUTTON', 'P400100000000', '/api/elders', 'DELETE', 4, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 5. 预约管理 (appointment)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('P500000000000', 'appointment', '预约管理', 'MENU', '0', NULL, NULL, 5, 'ri-calendar-check-line', 'NORMAL', NOW(), 0),
('P500100000000', 'appointment:list', '预约列表', 'MENU', 'P500000000000', '/appointment', NULL, 1, NULL, 'NORMAL', NOW(), 0),
('P500100100000', 'appointment:list:query', '查询列表', 'BUTTON', 'P500100000000', '/api/appointments', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P500100200000', 'appointment:list:add', '新增预约', 'BUTTON', 'P500100000000', '/api/appointments', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('P500100300000', 'appointment:list:edit', '编辑预约', 'BUTTON', 'P500100000000', '/api/appointments', 'PUT', 3, NULL, 'NORMAL', NOW(), 0),
('P500100400000', 'appointment:list:delete', '删除预约', 'BUTTON', 'P500100000000', '/api/appointments', 'DELETE', 4, NULL, 'NORMAL', NOW(), 0),
('P500100500000', 'appointment:list:import', '导入预约', 'BUTTON', 'P500100000000', '/api/appointments/import', 'POST', 5, NULL, 'NORMAL', NOW(), 0),
('P500100600000', 'appointment:list:confirm', '确认预约', 'BUTTON', 'P500100000000', '/api/appointments/*/confirm', 'PUT', 6, NULL, 'NORMAL', NOW(), 0),
('P500100700000', 'appointment:list:dispatch', '分配服务', 'BUTTON', 'P500100000000', '/api/appointments/*/dispatch', 'PUT', 7, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 6. 订单管理 (order)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('P600000000000', 'order', '订单管理', 'MENU', '0', NULL, NULL, 6, 'ri-file-list-2-line', 'NORMAL', NOW(), 0),
('P600100000000', 'order:list', '订单列表', 'MENU', 'P600000000000', '/order', NULL, 1, NULL, 'NORMAL', NOW(), 0),
('P600100100000', 'order:list:query', '查询列表', 'BUTTON', 'P600100000000', '/api/orders', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P600100200000', 'order:list:add', '创建订单', 'BUTTON', 'P600100000000', '/api/orders', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('P600100300000', 'order:list:edit', '编辑订单', 'BUTTON', 'P600100000000', '/api/orders', 'PUT', 3, NULL, 'NORMAL', NOW(), 0),
('P600100400000', 'order:list:cancel', '取消订单', 'BUTTON', 'P600100000000', '/api/orders/*/cancel', 'PUT', 4, NULL, 'NORMAL', NOW(), 0),
('P600100500000', 'order:list:dispatch', '派单', 'BUTTON', 'P600100000000', '/api/orders/*/dispatch', 'PUT', 5, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 7. 服务日志 (service-log)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('P700000000000', 'service-log', '服务日志', 'MENU', '0', NULL, NULL, 7, 'ri-file-chart-line', 'NORMAL', NOW(), 0),
('P700100000000', 'service-log:list', '日志列表', 'MENU', 'P700000000000', '/service-log', NULL, 1, NULL, 'NORMAL', NOW(), 0),
('P700100100000', 'service-log:list:query', '查询列表', 'BUTTON', 'P700100000000', '/api/service-logs', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P700100200000', 'service-log:list:add', '提交日志', 'BUTTON', 'P700100000000', '/api/service-logs', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('P700100300000', 'service-log:list:edit', '编辑日志', 'BUTTON', 'P700100000000', '/api/service-logs', 'PUT', 3, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 8. 服务质检 (quality)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('P800000000000', 'quality', '服务质检', 'MENU', '0', NULL, NULL, 8, 'ri-shield-check-line', 'NORMAL', NOW(), 0),
('P800100000000', 'quality:list', '质检列表', 'MENU', 'P800000000000', '/quality', NULL, 1, NULL, 'NORMAL', NOW(), 0),
('P800100100000', 'quality:list:query', '查询列表', 'BUTTON', 'P800100000000', '/api/quality-checks', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P800100200000', 'quality:list:add', '新增质检', 'BUTTON', 'P800100000000', '/api/quality-checks', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('P800100300000', 'quality:list:edit', '编辑质检', 'BUTTON', 'P800100000000', '/api/quality-checks', 'PUT', 3, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 9. 满意度评价 (evaluation)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('P900000000000', 'evaluation', '满意度评价', 'MENU', '0', NULL, NULL, 9, 'ri-star-smile-line', 'NORMAL', NOW(), 0),
('P900100000000', 'evaluation:list', '评价列表', 'MENU', 'P900000000000', '/evaluation', NULL, 1, NULL, 'NORMAL', NOW(), 0),
('P900100100000', 'evaluation:list:query', '查询列表', 'BUTTON', 'P900100000000', '/api/evaluations', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('P900100200000', 'evaluation:list:reply', '回复评价', 'BUTTON', 'P900100000000', '/api/evaluations/*/reply', 'PUT', 2, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 10. 财务结算 (financial)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('PA00000000000', 'financial', '财务结算', 'MENU', '0', NULL, NULL, 10, 'ri-wallet-3-line', 'NORMAL', NOW(), 0),
('PA00100000000', 'financial:price', '定价管理', 'MENU', 'PA00000000000', '/financial/prices', NULL, 1, NULL, 'NORMAL', NOW(), 0),
('PA00101000000', 'financial:price:query', '查询定价', 'BUTTON', 'PA00100000000', '/api/financial/prices', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('PA00102000000', 'financial:price:add', '新增定价', 'BUTTON', 'PA00100000000', '/api/financial/prices', 'POST', 2, NULL, 'NORMAL', NOW(), 0),
('PA00103000000', 'financial:price:edit', '编辑定价', 'BUTTON', 'PA00100000000', '/api/financial/prices', 'PUT', 3, NULL, 'NORMAL', NOW(), 0),
('PA00200000000', 'financial:settlement', '结算管理', 'MENU', 'PA00000000000', '/financial/settlements', NULL, 2, NULL, 'NORMAL', NOW(), 0),
('PA00201000000', 'financial:settlement:query', '查询结算', 'BUTTON', 'PA00200000000', '/api/financial/settlements', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('PA00202000000', 'financial:settlement:confirm', '确认结算', 'BUTTON', 'PA00200000000', '/api/financial/settlements/*/confirm', 'PUT', 2, NULL, 'NORMAL', NOW(), 0),
('PA00300000000', 'financial:refund', '退款管理', 'MENU', 'PA00000000000', '/financial/refunds', NULL, 3, NULL, 'NORMAL', NOW(), 0),
('PA00301000000', 'financial:refund:query', '查询退款', 'BUTTON', 'PA00300000000', '/api/financial/refunds', 'GET', 1, NULL, 'NORMAL', NOW(), 0),
('PA00302000000', 'financial:refund:audit', '审核退款', 'BUTTON', 'PA00300000000', '/api/financial/refunds/*/audit', 'PUT', 2, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 11. 数据统计 (statistics)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('PB00000000000', 'statistics', '数据统计', 'MENU', '0', NULL, NULL, 11, 'ri-bar-chart-box-line', 'NORMAL', NOW(), 0),
('PB00100000000', 'statistics:order', '订单统计', 'MENU', 'PB00000000000', '/statistics/orders', NULL, 1, NULL, 'NORMAL', NOW(), 0),
('PB00200000000', 'statistics:service', '服务统计', 'MENU', 'PB00000000000', '/statistics/service', NULL, 2, NULL, 'NORMAL', NOW(), 0),
('PB00300000000', 'statistics:financial', '财务统计', 'MENU', 'PB00000000000', '/statistics/financial', NULL, 3, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 12. 驾驶舱 (cockpit)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('PC00000000000', 'cockpit', '驾驶舱', 'MENU', '0', NULL, NULL, 12, 'ri-dashboard-line', 'NORMAL', NOW(), 0),
('PC00100000000', 'cockpit:overview', '总览', 'MENU', 'PC00000000000', '/cockpit', NULL, 1, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 13. 社区管理 (community)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('PD00000000000', 'community', '社区管理', 'MENU', '0', NULL, NULL, 13, 'ri-community-line', 'NORMAL', NOW(), 0),
('PD00100000000', 'community:list', '社区列表', 'MENU', 'PD00000000000', '/community', NULL, 1, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 14. 设备管理 (device)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('PE00000000000', 'device', '设备管理', 'MENU', '0', NULL, NULL, 14, 'ri-device-line', 'NORMAL', NOW(), 0),
('PE00100000000', 'device:list', '设备列表', 'MENU', 'PE00000000000', '/device', NULL, 1, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 15. 消息管理 (message)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('PF00000000000', 'message', '消息管理', 'MENU', '0', NULL, NULL, 15, 'ri-message-3-line', 'NORMAL', NOW(), 0),
('PF00100000000', 'message:list', '消息列表', 'MENU', 'PF00000000000', '/message', NULL, 1, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 16. 档案管理 (archive)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('PG00000000000', 'archive', '档案管理', 'MENU', '0', NULL, NULL, 16, 'ri-folder-history-line', 'NORMAL', NOW(), 0),
('PG00100000000', 'archive:list', '档案列表', 'MENU', 'PG00000000000', '/archive', NULL, 1, NULL, 'NORMAL', NOW(), 0);

-- ============================================
-- 17. 运营分析 (operation)
-- ============================================
INSERT INTO t_permission (permission_id, permission_code, permission_name, permission_type, parent_id, permission_url, permission_method, sort_order, icon, status, create_time, deleted) VALUES
('PH00000000000', 'operation', '运营分析', 'MENU', '0', NULL, NULL, 17, 'ri-line-chart-line', 'NORMAL', NOW(), 0),
('PH00100000000', 'operation:analysis', '运营分析', 'MENU', 'PH00000000000', '/operation', NULL, 1, NULL, 'NORMAL', NOW(), 0);

-- 分配所有权限给超级管理员角色
INSERT INTO t_role_permission (role_permission_id, role_id, permission_id, create_time)
SELECT CONCAT('RP', permission_id), 'R001', permission_id, NOW()
FROM t_permission;

-- 关联管理员用户和超级管理员角色
INSERT INTO t_user_role (user_role_id, user_id, role_id, create_time)
VALUES ('UR001', 'U001', 'R001', NOW());

SELECT '菜单权限数据初始化完成!' AS result;
