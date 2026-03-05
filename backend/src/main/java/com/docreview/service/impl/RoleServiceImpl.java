package com.docreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.docreview.dto.request.RoleCreateRequest;
import com.docreview.dto.request.RoleUpdateRequest;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.RoleResponse;
import com.docreview.entity.Role;
import com.docreview.entity.RoleMenu;
import com.docreview.entity.RolePermission;
import com.docreview.exception.BusinessException;
import com.docreview.exception.ErrorCode;
import com.docreview.mapper.MenuMapper;
import com.docreview.mapper.PermissionMapper;
import com.docreview.mapper.RoleMapper;
import com.docreview.mapper.RoleMenuMapper;
import com.docreview.mapper.RolePermissionMapper;
import com.docreview.service.RoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    
    @Autowired
    private MenuMapper menuMapper;
    
    @Autowired
    private PermissionMapper permissionMapper;
    
    @Autowired
    private RoleMenuMapper roleMenuMapper;
    
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    
    @Override
    public Role getByRoleCode(String roleCode) {
        return getOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, roleCode)
                .eq(Role::getDeleted, 0));
    }
    
    @Override
    public List<Role> getRolesByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }
    
    @Override
    public List<RoleResponse> getAllRoles() {
        return list(new LambdaQueryWrapper<Role>()
                .eq(Role::getDeleted, 0)
                .orderByAsc(Role::getId))
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResult<RoleResponse> getPage(Integer current, Integer size, String roleName, Integer status) {
        Page<Role> page = new Page<>(current, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getDeleted, 0);
        
        if (roleName != null && !roleName.isEmpty()) {
            wrapper.like(Role::getRoleName, roleName);
        }
        if (status != null) {
            wrapper.eq(Role::getStatus, status);
        }
        
        wrapper.orderByAsc(Role::getId);
        
        IPage<Role> rolePage = page(page, wrapper);
        
        PageResult<RoleResponse> result = new PageResult<>();
        result.setRecords(rolePage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
        result.setTotal(rolePage.getTotal());
        result.setSize(rolePage.getSize());
        result.setCurrent(rolePage.getCurrent());
        result.setPages(rolePage.getPages());
        
        return result;
    }
    
    @Override
    @Transactional
    public Long createRole(RoleCreateRequest request) {
        // 检查角色编码是否已存在
        if (getByRoleCode(request.getRoleCode()) != null) {
            throw new BusinessException(ErrorCode.DATA_EXISTS, "角色编码已存在");
        }
        
        Role role = new Role();
        BeanUtils.copyProperties(request, role);
        role.setStatus(1);
        
        save(role);
        
        // 分配菜单
        if (request.getMenuIds() != null && !request.getMenuIds().isEmpty()) {
            assignMenus(role.getId(), request.getMenuIds());
        }
        
        return role.getId();
    }
    
    @Override
    @Transactional
    public void updateRole(Long id, RoleUpdateRequest request) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "角色不存在");
        }
        
        BeanUtils.copyProperties(request, role);
        updateById(role);
    }
    
    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "角色不存在");
        }
        
        role.setDeleted(1);
        updateById(role);
        
        // 删除角色菜单关联
        roleMenuMapper.deleteByRoleId(id);
        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(id);
    }
    
    @Override
    public void updateStatus(Long id, Integer status) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "角色不存在");
        }
        
        role.setStatus(status);
        updateById(role);
    }
    
    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return menuMapper.selectMenuIdsByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public void assignMenus(Long roleId, List<Long> menuIds) {
        // 删除原有菜单
        roleMenuMapper.deleteByRoleId(roleId);
        
        // 分配新菜单
        if (menuIds != null && !menuIds.isEmpty()) {
            List<RoleMenu> list = menuIds.stream()
                    .map(menuId -> {
                        RoleMenu rm = new RoleMenu();
                        rm.setRoleId(roleId);
                        rm.setMenuId(menuId);
                        rm.setCreatedAt(LocalDateTime.now());
                        return rm;
                    })
                    .collect(Collectors.toList());
            roleMenuMapper.batchInsert(list);
        }
    }
    
    @Override
    public List<Long> getPermissionIdsByRoleId(Long roleId) {
        return permissionMapper.selectPermIdsByRoleId(roleId);
    }
    
    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        // 删除原有权限
        rolePermissionMapper.deleteByRoleId(roleId);
        
        // 分配新权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            List<RolePermission> list = permissionIds.stream()
                    .map(permId -> {
                        RolePermission rp = new RolePermission();
                        rp.setRoleId(roleId);
                        rp.setPermissionId(permId);
                        rp.setCreatedAt(LocalDateTime.now());
                        return rp;
                    })
                    .collect(Collectors.toList());
            rolePermissionMapper.batchInsert(list);
        }
    }
    
    private RoleResponse convertToResponse(Role role) {
        RoleResponse response = new RoleResponse();
        BeanUtils.copyProperties(role, response);
        response.setMenuIds(getMenuIdsByRoleId(role.getId()));
        response.setPermissionIds(getPermissionIdsByRoleId(role.getId()));
        return response;
    }
}
