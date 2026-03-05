package com.docreview.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统权限实体
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class Permission extends BaseEntity {
    
    /**
     * 权限名称
     */
    private String permName;
    
    /**
     * 权限编码
     */
    private String permCode;
    
    /**
     * 资源类型：menu/button/api
     */
    private String resourceType;
    
    /**
     * 资源URL
     */
    private String resourceUrl;
    
    /**
     * HTTP方法
     */
    private String method;
    
    /**
     * 权限描述
     */
    private String description;
}
