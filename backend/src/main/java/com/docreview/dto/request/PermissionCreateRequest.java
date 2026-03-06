package com.docreview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 权限创建请求
 */
@Data
public class PermissionCreateRequest {
    
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50")
    private String permName;
    
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100")
    private String permCode;
    
    @NotBlank(message = "资源类型不能为空")
    @Size(max = 20, message = "资源类型长度不能超过20")
    private String resourceType;
    
    @Size(max = 200, message = "资源URL长度不能超过200")
    private String resourceUrl;
    
    @Size(max = 10, message = "HTTP方法长度不能超过10")
    private String method;
    
    private String description;
}
