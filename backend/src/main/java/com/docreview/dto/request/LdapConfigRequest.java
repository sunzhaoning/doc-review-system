package com.docreview.dto.request;

import lombok.Data;

/**
 * LDAP配置请求
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
public class LdapConfigRequest {
    
    /**
     * 是否启用LDAP
     */
    private Boolean enabled;
    
    /**
     * LDAP服务器地址
     */
    private String url;
    
    /**
     * Base DN
     */
    private String baseDn;
    
    /**
     * 绑定账号
     */
    private String bindDn;
    
    /**
     * 绑定密码
     */
    private String bindPassword;
    
    /**
     * 用户名属性
     */
    private String userAttribute;
    
    /**
     * 邮箱属性
     */
    private String emailAttribute;
    
    /**
     * 姓名属性
     */
    private String nameAttribute;
}
