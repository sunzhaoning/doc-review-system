package com.docreview.security;

import cn.dev33.satoken.stp.StpInterface;
import com.docreview.entity.Permission;
import com.docreview.entity.Role;
import com.docreview.service.PermissionService;
import com.docreview.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限接口实现
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Component
public class StpInterfaceImpl implements StpInterface {
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private PermissionService permissionService;
    
    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        try {
            Long userId = Long.parseLong(loginId.toString());
            List<Permission> permissions = permissionService.getPermissionsByUserId(userId);
            return permissions.stream()
                    .map(Permission::getPermCode)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * 返回一个账号所拥有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            Long userId = Long.parseLong(loginId.toString());
            List<Role> roles = roleService.getRolesByUserId(userId);
            return roles.stream()
                    .map(Role::getRoleCode)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
