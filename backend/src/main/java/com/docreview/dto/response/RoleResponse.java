package com.docreview.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 角色响应
 */
@Data
public class RoleResponse {
    
    private Long id;
    
    private String roleName;
    
    private String roleCode;
    
    private String description;
    
    private Integer dataScope;
    
    private Integer status;
    
    private List<Long> menuIds;
    
    private List<Long> permissionIds;
}
