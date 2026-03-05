package com.docreview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.docreview.dto.request.PermissionCreateRequest;
import com.docreview.dto.request.PermissionUpdateRequest;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.PermissionResponse;
import com.docreview.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public interface PermissionService extends IService<Permission> {
    
    /**
     * 根据用户ID查询权限列表
     */
    List<Permission> getPermissionsByUserId(Long userId);
    
    /**
     * 获取所有权限
     */
    List<PermissionResponse> getAllPermissions();
    
    /**
     * 分页查询权限
     */
    PageResult<PermissionResponse> getPage(Integer current, Integer size, String permName, String resourceType);
    
    /**
     * 创建权限
     */
    Long createPermission(PermissionCreateRequest request);
    
    /**
     * 更新权限
     */
    void updatePermission(Long id, PermissionUpdateRequest request);
    
    /**
     * 删除权限
     */
    void deletePermission(Long id);
}
