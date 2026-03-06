package com.docreview.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限响应
 */
@Data
public class PermissionResponse {
    
    private Long id;
    
    private String permName;
    
    private String permCode;
    
    private String resourceType;
    
    private String resourceUrl;
    
    private String method;
    
    private String description;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
