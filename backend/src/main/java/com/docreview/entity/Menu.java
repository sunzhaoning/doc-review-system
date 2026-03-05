package com.docreview.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统菜单实体
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class Menu extends BaseEntity {
    
    /**
     * 父菜单ID
     */
    private Long parentId;
    
    /**
     * 菜单名称
     */
    private String menuName;
    
    /**
     * 菜单编码
     */
    private String menuCode;
    
    /**
     * 菜单类型：1目录 2菜单 3按钮
     */
    private Integer menuType;
    
    /**
     * 路由地址
     */
    private String path;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 权限标识
     */
    private String perms;
    
    /**
     * 菜单图标
     */
    private String icon;
    
    /**
     * 排序号
     */
    private Integer sort;
    
    /**
     * 是否可见：0隐藏 1显示
     */
    private Integer visible;
    
    /**
     * 状态：0禁用 1启用
     */
    private Integer status;
}
