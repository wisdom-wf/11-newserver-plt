package com.elderlycare.service.impl;

import com.elderlycare.entity.RoleMenu;
import com.elderlycare.mapper.RoleMenuMapper;
import com.elderlycare.service.RoleMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色菜单服务实现类
 */
@Service
public class RoleMenuServiceImpl implements RoleMenuService {

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Override
    @Transactional
    public void assignMenusToRole(String roleId, List<RoleMenu> menus) {
        roleMenuMapper.deleteByRoleId(roleId);
        if (menus != null && !menus.isEmpty()) {
            roleMenuMapper.batchInsert(menus);
        }
    }

    @Override
    public List<String> getMenuIdsByRoleId(String roleId) {
        return roleMenuMapper.selectByRoleId(roleId).stream()
                .map(m -> m.getMenuId())
                .collect(java.util.stream.Collectors.toList());
    }
}
