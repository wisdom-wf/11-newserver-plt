package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 权限Mapper接口
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据角色ID查询权限列表
     */
    List<Permission> selectByRoleId(@Param("roleId") String roleId);

    /**
     * 根据角色ID列表查询权限列表
     */
    List<Permission> selectByRoleIds(@Param("roleIds") List<String> roleIds);

    /**
     * 查询用户拥有的权限列表
     */
    List<Permission> selectByUserId(@Param("userId") String userId);

    /**
     * 根据权限代码查询权限
     */
    Permission selectByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 查询树形权限列表
     */
    List<Permission> selectTreeList();

    /**
     * 根据父ID查询子权限
     */
    List<Permission> selectByParentId(@Param("parentId") String parentId);
}
