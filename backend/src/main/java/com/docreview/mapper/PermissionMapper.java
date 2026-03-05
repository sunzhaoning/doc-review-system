package com.docreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docreview.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限 Mapper
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
    
    /**
     * 根据用户ID查询权限列表
     */
    List<Permission> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询权限ID列表
     */
    List<Long> selectPermIdsByRoleId(@Param("roleId") Long roleId);
}
