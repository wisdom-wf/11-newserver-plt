-- ============================================
-- R004 / R005 按钮权限补充 SQL
-- 根因：t_role_permission 表中 R004=0条、R005=0条
--       导致 PROVIDER_ADMIN 和 STAFF 用户 buttons=null → 所有 API 403
-- 执行: docker exec mysql-dev mysql -uroot -proot elderly_care < sql/rbac_r004_r005_permission_fix.sql
-- 验证: 执行后重新登录，userInfo.buttons 应有值
-- 回滚: DELETE FROM t_role_permission WHERE role_permission_id LIKE 'RP04_%' OR role_permission_id LIKE 'RP05_%';
-- ============================================

USE elderly_care;

-- ============================================
-- R004 (PROVIDER_ADMIN) 按钮权限补充
-- 范围：对标 R002 的业务范围（不含系统管理）
-- 加上服务商自己的员工管理权限
-- ============================================
INSERT INTO t_role_permission (role_permission_id, role_id, permission_id, create_time)
SELECT CONCAT('RP04_', permission_id), 'R004', permission_id, NOW()
FROM t_permission
WHERE permission_code IN (
    -- 服务人员管理（服务商管自己员工）
    'staff', 'staff:list', 'staff:list:query', 'staff:list:add', 'staff:list:edit', 'staff:list:delete',
    -- 老人客户管理
    'elder', 'elder:list', 'elder:list:query', 'elder:list:add', 'elder:list:edit', 'elder:list:delete',
    -- 预约管理
    'appointment', 'appointment:list', 'appointment:list:query', 'appointment:list:add',
    'appointment:list:edit', 'appointment:list:delete', 'appointment:list:import',
    'appointment:list:confirm', 'appointment:list:dispatch',
    -- 订单管理
    'order', 'order:list', 'order:list:query', 'order:list:add', 'order:list:edit',
    'order:list:cancel', 'order:list:dispatch',
    -- 服务日志
    'service-log', 'service-log:list', 'service-log:list:query', 'service-log:list:add', 'service-log:list:edit',
    -- 服务质检
    'quality', 'quality:list', 'quality:list:query', 'quality:list:add', 'quality:list:edit',
    -- 服务评价
    'evaluation', 'evaluation:list', 'evaluation:list:query', 'evaluation:list:reply',
    -- 服务商查询（服务商管理员可查看自己服务商信息）
    'provider', 'provider:list', 'provider:list:query',
    -- 驾驶舱（总览+排行，但去掉敏感统计）
    'cockpit', 'cockpit:overview', 'cockpit:overview:query',
    'cockpit:overview:orderTrend', 'cockpit:overview:providerRanking', 'cockpit:overview:staffRanking'
)
AND permission_id NOT IN (SELECT permission_id FROM t_role_permission WHERE role_id = 'R004');

-- ============================================
-- R005 (STAFF) 按钮权限补充
-- 范围：受限业务（查看+执行），不含：增删改老人、增删员工、财务、敏感统计
-- ============================================
INSERT INTO t_role_permission (role_permission_id, role_id, permission_id, create_time)
SELECT CONCAT('RP05_', permission_id), 'R005', permission_id, NOW()
FROM t_permission
WHERE permission_code IN (
    -- 服务人员（只能查看自己所在服务商的人员列表）
    'staff', 'staff:list', 'staff:list:query',
    -- 老人客户（查看+编辑，不含新增/删除）
    'elder', 'elder:list', 'elder:list:query', 'elder:list:edit',
    -- 预约管理（查看+编辑+确认+派单）
    'appointment', 'appointment:list', 'appointment:list:query', 'appointment:list:edit',
    'appointment:list:confirm', 'appointment:list:dispatch',
    -- 订单管理（查看+派单，不含新增/取消/编辑）
    'order', 'order:list', 'order:list:query', 'order:list:dispatch',
    -- 服务日志（查看+新增+编辑）
    'service-log', 'service-log:list', 'service-log:list:query', 'service-log:list:add', 'service-log:list:edit',
    -- 服务质检（查看+编辑，不含新增）
    'quality', 'quality:list', 'quality:list:query', 'quality:list:edit',
    -- 服务评价（查看，不含回复）
    'evaluation', 'evaluation:list', 'evaluation:list:query',
    -- 驾驶舱（只读总览，不含排行）
    'cockpit', 'cockpit:overview', 'cockpit:overview:query'
)
AND permission_id NOT IN (SELECT permission_id FROM t_role_permission WHERE role_id = 'R005');

-- ============================================
-- 验证1：各角色按钮权限数量
-- ============================================
SELECT
    r.role_id,
    r.role_code,
    COUNT(rp.permission_id) AS button_permission_count
FROM t_role r
LEFT JOIN t_role_permission rp ON r.role_id = rp.role_id
GROUP BY r.role_id, r.role_code
ORDER BY r.role_id;

-- ============================================
-- 验证2：各用户实际 buttons 数量（需重启后端使 JWT 重新加载）
-- ============================================
SELECT
    u.user_id,
    u.username,
    u.user_type,
    GROUP_CONCAT(DISTINCT r.role_code ORDER BY r.role_code) AS roles,
    COUNT(DISTINCT rp.permission_id) AS button_count
FROM t_user u
LEFT JOIN t_user_role ur ON u.user_id = ur.user_id
LEFT JOIN t_role r ON ur.role_id = r.role_id
LEFT JOIN t_role_permission rp ON r.role_id = rp.role_id
WHERE u.deleted = 0
GROUP BY u.user_id, u.username, u.user_type
ORDER BY u.user_id;
