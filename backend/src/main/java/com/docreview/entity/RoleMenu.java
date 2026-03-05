package com.docreview.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色菜单关联实体
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
@TableName("sys_role_menu")
public class RoleMenu implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 菜单ID
     */
    private Long menuId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
