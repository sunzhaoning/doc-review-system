package com.docreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docreview.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 Mapper
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {
    
    /**
     * 根据用户ID查询菜单列表
     */
    List<Menu> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
    
    /**
     * 查询所有菜单（树形结构）
     */
    List<Menu> selectAllMenus();
}
