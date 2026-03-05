package com.docreview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.docreview.dto.request.RoleCreateRequest;
import com.docreview.dto.request.RoleUpdateRequest;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.RoleResponse;
import com.docreview.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public interface RoleService extends IService<Role> {
    
    /**
     * 根据角色编码查询
     */
    Role getByRoleCode(String roleCode);
    
    /**
     * 根据用户ID查询角色列表
     */
    List<Role> getRolesByUserId(Long userId);
    
    /**
     * 获取所有角色
     */
    List<RoleResponse> getAllRoles();
    
    /**
     * 分页查询角色
     */
    PageResult<RoleResponse> getPage(Integer current, Integer size, String roleName, Integer status);
    
    /**
     * 创建角色
     */
    Long createRole(RoleCreateRequest request);
    
    /**
     * 更新角色
     */
    void updateRole(Long id, RoleUpdateRequest request);
    
    /**
     * 删除角色
     */
    void deleteRole(Long id);
    
    /**
     * 更新角色状态
     */
    void updateStatus(Long id, Integer status);
    
    /**
     * 获取角色菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
    
    /**
     * 分配角色菜单
     */
    void assignMenus(Long roleId, List<Long> menuIds);
    
    /**
     * 获取角色权限ID列表
     */
    List<Long> getPermissionIdsByRoleId(Long roleId);
    
    /**
     * 分配角色权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);
}
