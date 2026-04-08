package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 角色权限Mapper接口
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 批量插入角色权限关联
     */
    int batchInsert(@Param("list") List<RolePermission> rolePermissions);

    /**
     * 删除角色所有权限关联
     */
    int deleteByRoleId(@Param("roleId") String roleId);

    /**
     * 查询角色的权限ID列表
     */
    List<String> selectPermissionIdsByRoleId(@Param("roleId") String roleId);
}
