package com.docreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.docreview.dto.request.PermissionCreateRequest;
import com.docreview.dto.request.PermissionUpdateRequest;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.PermissionResponse;
import com.docreview.entity.Permission;
import com.docreview.exception.BusinessException;
import com.docreview.exception.ErrorCode;
import com.docreview.mapper.PermissionMapper;
import com.docreview.service.PermissionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限服务实现
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    
    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }
    
    @Override
    public List<PermissionResponse> getAllPermissions() {
        return list(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getDeleted, 0)
                .orderByAsc(Permission::getId))
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResult<PermissionResponse> getPage(Integer current, Integer size, String permName, String resourceType) {
        Page<Permission> page = new Page<>(current, size);
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getDeleted, 0);
        
        if (permName != null && !permName.isEmpty()) {
            wrapper.like(Permission::getPermName, permName);
        }
        if (resourceType != null && !resourceType.isEmpty()) {
            wrapper.eq(Permission::getResourceType, resourceType);
        }
        
        wrapper.orderByAsc(Permission::getId);
        
        IPage<Permission> permPage = page(page, wrapper);
        
        PageResult<PermissionResponse> result = new PageResult<>();
        result.setRecords(permPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
        result.setTotal(permPage.getTotal());
        result.setSize(permPage.getSize());
        result.setCurrent(permPage.getCurrent());
        result.setPages(permPage.getPages());
        
        return result;
    }
    
    @Override
    @Transactional
    public Long createPermission(PermissionCreateRequest request) {
        // 检查权限编码是否已存在
        Permission existing = getOne(new LambdaQueryWrapper<Permission>()
                .eq(Permission::getPermCode, request.getPermCode())
                .eq(Permission::getDeleted, 0));
        if (existing != null) {
            throw new BusinessException(ErrorCode.DATA_EXISTS, "权限编码已存在");
        }
        
        Permission permission = new Permission();
        BeanUtils.copyProperties(request, permission);
        
        save(permission);
        return permission.getId();
    }
    
    @Override
    @Transactional
    public void updatePermission(Long id, PermissionUpdateRequest request) {
        Permission permission = getById(id);
        if (permission == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "权限不存在");
        }
        
        BeanUtils.copyProperties(request, permission);
        updateById(permission);
    }
    
    @Override
    @Transactional
    public void deletePermission(Long id) {
        Permission permission = getById(id);
        if (permission == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "权限不存在");
        }
        
        permission.setDeleted(1);
        updateById(permission);
    }
    
    private PermissionResponse convertToResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        BeanUtils.copyProperties(permission, response);
        return response;
    }
}
