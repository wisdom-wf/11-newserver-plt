package com.elderlycare.service.impl;

import com.elderlycare.entity.Menu;
import com.elderlycare.mapper.MenuMapper;
import com.elderlycare.service.MenuService;
import com.elderlycare.vo.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 */
@Service
public class MenuServiceImpl implements MenuService {

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public List<Menu> getAllMenus() {
        return menuMapper.selectAllMenus();
    }

    @Override
    public List<MenuVO> getMenuTree() {
        List<Menu> menus = getAllMenus();
        return buildMenuTree(menus);
    }

    @Override
    public List<Menu> getMenusByRoleId(String roleId) {
        return menuMapper.selectByRoleId(roleId);
    }

    @Override
    public List<Menu> getMenusByUserId(String userId) {
        return menuMapper.selectByUserId(userId);
    }

    @Override
    public List<MenuVO> getMenuTreeByUserId(String userId) {
        List<Menu> menus = getMenusByUserId(userId);
        return buildMenuTree(menus);
    }

    private List<MenuVO> buildMenuTree(List<Menu> menus) {
        List<MenuVO> tree = new ArrayList<>();
        Map<String, List<Menu>> childrenMap = new HashMap<>();

        for (Menu menu : menus) {
            MenuVO vo = convertToVO(menu);
            if (menu.getParentId() == null || "".equals(menu.getParentId())) {
                tree.add(vo);
            } else {
                childrenMap.computeIfAbsent(menu.getParentId(), k -> new ArrayList<>()).add(menu);
            }
        }

        for (MenuVO vo : tree) {
            fillChildren(vo, childrenMap);
        }

        return tree;
    }

    private void fillChildren(MenuVO parent, Map<String, List<Menu>> childrenMap) {
        List<Menu> children = childrenMap.get(parent.getMenuId());
        if (children != null && !children.isEmpty()) {
            for (Menu child : children) {
                MenuVO childVO = convertToVO(child);
                parent.getChildren().add(childVO);
                fillChildren(childVO, childrenMap);
            }
        }
    }

    private MenuVO convertToVO(Menu menu) {
        MenuVO vo = new MenuVO();
        vo.setMenuId(menu.getMenuId());
        vo.setMenuCode(menu.getMenuCode());
        vo.setMenuName(menu.getMenuName());
        vo.setParentId(menu.getParentId());
        vo.setMenuType(menu.getMenuType());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setIcon(menu.getIcon());
        vo.setOrderNum(menu.getOrderNum());
        vo.setIsHidden(menu.getIsHidden());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
