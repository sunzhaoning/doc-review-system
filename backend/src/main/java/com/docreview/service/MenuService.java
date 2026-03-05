package com.docreview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.docreview.dto.request.MenuCreateRequest;
import com.docreview.dto.request.MenuUpdateRequest;
import com.docreview.dto.response.MenuTreeResponse;
import com.docreview.entity.Menu;

import java.util.List;

/**
 * 菜单服务接口
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public interface MenuService extends IService<Menu> {
    
    /**
     * 根据用户ID查询菜单列表
     */
    List<Menu> getMenusByUserId(Long userId);
    
    /**
     * 获取菜单树
     */
    List<MenuTreeResponse> getMenuTree();
    
    /**
     * 获取用户菜单树（用于动态路由）
     */
    List<MenuTreeResponse> getUserMenuTree(Long userId);
    
    /**
     * 创建菜单
     */
    Long createMenu(MenuCreateRequest request);
    
    /**
     * 更新菜单
     */
    void updateMenu(Long id, MenuUpdateRequest request);
    
    /**
     * 删除菜单
     */
    void deleteMenu(Long id);
}
