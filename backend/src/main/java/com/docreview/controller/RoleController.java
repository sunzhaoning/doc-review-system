package com.docreview.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.docreview.dto.request.*;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.Result;
import com.docreview.dto.response.RoleResponse;
import com.docreview.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 角色控制器
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    @Operation(summary = "获取角色列表")
    @SaCheckPermission("sys:role:list")
    @GetMapping
    public Result<PageResult<RoleResponse>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) Integer status) {
        return Result.success(roleService.getPage(current, size, roleName, status));
    }
    
    @Operation(summary = "获取所有角色")
    @GetMapping("/all")
    public Result<List<RoleResponse>> getAllRoles() {
        return Result.success(roleService.getAllRoles());
    }
    
    @Operation(summary = "获取角色详情")
    @SaCheckPermission("sys:role:query")
    @GetMapping("/{id}")
    public Result<RoleResponse> getById(@PathVariable Long id) {
        com.docreview.entity.Role role = roleService.getById(id);
        if (role == null) {
            return Result.error(40001, "角色不存在");
        }
        
        RoleResponse response = new RoleResponse();
        org.springframework.beans.BeanUtils.copyProperties(role, response);
        response.setMenuIds(roleService.getMenuIdsByRoleId(id));
        response.setPermissionIds(roleService.getPermissionIdsByRoleId(id));
        
        return Result.success(response);
    }
    
    @Operation(summary = "创建角色")
    @SaCheckPermission("sys:role:create")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody RoleCreateRequest request) {
        Long id = roleService.createRole(request);
        return Result.success("创建成功", id);
    }
    
    @Operation(summary = "更新角色")
    @SaCheckPermission("sys:role:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RoleUpdateRequest request) {
        roleService.updateRole(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除角色")
    @SaCheckPermission("sys:role:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }
    
    @Operation(summary = "更新角色状态")
    @SaCheckPermission("sys:role:edit")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        roleService.updateStatus(id, status);
        return Result.success();
    }
    
    @Operation(summary = "获取角色菜单")
    @SaCheckPermission("sys:role:query")
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getMenus(@PathVariable Long id) {
        return Result.success(roleService.getMenuIdsByRoleId(id));
    }
    
    @Operation(summary = "分配角色菜单")
    @SaCheckPermission("sys:role:edit")
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> menuIds = body.get("menuIds");
        roleService.assignMenus(id, menuIds);
        return Result.success();
    }
    
    @Operation(summary = "获取角色权限")
    @SaCheckPermission("sys:role:query")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getPermissions(@PathVariable Long id) {
        return Result.success(roleService.getPermissionIdsByRoleId(id));
    }
    
    @Operation(summary = "分配角色权限")
    @SaCheckPermission("sys:role:edit")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> permissionIds = body.get("permissionIds");
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }
}
