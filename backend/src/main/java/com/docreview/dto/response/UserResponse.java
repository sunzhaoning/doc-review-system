package com.docreview.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 用户响应
 */
@Data
public class UserResponse {
    
    private Long id;
    
    private String username;
    
    private String email;
    
    private String phone;
    
    private String realName;
    
    private String avatar;
    
    private Long deptId;
    
    private Integer status;
    
    private Boolean ldapEnabled;
    
    private List<Long> roleIds;
}
