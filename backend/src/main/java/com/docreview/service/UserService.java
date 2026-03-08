package com.docreview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.docreview.dto.request.UserCreateRequest;
import com.docreview.dto.request.UserUpdateRequest;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.UserResponse;
import com.docreview.entity.Role;
import com.docreview.entity.User;

import java.util.List;

/**
 * 用户服务接口
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public interface UserService extends IService<User> {
    
    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);
    
    /**
     * 校验密码
     */
    boolean checkPassword(User user, String password);
    
    /**
     * 加密密码
     */
    String encodePassword(String password);
    
    /**
     * 分页查询用户
     */
    PageResult<UserResponse> getPage(Integer current, Integer size, String username, String realName, Integer status);
    
    /**
     * 创建用户
     */
    Long createUser(UserCreateRequest request);
    
    /**
     * 更新用户
     */
    void updateUser(Long id, UserUpdateRequest request);
    
    /**
     * 删除用户
     */
    void deleteUser(Long id);
    
    /**
     * 更新用户状态
     */
    void updateStatus(Long id, Integer status);
    
    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);
    
    /**
     * 获取用户角色
     */
    List<Role> getRolesByUserId(Long userId);
    
    /**
     * 获取用户角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long userId);
    
    /**
     * 分配用户角色
     */
    void assignRoles(Long userId, List<Long> roleIds);
    
    /**
     * 搜索用户
     */
    List<UserResponse> search(String keyword, Integer limit);
}
