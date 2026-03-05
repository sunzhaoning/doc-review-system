package com.docreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.docreview.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户 Mapper
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户ID查询角色列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询权限列表
     */
    List<String> selectPermCodesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 分页查询用户（带部门信息）
     */
    IPage<User> selectPageWithDept(Page<User> page, @Param("username") String username,
                                    @Param("realName") String realName, @Param("status") Integer status);
}
