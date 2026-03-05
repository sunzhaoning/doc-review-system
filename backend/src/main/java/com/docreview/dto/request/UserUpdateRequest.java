package com.docreview.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 用户更新请求
 */
@Data
public class UserUpdateRequest {
    
    private String email;
    
    private String phone;
    
    private String realName;
    
    private String avatar;
    
    private Long deptId;
    
    private List<Long> roleIds;
}
