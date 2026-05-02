USE elderly_care;
SET NAMES utf8mb4;

-- =============================================
-- 菜单表
-- 用于存储前端菜单配置，配合 t_role_menu 实现动态菜单
-- =============================================
CREATE TABLE IF NOT EXISTS t_menu (
    menu_id VARCHAR(64) PRIMARY KEY COMMENT '菜单ID',
    menu_code VARCHAR(128) UNIQUE NOT NULL COMMENT '菜单代码(路由name)',
    menu_name VARCHAR(64) NOT NULL COMMENT '菜单名称',
    parent_id VARCHAR(64) DEFAULT NULL COMMENT '父菜单ID',
    menu_type VARCHAR(32) DEFAULT 'MENU' COMMENT '类型 MENU/BUTTON',
    path VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
    component VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
    icon VARCHAR(128) DEFAULT NULL COMMENT '图标',
    order_num INT DEFAULT 0 COMMENT '排序',
    is_hidden TINYINT DEFAULT 0 COMMENT '是否隐藏 0-否 1-是',
    status VARCHAR(32) DEFAULT 'NORMAL' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='菜单表';

-- =============================================
-- 角色菜单关联表
-- =============================================
CREATE TABLE IF NOT EXISTS t_role_menu (
    role_menu_id VARCHAR(64) PRIMARY KEY COMMENT '主键',
    role_id VARCHAR(64) NOT NULL COMMENT '角色ID',
    menu_id VARCHAR(64) NOT NULL COMMENT '菜单ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_role_id (role_id),
    INDEX idx_menu_id (menu_id),
    UNIQUE KEY uk_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色菜单关联表';
