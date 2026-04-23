USE elderly_care;
SET NAMES utf8mb4;

-- =============================================
-- 菜单种子数据
-- 基于 dingfeng-work/src/router/elegant/routes.ts 的静态路由配置
-- =============================================

-- 顶级菜单
INSERT INTO t_menu (menu_id, menu_code, menu_name, parent_id, menu_type, path, component, icon, order_num, is_hidden, status, deleted) VALUES
('M001', 'business', '业务管理', NULL, 'MENU', '/business', 'layout.base', 'mdi:briefcase-outline', 1, 0, 'NORMAL', 0),
('M002', 'system', '系统管理', NULL, 'MENU', '/system', 'layout.base', 'mdi:cog-outline', 9, 0, 'NORMAL', 0);

-- 业务管理子菜单
-- 注意：menu_code 必须与 src/router/elegant/imports.ts 中的 views 键名一致
INSERT INTO t_menu (menu_id, menu_code, menu_name, parent_id, menu_type, path, component, icon, order_num, is_hidden, status, deleted) VALUES
('M101', 'provider', '服务商管理', 'M001', 'MENU', '/provider', 'view.provider', 'mdi:domain', 1, 0, 'NORMAL', 0),
('M102', 'business_staff', '服务人员', 'M001', 'MENU', '/business/staff', 'view.business_staff', 'mdi:account-tie', 2, 0, 'NORMAL', 0),
('M103', 'business_elder', '客户管理', 'M001', 'MENU', '/business/elder', 'view.business_elder', 'mdi:account-outline', 3, 0, 'NORMAL', 0),
('M104', 'appointment', '预约管理', 'M001', 'MENU', '/appointment', 'view.appointment', 'mdi:calendar-clock', 4, 0, 'NORMAL', 0),
('M105', 'business_order', '订单管理', 'M001', 'MENU', '/business/order', 'view.business_order', 'mdi:clipboard-list-outline', 5, 0, 'NORMAL', 0),
('M106', 'business_service-log', '服务日志', 'M001', 'MENU', '/business/service-log', 'view.business_service-log', 'mdi:file-document-outline', 6, 0, 'NORMAL', 0),
('M107', 'business_quality', '质检管理', 'M001', 'MENU', '/business/quality', 'view.business_quality', 'mdi:clipboard-check-outline', 7, 0, 'NORMAL', 0),
('M108', 'business_evaluation', '服务评价', 'M001', 'MENU', '/business/evaluation', 'view.business_evaluation', 'mdi:star-outline', 8, 0, 'NORMAL', 0),
('M109', 'business_financial', '财务结算', 'M001', 'MENU', '/business/financial', 'view.business_financial', 'mdi:calculator', 9, 0, 'NORMAL', 0);

-- 系统管理子菜单
INSERT INTO t_menu (menu_id, menu_code, menu_name, parent_id, menu_type, path, component, icon, order_num, is_hidden, status, deleted) VALUES
('M201', 'system_user', '用户管理', 'M002', 'MENU', '/system/user', 'view.system_user', 'mdi:account-multiple-outline', 1, 0, 'NORMAL', 0),
('M202', 'system_role', '角色管理', 'M002', 'MENU', '/system/role', 'view.system_role', 'mdi:shield-account-outline', 2, 0, 'NORMAL', 0),
('M203', 'system_menu', '菜单管理', 'M002', 'MENU', '/system/menu', 'view.system_menu', 'mdi:menu-open', 3, 0, 'NORMAL', 0),
('M205', 'system_dict', '字典管理', 'M002', 'MENU', '/system/dict', 'view.system_dict', 'mdi:book-outline', 5, 0, 'NORMAL', 0);
