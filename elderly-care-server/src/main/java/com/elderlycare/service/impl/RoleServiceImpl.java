package com.elderlycare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.dto.RoleDTO;
import com.elderlycare.entity.Role;
import com.elderlycare.entity.RolePermission;
import com.elderlycare.mapper.RoleMapper;
import com.elderlycare.mapper.RolePermissionMapper;
import com.elderlycare.service.PermissionService;
import com.elderlycare.service.RoleService;
import com.elderlycare.vo.PermissionVO;
import com.elderlycare.vo.RoleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private RolePermissionMapper rolePermissionMapper;

    @Autowired
    private PermissionService permissionService;

    @Override
    public Page<RoleVO> pageRoles(int current, int size, String roleName, String status) {
        Page<Role> page = new Page<>(current, size);
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(roleName)) {
            queryWrapper.like(Role::getRoleName, roleName);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Role::getStatus, status);
        }

        Page<Role> rolePage = page(page, queryWrapper);

        // 转换为VO
        Page<RoleVO> voPage = new Page<>(rolePage.getCurrent(), rolePage.getSize(), rolePage.getTotal());
        List<RoleVO> voList = rolePage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public RoleVO getRoleById(String roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        RoleVO roleVO = convertToVO(role);
        // 填充权限信息
        List<PermissionVO> permissions = permissionService.getPermissionsByRoleId(roleId);
        roleVO.setPermissions(permissions);
        return roleVO;
    }

    @Override
    public Role getRoleByRoleCode(String roleCode) {
        return roleMapper.selectByRoleCode(roleCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRole(RoleDTO roleDTO) {
        // 检查角色代码是否已存在
        Role existRole = roleMapper.selectByRoleCode(roleDTO.getRoleCode());
        if (existRole != null) {
            throw new BusinessException(400, "角色代码已存在");
        }

        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);
        role.setRoleId(IDGenerator.generateId());
        role.setStatus("NORMAL");

        int result = roleMapper.insert(role);

        // 分配权限
        if (result > 0 && roleDTO.getPermissionIds() != null && !roleDTO.getPermissionIds().isEmpty()) {
            assignPermissions(role.getRoleId(), roleDTO.getPermissionIds());
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(RoleDTO roleDTO) {
        Role existRole = roleMapper.selectById(roleDTO.getRoleId());
        if (existRole == null) {
            throw new BusinessException(404, "角色不存在");
        }

        // 如果修改了角色代码，检查是否重复
        if (!existRole.getRoleCode().equals(roleDTO.getRoleCode())) {
            Role duplicateRole = roleMapper.selectByRoleCode(roleDTO.getRoleCode());
            if (duplicateRole != null) {
                throw new BusinessException(400, "角色代码已存在");
            }
        }

        Role role = new Role();
        BeanUtils.copyProperties(roleDTO, role);

        int result = roleMapper.updateById(role);

        // 更新权限
        if (result > 0 && roleDTO.getPermissionIds() != null) {
            assignPermissions(roleDTO.getRoleId(), roleDTO.getPermissionIds());
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(String roleId) {
        Role role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }

        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(roleId);

        // 逻辑删除
        return roleMapper.deleteById(roleId) > 0;
    }

    @Override
    public List<RoleVO> listAllRoles() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getStatus, "NORMAL");
        List<Role> roles = roleMapper.selectList(queryWrapper);
        return roles.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignPermissions(String roleId, List<String> permissionIds) {
        // 删除现有权限关联
        rolePermissionMapper.deleteByRoleId(roleId);

        // 添加新权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermission> rolePermissions = permissionIds.stream().map(permissionId -> {
                RolePermission rp = new RolePermission();
                rp.setRolePermissionId(IDGenerator.generateId());
                rp.setRoleId(roleId);
                rp.setPermissionId(permissionId);
                return rp;
            }).collect(Collectors.toList());

            rolePermissionMapper.batchInsert(rolePermissions);
        }

        return true;
    }

    @Override
    public List<String> getRolePermissionIds(String roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }

    @Override
    public List<RoleVO> getRolesByUserId(String userId) {
        List<Role> roles = roleMapper.selectByUserId(userId);
        return roles.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 转换为VO
     */
    private RoleVO convertToVO(Role role) {
        RoleVO roleVO = new RoleVO();
        BeanUtils.copyProperties(role, roleVO);
        return roleVO;
    }
}
