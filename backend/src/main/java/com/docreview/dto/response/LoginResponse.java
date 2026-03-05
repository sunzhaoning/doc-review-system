package com.docreview.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 登录响应
 */
@Data
public class LoginResponse {
    
    private String token;
    
    private UserInfo userInfo;
    
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private String avatar;
        private List<String> roles;
        private List<String> permissions;
    }
}
