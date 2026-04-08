package com.elderlycare.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.Result;
import com.elderlycare.dto.RoleDTO;
import com.elderlycare.service.RoleService;
import com.elderlycare.vo.RoleVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色控制器
 */
@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 分页查询角色列表
     */
    @GetMapping
    public Result<Page<RoleVO>> pageRoles(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String status) {
        Page<RoleVO> page = roleService.pageRoles(current, size, roleName, status);
        return Result.success(page);
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public Result<RoleVO> getRoleById(@PathVariable String id) {
        RoleVO roleVO = roleService.getRoleById(id);
        return Result.success(roleVO);
    }

    /**
     * 查询所有角色
     */
    @GetMapping("/all")
    public Result<List<RoleVO>> listAllRoles() {
        List<RoleVO> roles = roleService.listAllRoles();
        return Result.success(roles);
    }

    /**
     * 新增角色
     */
    @PostMapping
    public Result<Void> addRole(@Valid @RequestBody RoleDTO roleDTO) {
        roleService.addRole(roleDTO);
        return Result.success();
    }

    /**
     * 修改角色
     */
    @PutMapping("/{id}")
    public Result<Void> updateRole(@PathVariable String id, @Valid @RequestBody RoleDTO roleDTO) {
        roleDTO.setRoleId(id);
        roleService.updateRole(roleDTO);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(@PathVariable String id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 分配角色权限
     */
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable String id, @RequestBody List<String> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }

    /**
     * 获取角色权限ID列表
     */
    @GetMapping("/{id}/permissions")
    public Result<List<String>> getRolePermissionIds(@PathVariable String id) {
        List<String> permissionIds = roleService.getRolePermissionIds(id);
        return Result.success(permissionIds);
    }
}
