package com.elderlycare.service;

import com.elderlycare.entity.Menu;
import com.elderlycare.vo.MenuVO;
import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 获取所有菜单
     */
    List<Menu> getAllMenus();

    /**
     * 获取菜单树
     */
    List<MenuVO> getMenuTree();

    /**
     * 根据角色ID获取菜单
     */
    List<Menu> getMenusByRoleId(String roleId);

    /**
     * 根据用户ID获取菜单
     */
    List<Menu> getMenusByUserId(String userId);

    /**
     * 根据用户ID获取菜单树
     */
    List<MenuVO> getMenuTreeByUserId(String userId);
}
