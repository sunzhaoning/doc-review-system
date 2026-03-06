package com.docreview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 角色创建请求
 */
@Data
public class RoleCreateRequest {
    
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50")
    private String roleName;
    
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50")
    private String roleCode;
    
    private String description;
    
    private Integer dataScope;
    
    private Integer status;
    
    private List<Long> menuIds;
    
    private List<Long> permissionIds;
}
