package com.docreview.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.docreview.dto.request.UserCreateRequest;
import com.docreview.dto.request.UserUpdateRequest;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.UserResponse;
import com.docreview.entity.Role;
import com.docreview.entity.User;
import com.docreview.entity.UserRole;
import com.docreview.exception.BusinessException;
import com.docreview.exception.ErrorCode;
import com.docreview.mapper.RoleMapper;
import com.docreview.mapper.UserMapper;
import com.docreview.mapper.UserRoleMapper;
import com.docreview.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private RoleMapper roleMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getDeleted, 0));
    }
    
    @Override
    public boolean checkPassword(User user, String password) {
        return BCrypt.checkpw(password, user.getPassword());
    }
    
    @Override
    public String encodePassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    
    @Override
    public PageResult<UserResponse> getPage(Integer current, Integer size, String username, String realName, Integer status) {
        Page<User> page = new Page<>(current, size);
        IPage<User> userPage = baseMapper.selectPageWithDept(page, username, realName, status);
        
        PageResult<UserResponse> result = new PageResult<>();
        result.setRecords(userPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
        result.setTotal(userPage.getTotal());
        result.setSize(userPage.getSize());
        result.setCurrent(userPage.getCurrent());
        result.setPages(userPage.getPages());
        
        return result;
    }
    
    @Override
    @Transactional
    public Long createUser(UserCreateRequest request) {
        // 检查用户名是否已存在
        if (getByUsername(request.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        
        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(encodePassword(request.getPassword()));
        user.setStatus(1);
        user.setLdapEnabled(false);
        
        save(user);
        
        // 分配角色
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            assignRoles(user.getId(), request.getRoleIds());
        }
        
        return user.getId();
    }
    
    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        BeanUtils.copyProperties(request, user);
        // 不更新密码
        user.setPassword(null);
        
        updateById(user);
        
        // 更新角色关联
        if (request.getRoleIds() != null) {
            assignRoles(id, request.getRoleIds());
        }
    }
    
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 使用 MyBatis-Plus 的逻辑删除
        removeById(id);
        
        // 删除用户角色关联
        userRoleMapper.deleteByUserId(id);
    }
    
    @Override
    public void updateStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        user.setStatus(status);
        updateById(user);
    }
    
    @Override
    public void resetPassword(Long id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        user.setPassword(encodePassword(newPassword));
        updateById(user);
    }
    
    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        
        // 验证旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        
        // 设置新密码
        user.setPassword(encodePassword(newPassword));
        updateById(user);
    }
    
    @Override
    public List<Role> getRolesByUserId(Long userId) {
        return roleMapper.selectByUserId(userId);
    }
    
    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return baseMapper.selectRoleIdsByUserId(userId);
    }
    
    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        // 删除原有角色
        userRoleMapper.deleteByUserId(userId);
        
        // 分配新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            List<UserRole> list = roleIds.stream()
                    .map(roleId -> {
                        UserRole ur = new UserRole();
                        ur.setUserId(userId);
                        ur.setRoleId(roleId);
                        ur.setCreatedAt(LocalDateTime.now());
                        return ur;
                    })
                    .collect(Collectors.toList());
            userRoleMapper.batchInsert(list);
        }
    }
    
    @Override
    public List<UserResponse> search(String keyword, Integer limit) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getDeleted, 0)
               .eq(User::getStatus, 1);
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword));
        }
        
        wrapper.last("LIMIT " + Math.min(limit, 50));
        
        return list(wrapper).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response);
        response.setRoleIds(getRoleIdsByUserId(user.getId()));
        return response;
    }
}
