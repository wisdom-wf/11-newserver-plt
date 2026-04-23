package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 菜单Mapper接口
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 查询所有启用的菜单
     */
    List<Menu> selectAllMenus();

    /**
     * 根据角色ID查询菜单
     */
    List<Menu> selectByRoleId(@Param("roleId") String roleId);

    /**
     * 查询用户的菜单（通过角色）
     */
    List<Menu> selectByUserId(@Param("userId") String userId);
}
