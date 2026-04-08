package com.elderlycare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.Result;
import com.elderlycare.dto.PasswordDTO;
import com.elderlycare.dto.UserDTO;
import com.elderlycare.service.UserService;
import com.elderlycare.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/system/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public Result<Page<UserVO>> pageUsers(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userType) {
        Page<UserVO> page = userService.pageUsers(current, size, username, status, userType);
        return Result.success(page);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable String id) {
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    /**
     * 新增用户
     */
    @PostMapping
    public Result<Void> addUser(@Valid @RequestBody UserDTO userDTO) {
        userService.addUser(userDTO);
        return Result.success();
    }

    /**
     * 修改用户
     */
    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable String id, @Valid @RequestBody UserDTO userDTO) {
        userDTO.setUserId(id);
        userService.updateUser(userDTO);
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return Result.success();
    }

    /**
     * 重置密码
     */
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable String id, @RequestBody PasswordDTO passwordDTO) {
        userService.resetPassword(id, passwordDTO.getNewPassword());
        return Result.success();
    }

    /**
     * 分配用户角色
     */
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable String id, @RequestBody List<String> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }

    /**
     * 获取用户角色ID列表
     */
    @GetMapping("/{id}/roles")
    public Result<List<String>> getUserRoleIds(@PathVariable String id) {
        List<String> roleIds = userService.getUserRoleIds(id);
        return Result.success(roleIds);
    }
}
