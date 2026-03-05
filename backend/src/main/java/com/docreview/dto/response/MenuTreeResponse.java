package com.docreview.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 菜单树响应
 */
@Data
public class MenuTreeResponse {
    
    private Long id;
    
    private Long parentId;
    
    private String menuName;
    
    private String menuCode;
    
    private Integer menuType;
    
    private String path;
    
    private String component;
    
    private String perms;
    
    private String icon;
    
    private Integer sort;
    
    private Integer visible;
    
    private Integer status;
    
    private List<MenuTreeResponse> children;
}
