package com.docreview.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.docreview.dto.request.UserCreateRequest;
import com.docreview.dto.request.UserUpdateRequest;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.Result;
import com.docreview.dto.response.UserResponse;
import com.docreview.entity.Role;
import com.docreview.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Operation(summary = "获取用户列表")
    @SaCheckPermission("sys:user:list")
    @GetMapping
    public Result<PageResult<UserResponse>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status) {
        return Result.success(userService.getPage(current, size, username, realName, status));
    }
    
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<UserResponse> getProfile() {
        Long userId = StpUtil.getLoginIdAsLong();
        com.docreview.entity.User user = userService.getById(userId);
        if (user == null) {
            return Result.error(40001, "用户不存在");
        }
        UserResponse response = new UserResponse();
        org.springframework.beans.BeanUtils.copyProperties(user, response);
        response.setRoleIds(userService.getRoleIdsByUserId(userId));
        return Result.success(response);
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserUpdateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        // 仅允许修改昵称、邮箱、手机号
        UserUpdateRequest safeRequest = new UserUpdateRequest();
        safeRequest.setRealName(request.getRealName());
        safeRequest.setEmail(request.getEmail());
        safeRequest.setPhone(request.getPhone());
        userService.updateUser(userId, safeRequest);
        return Result.success();
    }

    @Operation(summary = "搜索用户")
    @GetMapping("/search")
    public Result<List<UserResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(userService.search(keyword, limit));
    }
    
    @Operation(summary = "获取用户详情")
    @SaCheckPermission("sys:user:query")
    @GetMapping("/{id}")
    public Result<UserResponse> getById(@PathVariable Long id) {
        com.docreview.entity.User user = userService.getById(id);
        if (user == null) {
            return Result.error(40001, "用户不存在");
        }
        
        UserResponse response = new UserResponse();
        org.springframework.beans.BeanUtils.copyProperties(user, response);
        response.setRoleIds(userService.getRoleIdsByUserId(id));
        
        return Result.success(response);
    }
    
    @Operation(summary = "创建用户")
    @SaCheckPermission("sys:user:create")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) {
        Long id = userService.createUser(request);
        return Result.success("创建成功", id);
    }
    
    @Operation(summary = "更新用户")
    @SaCheckPermission("sys:user:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除用户")
    @SaCheckPermission("sys:user:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
    
    @Operation(summary = "更新用户状态")
    @SaCheckPermission("sys:user:edit")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        userService.updateStatus(id, status);
        return Result.success();
    }
    
    @Operation(summary = "重置密码")
    @SaCheckPermission("sys:user:resetPwd")
    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
    
    @Operation(summary = "修改密码")
    @PutMapping("/{id}/password")
    public Result<Void> changePassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        userService.changePassword(id, oldPassword, newPassword);
        return Result.success();
    }
    
    @Operation(summary = "获取用户角色")
    @SaCheckPermission("sys:user:query")
    @GetMapping("/{id}/roles")
    public Result<List<Role>> getRoles(@PathVariable Long id) {
        return Result.success(userService.getRolesByUserId(id));
    }
    
    @Operation(summary = "分配用户角色")
    @SaCheckPermission("sys:user:edit")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> roleIds = body.get("roleIds");
        userService.assignRoles(id, roleIds);
        return Result.success();
    }
}
