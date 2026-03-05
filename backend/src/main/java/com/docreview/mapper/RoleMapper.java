package com.docreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docreview.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
    
    /**
     * 根据用户ID查询角色列表
     */
    List<Role> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据菜单ID查询角色列表
     */
    List<Long> selectRoleIdsByMenuId(@Param("menuId") Long menuId);
}
