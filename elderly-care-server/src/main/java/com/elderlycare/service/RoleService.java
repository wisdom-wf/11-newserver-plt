package com.elderlycare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elderlycare.dto.RoleDTO;
import com.elderlycare.entity.Role;
import com.elderlycare.vo.RoleVO;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色列表
     */
    Page<RoleVO> pageRoles(int current, int size, String roleName, String status);

    /**
     * 根据ID查询角色详情
     */
    RoleVO getRoleById(String roleId);

    /**
     * 根据角色代码查询角色
     */
    Role getRoleByRoleCode(String roleCode);

    /**
     * 新增角色
     */
    boolean addRole(RoleDTO roleDTO);

    /**
     * 修改角色
     */
    boolean updateRole(RoleDTO roleDTO);

    /**
     * 删除角色
     */
    boolean deleteRole(String roleId);

    /**
     * 查询所有角色
     */
    List<RoleVO> listAllRoles();

    /**
     * 分配角色权限
     */
    boolean assignPermissions(String roleId, List<String> permissionIds);

    /**
     * 查询角色权限ID列表
     */
    List<String> getRolePermissionIds(String roleId);

    /**
     * 查询用户拥有的角色列表
     */
    List<RoleVO> getRolesByUserId(String userId);
}
