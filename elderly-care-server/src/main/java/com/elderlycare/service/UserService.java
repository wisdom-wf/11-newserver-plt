package com.elderlycare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.elderlycare.dto.UserDTO;
import com.elderlycare.entity.User;
import com.elderlycare.vo.UserVO;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户列表
     */
    Page<UserVO> pageUsers(int current, int size, String username, String status, String userType);

    /**
     * 根据ID查询用户详情
     */
    UserVO getUserById(String userId);

    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);

    /**
     * 新增用户
     */
    boolean addUser(UserDTO userDTO);

    /**
     * 修改用户
     */
    boolean updateUser(UserDTO userDTO);

    /**
     * 删除用户
     */
    boolean deleteUser(String userId);

    /**
     * 重置密码
     */
    boolean resetPassword(String userId, String newPassword);

    /**
     * 分配用户角色
     */
    boolean assignRoles(String userId, List<String> roleIds);

    /**
     * 查询用户角色ID列表
     */
    List<String> getUserRoleIds(String userId);

    /**
     * 查询用户角色代码列表
     */
    List<String> getUserRoleCodes(String userId);

    /**
     * 查询用户权限代码列表
     */
    List<String> getUserPermissionCodes(String userId);

    /**
     * 查询用户权限URL+方法列表（用于后端权限校验）
     * 返回格式: "METHOD:url" (如 "GET:/api/orders")
     */
    List<String> getUserPermissionUrls(String userId);
}
