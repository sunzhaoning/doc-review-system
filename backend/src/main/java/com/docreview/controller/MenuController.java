package com.docreview.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.docreview.dto.request.MenuCreateRequest;
import com.docreview.dto.request.MenuUpdateRequest;
import com.docreview.dto.response.MenuTreeResponse;
import com.docreview.dto.response.Result;
import com.docreview.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {
    
    @Autowired
    private MenuService menuService;
    
    @Operation(summary = "获取菜单树")
    @SaCheckPermission("sys:menu:list")
    @GetMapping("/tree")
    public Result<List<MenuTreeResponse>> getMenuTree() {
        return Result.success(menuService.getMenuTree());
    }
    
    @Operation(summary = "获取菜单列表")
    @SaCheckPermission("sys:menu:list")
    @GetMapping
    public Result<List<MenuTreeResponse>> getMenuList() {
        return Result.success(menuService.getMenuTree());
    }
    
    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/user")
    public Result<List<MenuTreeResponse>> getCurrentUserMenus() {
        // 从 Sa-Token 获取当前用户ID
        Long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        return Result.success(menuService.getUserMenuTree(userId));
    }
    
    @Operation(summary = "获取菜单详情")
    @SaCheckPermission("sys:menu:query")
    @GetMapping("/{id}")
    public Result<com.docreview.entity.Menu> getById(@PathVariable Long id) {
        com.docreview.entity.Menu menu = menuService.getById(id);
        if (menu == null) {
            return Result.error(40001, "菜单不存在");
        }
        return Result.success(menu);
    }
    
    @Operation(summary = "创建菜单")
    @SaCheckPermission("sys:menu:create")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody MenuCreateRequest request) {
        Long id = menuService.createMenu(request);
        return Result.success("创建成功", id);
    }
    
    @Operation(summary = "更新菜单")
    @SaCheckPermission("sys:menu:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody MenuUpdateRequest request) {
        menuService.updateMenu(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除菜单")
    @SaCheckPermission("sys:menu:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }
}
