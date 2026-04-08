package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 角色Mapper接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色代码查询角色
     */
    Role selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 查询用户拥有的角色列表
     */
    List<Role> selectByUserId(@Param("userId") String userId);

    /**
     * 根据角色ID列表查询角色
     */
    List<Role> selectByRoleIds(@Param("roleIds") List<String> roleIds);
}
