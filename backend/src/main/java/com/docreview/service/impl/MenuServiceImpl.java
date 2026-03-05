package com.docreview.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.docreview.dto.request.MenuCreateRequest;
import com.docreview.dto.request.MenuUpdateRequest;
import com.docreview.dto.response.MenuTreeResponse;
import com.docreview.entity.Menu;
import com.docreview.exception.BusinessException;
import com.docreview.exception.ErrorCode;
import com.docreview.mapper.MenuMapper;
import com.docreview.service.MenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务实现
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {
    
    @Override
    public List<Menu> getMenusByUserId(Long userId) {
        return baseMapper.selectByUserId(userId);
    }
    
    @Override
    public List<MenuTreeResponse> getMenuTree() {
        List<Menu> menus = baseMapper.selectAllMenus();
        return buildMenuTree(menus, 0L);
    }
    
    @Override
    public List<MenuTreeResponse> getUserMenuTree(Long userId) {
        List<Menu> menus = getMenusByUserId(userId);
        return buildMenuTree(menus, 0L);
    }
    
    @Override
    @Transactional
    public Long createMenu(MenuCreateRequest request) {
        Menu menu = new Menu();
        BeanUtils.copyProperties(request, menu);
        menu.setStatus(1);
        menu.setVisible(1);
        
        save(menu);
        return menu.getId();
    }
    
    @Override
    @Transactional
    public void updateMenu(Long id, MenuUpdateRequest request) {
        Menu menu = getById(id);
        if (menu == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "菜单不存在");
        }
        
        BeanUtils.copyProperties(request, menu);
        updateById(menu);
    }
    
    @Override
    @Transactional
    public void deleteMenu(Long id) {
        Menu menu = getById(id);
        if (menu == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "菜单不存在");
        }
        
        // 检查是否有子菜单
        long count = count(new LambdaQueryWrapper<Menu>()
                .eq(Menu::getParentId, id)
                .eq(Menu::getDeleted, 0));
        if (count > 0) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "存在子菜单，无法删除");
        }
        
        menu.setDeleted(1);
        updateById(menu);
    }
    
    /**
     * 构建菜单树
     */
    private List<MenuTreeResponse> buildMenuTree(List<Menu> menus, Long parentId) {
        List<MenuTreeResponse> tree = new ArrayList<>();
        
        for (Menu menu : menus) {
            if (menu.getParentId().equals(parentId)) {
                MenuTreeResponse node = new MenuTreeResponse();
                BeanUtils.copyProperties(menu, node);
                node.setChildren(buildMenuTree(menus, menu.getId()));
                tree.add(node);
            }
        }
        
        return tree;
    }
}
