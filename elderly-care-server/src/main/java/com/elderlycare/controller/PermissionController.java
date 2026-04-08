package com.elderlycare.controller;

import com.elderlycare.common.Result;
import com.elderlycare.dto.PermissionDTO;
import com.elderlycare.service.PermissionService;
import com.elderlycare.vo.PermissionVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限控制器
 */
@RestController
@RequestMapping("/api/system/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    /**
     * 查询所有权限(树形结构)
     */
    @GetMapping("/tree")
    public Result<List<PermissionVO>> listAllPermissionsTree() {
        List<PermissionVO> permissions = permissionService.listAllPermissionsTree();
        return Result.success(permissions);
    }

    /**
     * 获取权限详情
     */
    @GetMapping("/{id}")
    public Result<PermissionVO> getPermissionById(@PathVariable String id) {
        PermissionVO permissionVO = permissionService.getPermissionById(id);
        return Result.success(permissionVO);
    }

    /**
     * 新增权限
     */
    @PostMapping
    public Result<Void> addPermission(@Valid @RequestBody PermissionDTO permissionDTO) {
        permissionService.addPermission(permissionDTO);
        return Result.success();
    }

    /**
     * 修改权限
     */
    @PutMapping("/{id}")
    public Result<Void> updatePermission(@PathVariable String id, @Valid @RequestBody PermissionDTO permissionDTO) {
        permissionDTO.setPermissionId(id);
        permissionService.updatePermission(permissionDTO);
        return Result.success();
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePermission(@PathVariable String id) {
        permissionService.deletePermission(id);
        return Result.success();
    }

    /**
     * 根据角色ID查询权限列表
     */
    @GetMapping("/role/{roleId}")
    public Result<List<PermissionVO>> getPermissionsByRoleId(@PathVariable String roleId) {
        List<PermissionVO> permissions = permissionService.getPermissionsByRoleId(roleId);
        return Result.success(permissions);
    }

    /**
     * 根据用户ID查询权限列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<PermissionVO>> getPermissionsByUserId(@PathVariable String userId) {
        List<PermissionVO> permissions = permissionService.getPermissionsByUserId(userId);
        return Result.success(permissions);
    }

    /**
     * 获取用户菜单树
     */
    @GetMapping("/menu/{userId}")
    public Result<List<PermissionVO>> getMenuTreeByUserId(@PathVariable String userId) {
        List<PermissionVO> menus = permissionService.getMenuTreeByUserId(userId);
        return Result.success(menus);
    }
}
