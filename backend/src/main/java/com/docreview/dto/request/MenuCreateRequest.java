package com.docreview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 菜单创建请求
 */
@Data
public class MenuCreateRequest {
    
    private Long parentId;
    
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50")
    private String menuName;
    
    @NotBlank(message = "菜单编码不能为空")
    @Size(max = 50, message = "菜单编码长度不能超过50")
    private String menuCode;
    
    private Integer menuType;
    
    @Size(max = 200, message = "路由地址长度不能超过200")
    private String path;
    
    @Size(max = 200, message = "组件路径长度不能超过200")
    private String component;
    
    @Size(max = 100, message = "权限标识长度不能超过100")
    private String perms;
    
    @Size(max = 100, message = "菜单图标长度不能超过100")
    private String icon;
    
    private Integer sort;
    
    private Integer visible;
    
    private Integer status;
}
