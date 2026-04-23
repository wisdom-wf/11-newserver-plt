package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 角色菜单Mapper接口
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /**
     * 根据角色ID删除
     */
    int deleteByRoleId(@Param("roleId") String roleId);

    /**
     * 批量插入角色菜单
     */
    int batchInsert(@Param("list") List<RoleMenu> list);

    /**
     * 根据角色ID查询
     */
    List<RoleMenu> selectByRoleId(@Param("roleId") String roleId);
}
