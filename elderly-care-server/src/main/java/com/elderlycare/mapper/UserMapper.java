package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 查询用户角色ID列表
     */
    List<String> selectRoleIdsByUserId(@Param("userId") String userId);

    /**
     * 查询用户角色代码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") String userId);

    /**
     * 查询用户权限代码列表
     */
    List<String> selectPermissionCodesByUserId(@Param("userId") String userId);

    /**
     * 查询用户权限URL+方法列表（用于后端权限校验）
     * 返回格式: "METHOD:url" (如 "GET:/api/orders")
     */
    List<String> selectPermissionUrlsByUserId(@Param("userId") String userId);

    /**
     * 根据用户名模糊查询用户列表
     */
    List<User> selectByUsernameLike(@Param("username") String username);

    /**
     * 更新用户登录信息
     */
    int updateLoginInfo(@Param("userId") String userId, @Param("loginIp") String loginIp);

    /**
     * 增加登录失败次数
     */
    int incrementLoginFailCount(@Param("userId") String userId);

    /**
     * 重置登录失败次数
     */
    int resetLoginFailCount(@Param("userId") String userId);

    /**
     * 锁定用户
     */
    int lockUser(@Param("userId") String userId);

    /**
     * 解锁用户
     */
    int unlockUser(@Param("userId") String userId);
}
