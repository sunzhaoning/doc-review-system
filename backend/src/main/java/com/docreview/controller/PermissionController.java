package com.docreview.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.docreview.dto.request.PermissionCreateRequest;
import com.docreview.dto.request.PermissionUpdateRequest;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.PermissionResponse;
import com.docreview.dto.response.Result;
import com.docreview.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限控制器
 * 
 * @author fullstack-dev
 * @since 2026-03-07
 */
@Tag(name = "权限管理")
@RestController
@RequestMapping("/api/v1/permissions")
public class PermissionController {
    
    @Autowired
    private PermissionService permissionService;
    
    @Operation(summary = "获取权限列表")
    @SaCheckPermission("sys:role:list")
    @GetMapping
    public Result<PageResult<PermissionResponse>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String permName,
            @RequestParam(required = false) String resourceType) {
        return Result.success(permissionService.getPage(current, size, permName, resourceType));
    }
    
    @Operation(summary = "获取所有权限")
    @SaCheckPermission("sys:role:list")
    @GetMapping("/all")
    public Result<List<PermissionResponse>> getAll() {
        return Result.success(permissionService.getAllPermissions());
    }
    
    @Operation(summary = "获取权限详情")
    @SaCheckPermission("sys:role:query")
    @GetMapping("/{id}")
    public Result<PermissionResponse> getById(@PathVariable Long id) {
        return Result.success(permissionService.getAllPermissions().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null));
    }
    
    @Operation(summary = "创建权限")
    @SaCheckPermission("sys:role:create")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody PermissionCreateRequest request) {
        Long id = permissionService.createPermission(request);
        return Result.success("创建成功", id);
    }
    
    @Operation(summary = "更新权限")
    @SaCheckPermission("sys:role:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PermissionUpdateRequest request) {
        permissionService.updatePermission(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除权限")
    @SaCheckPermission("sys:role:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.deletePermission(id);
        return Result.success();
    }
}
