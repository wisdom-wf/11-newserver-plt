package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户角色Mapper接口
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 批量插入用户角色关联
     */
    int batchInsert(@Param("list") List<UserRole> userRoles);

    /**
     * 删除用户所有角色关联
     */
    int deleteByUserId(@Param("userId") String userId);

    /**
     * 查询用户的角色ID列表
     */
    List<String> selectRoleIdsByUserId(@Param("userId") String userId);
}
