package com.elderlycare.service;

import com.elderlycare.entity.RoleMenu;
import java.util.List;

/**
 * 角色菜单服务接口
 */
public interface RoleMenuService {

    /**
     * 为角色分配菜单
     */
    void assignMenusToRole(String roleId, List<RoleMenu> menus);

    /**
     * 获取角色的菜单ID列表
     */
    List<String> getMenuIdsByRoleId(String roleId);
}
