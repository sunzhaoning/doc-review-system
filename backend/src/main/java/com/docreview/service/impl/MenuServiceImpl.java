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
import java.util.Comparator;
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
        return buildMenuTree(menus);
    }
    
    @Override
    public List<MenuTreeResponse> getUserMenuTree(Long userId) {
        List<Menu> menus = getMenusByUserId(userId);
        return buildMenuTree(menus);
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
     * 构建菜单树（支持任意顺序的菜单列表）
     */
    private List<MenuTreeResponse> buildMenuTree(List<Menu> menus) {
        if (menus == null || menus.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 先将所有菜单转换为响应对象，并按 parentId 分组
        Map<Long, List<MenuTreeResponse>> menuMap = menus.stream()
                .map(menu -> {
                    MenuTreeResponse node = new MenuTreeResponse();
                    BeanUtils.copyProperties(menu, node);
                    return node;
                })
                .collect(Collectors.groupingBy(MenuTreeResponse::getParentId));
        
        // 为每个菜单设置子菜单
        for (MenuTreeResponse node : menuMap.values().stream().flatMap(List::stream).collect(Collectors.toList())) {
            List<MenuTreeResponse> children = menuMap.get(node.getId());
            if (children != null && !children.isEmpty()) {
                // 按 sort 排序子菜单
                children.sort(Comparator.comparingInt(MenuTreeResponse::getSort));
                node.setChildren(children);
            }
        }
        
        // 获取根节点（parentId = 0）并排序
        List<MenuTreeResponse> rootMenus = menuMap.getOrDefault(0L, new ArrayList<>());
        rootMenus.sort(Comparator.comparingInt(MenuTreeResponse::getSort));
        
        return rootMenus;
    }
}
