package com.docreview.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.docreview.dto.request.LoginRequest;
import com.docreview.dto.response.LoginResponse;
import com.docreview.dto.response.Result;
import com.docreview.dto.response.UserResponse;
import com.docreview.entity.Menu;
import com.docreview.entity.Permission;
import com.docreview.entity.Role;
import com.docreview.entity.User;
import com.docreview.service.MenuService;
import com.docreview.service.PermissionService;
import com.docreview.service.RoleService;
import com.docreview.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 认证控制器
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private PermissionService permissionService;
    
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // 查询用户
        User user = userService.getByUsername(request.getUsername());
        if (user == null) {
            return Result.error(40001, "用户名或密码错误");
        }
        
        // 检查状态
        if (user.getStatus() != 1) {
            return Result.error(40002, "账号已被禁用");
        }
        
        // 校验密码
        if (!userService.checkPassword(user, request.getPassword())) {
            return Result.error(40001, "用户名或密码错误");
        }
        
        // Sa-Token 登录
        StpUtil.login(user.getId());
        
        // 获取 Token
        String token = StpUtil.getTokenValue();
        
        // 获取角色和权限
        List<Role> roles = roleService.getRolesByUserId(user.getId());
        List<Permission> permissions = permissionService.getPermissionsByUserId(user.getId());
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRoles(roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));
        userInfo.setPermissions(permissions.stream().map(Permission::getPermCode).collect(Collectors.toList()));
        
        response.setUserInfo(userInfo);
        
        return Result.success("登录成功", response);
    }
    
    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }
    
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<Map<String, Object>> getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userService.getById(userId);
        
        if (user == null) {
            return Result.error(40001, "用户不存在");
        }
        
        List<Role> roles = roleService.getRolesByUserId(userId);
        List<Permission> permissions = permissionService.getPermissionsByUserId(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("realName", user.getRealName());
        result.put("avatar", user.getAvatar());
        result.put("roles", roles.stream().map(Role::getRoleCode).collect(Collectors.toList()));
        result.put("permissions", permissions.stream().map(Permission::getPermCode).collect(Collectors.toList()));
        
        return Result.success(result);
    }
    
    @Operation(summary = "获取当前用户菜单")
    @GetMapping("/menus")
    public Result<List<Menu>> getCurrentUserMenus() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Menu> menus = menuService.getMenusByUserId(userId);
        return Result.success(menus);
    }
}
