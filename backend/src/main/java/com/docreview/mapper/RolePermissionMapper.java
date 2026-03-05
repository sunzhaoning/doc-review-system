package com.docreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docreview.entity.RolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色权限关联 Mapper
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
    
    /**
     * 删除角色的所有权限
     */
    int deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 批量插入角色权限
     */
    int batchInsert(@Param("list") java.util.List<RolePermission> list);
}
