package com.docreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docreview.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色菜单关联 Mapper
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
    
    /**
     * 删除角色的所有菜单
     */
    int deleteByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 批量插入角色菜单
     */
    int batchInsert(@Param("list") java.util.List<RoleMenu> list);
}
