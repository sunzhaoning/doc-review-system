package com.docreview.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色更新请求
 */
@Data
public class RoleUpdateRequest {
    
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;
    
    @Size(max = 50, message = "角色编码长度不能超过50")
    private String roleCode;
    
    private String description;
    
    private Integer dataScope;
    
    private Integer status;
    
    private List<Long> menuIds;
    
    private List<Long> permissionIds;
}
