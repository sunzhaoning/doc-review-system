package com.docreview.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统角色实体
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 角色编码
     */
    private String roleCode;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 数据权限范围：1全部 2自定义 3本部门及下级 4本部门 5仅本人
     */
    private Integer dataScope;
    
    /**
     * 状态：0禁用 1启用
     */
    private Integer status;
}
