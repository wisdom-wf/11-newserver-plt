package com.elderlycare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.dto.PermissionDTO;
import com.elderlycare.entity.Permission;
import com.elderlycare.mapper.PermissionMapper;
import com.elderlycare.service.PermissionService;
import com.elderlycare.vo.PermissionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务实现类
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {

    @Autowired
    private PermissionMapper permissionMapper;

    @Override
    public List<PermissionVO> listAllPermissionsTree() {
        List<Permission> allPermissions = permissionMapper.selectTreeList();
        return buildTree(allPermissions);
    }

    @Override
    public PermissionVO getPermissionById(String permissionId) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException(404, "权限不存在");
        }
        return convertToVO(permission);
    }

    @Override
    public Permission getPermissionByPermissionCode(String permissionCode) {
        return permissionMapper.selectByPermissionCode(permissionCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addPermission(PermissionDTO permissionDTO) {
        // 检查权限代码是否已存在
        Permission existPermission = permissionMapper.selectByPermissionCode(permissionDTO.getPermissionCode());
        if (existPermission != null) {
            throw new BusinessException(400, "权限代码已存在");
        }

        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionDTO, permission);
        permission.setPermissionId(IDGenerator.generateId());
        permission.setStatus("NORMAL");

        return permissionMapper.insert(permission) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePermission(PermissionDTO permissionDTO) {
        Permission existPermission = permissionMapper.selectById(permissionDTO.getPermissionId());
        if (existPermission == null) {
            throw new BusinessException(404, "权限不存在");
        }

        // 如果修改了权限代码，检查是否重复
        if (!existPermission.getPermissionCode().equals(permissionDTO.getPermissionCode())) {
            Permission duplicatePermission = permissionMapper.selectByPermissionCode(permissionDTO.getPermissionCode());
            if (duplicatePermission != null) {
                throw new BusinessException(400, "权限代码已存在");
            }
        }

        Permission permission = new Permission();
        BeanUtils.copyProperties(permissionDTO, permission);

        return permissionMapper.updateById(permission) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePermission(String permissionId) {
        Permission permission = permissionMapper.selectById(permissionId);
        if (permission == null) {
            throw new BusinessException(404, "权限不存在");
        }

        // 逻辑删除
        return permissionMapper.deleteById(permissionId) > 0;
    }

    @Override
    public List<PermissionVO> getPermissionsByRoleId(String roleId) {
        List<Permission> permissions = permissionMapper.selectByRoleId(roleId);
        return permissions.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> getPermissionsByUserId(String userId) {
        List<Permission> permissions = permissionMapper.selectByUserId(userId);
        return permissions.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<PermissionVO> getMenuTreeByUserId(String userId) {
        List<Permission> permissions = permissionMapper.selectByUserId(userId);
        // 只返回菜单类型的权限
        List<Permission> menus = permissions.stream()
                .filter(p -> "MENU".equals(p.getPermissionType()))
                .collect(Collectors.toList());
        return buildTree(menus);
    }

    /**
     * 构建树形结构
     */
    private List<PermissionVO> buildTree(List<Permission> permissions) {
        List<PermissionVO> tree = new ArrayList<>();
        for (Permission permission : permissions) {
            if ("0".equals(permission.getParentId()) || permission.getParentId() == null) {
                PermissionVO vo = convertToVO(permission);
                vo.setChildren(getChildren(permission.getPermissionId(), permissions));
                tree.add(vo);
            }
        }
        return tree;
    }

    /**
     * 获取子节点
     */
    private List<PermissionVO> getChildren(String parentId, List<Permission> permissions) {
        List<PermissionVO> children = new ArrayList<>();
        for (Permission permission : permissions) {
            if (parentId.equals(permission.getParentId())) {
                PermissionVO vo = convertToVO(permission);
                vo.setChildren(getChildren(permission.getPermissionId(), permissions));
                children.add(vo);
            }
        }
        return children;
    }

    /**
     * 转换为VO
     */
    private PermissionVO convertToVO(Permission permission) {
        PermissionVO vo = new PermissionVO();
        BeanUtils.copyProperties(permission, vo);
        return vo;
    }
}
