package com.docreview.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.docreview.dto.request.LdapConfigRequest;
import com.docreview.dto.response.Result;
import com.docreview.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置控制器
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Tag(name = "系统配置")
@RestController
@RequestMapping("/api/v1/system")
public class SystemConfigController {
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @Operation(summary = "获取所有配置")
    @SaCheckPermission("sys:config:query")
    @GetMapping("/config")
    public Result<Map<String, String>> getAllConfigs() {
        return Result.success(systemConfigService.getAllConfigs());
    }
    
    @Operation(summary = "获取指定配置")
    @SaCheckPermission("sys:config:query")
    @GetMapping("/config/{key}")
    public Result<String> getConfig(@PathVariable String key) {
        return Result.success(systemConfigService.getConfig(key));
    }
    
    @Operation(summary = "设置配置")
    @SaCheckPermission("sys:config:edit")
    @PutMapping("/config/{key}")
    public Result<Void> setConfig(@PathVariable String key, @RequestBody Map<String, String> body) {
        String value = body.get("value");
        systemConfigService.setConfig(key, value);
        return Result.success("设置成功");
    }
    
    @Operation(summary = "获取LDAP配置")
    @SaCheckPermission("sys:config:query")
    @GetMapping("/ldap-config")
    public Result<LdapConfigRequest> getLdapConfig() {
        return Result.success(systemConfigService.getLdapConfig());
    }
    
    @Operation(summary = "更新LDAP配置")
    @SaCheckPermission("sys:config:edit")
    @PutMapping("/ldap-config")
    public Result<Void> updateLdapConfig(@RequestBody LdapConfigRequest request) {
        systemConfigService.updateLdapConfig(request);
        return Result.success("更新成功");
    }
    
    @Operation(summary = "测试LDAP连接")
    @SaCheckPermission("sys:config:edit")
    @PostMapping("/ldap-test")
    public Result<Boolean> testLdapConnection(@RequestBody LdapConfigRequest request) {
        boolean success = systemConfigService.testLdapConnection(request);
        if (success) {
            return Result.success("连接成功", true);
        } else {
            return Result.error(500, "连接失败");
        }
    }
}
