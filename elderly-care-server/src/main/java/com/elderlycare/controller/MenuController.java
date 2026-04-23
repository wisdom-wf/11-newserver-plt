package com.elderlycare.controller;

import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.service.MenuService;
import com.elderlycare.vo.MenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 获取当前用户的菜单树
     */
    @GetMapping("/user")
    public Result<List<MenuVO>> getUserMenus() {
        String userId = UserContext.getUserId();
        List<MenuVO> menus = menuService.getMenuTreeByUserId(userId);
        return Result.success(menus);
    }

    /**
     * 获取所有菜单（管理用）
     */
    @GetMapping("/list")
    public Result<List<MenuVO>> getAllMenus() {
        List<MenuVO> menus = menuService.getMenuTree();
        return Result.success(menus);
    }
}
