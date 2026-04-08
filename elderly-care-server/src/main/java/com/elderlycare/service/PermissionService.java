package com.elderlycare.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderlycare.dto.PermissionDTO;
import com.elderlycare.entity.Permission;
import com.elderlycare.vo.PermissionVO;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService extends IService<Permission> {

    /**
     * 查询所有权限(树形结构)
     */
    List<PermissionVO> listAllPermissionsTree();

    /**
     * 根据ID查询权限详情
     */
    PermissionVO getPermissionById(String permissionId);

    /**
     * 根据权限代码查询权限
     */
    Permission getPermissionByPermissionCode(String permissionCode);

    /**
     * 新增权限
     */
    boolean addPermission(PermissionDTO permissionDTO);

    /**
     * 修改权限
     */
    boolean updatePermission(PermissionDTO permissionDTO);

    /**
     * 删除权限
     */
    boolean deletePermission(String permissionId);

    /**
     * 根据角色ID查询权限列表
     */
    List<PermissionVO> getPermissionsByRoleId(String roleId);

    /**
     * 根据用户ID查询权限列表
     */
    List<PermissionVO> getPermissionsByUserId(String userId);

    /**
     * 查询用户的菜单树
     */
    List<PermissionVO> getMenuTreeByUserId(String userId);
}
