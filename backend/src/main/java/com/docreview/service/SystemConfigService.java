package com.docreview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.docreview.dto.request.LdapConfigRequest;
import com.docreview.entity.SystemConfig;

import java.util.Map;

/**
 * 系统配置服务接口
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public interface SystemConfigService extends IService<SystemConfig> {
    
    /**
     * 获取配置值
     */
    String getConfig(String key);
    
    /**
     * 设置配置值
     */
    void setConfig(String key, String value);
    
    /**
     * 获取所有配置
     */
    Map<String, String> getAllConfigs();
    
    /**
     * 获取LDAP配置
     */
    LdapConfigRequest getLdapConfig();
    
    /**
     * 更新LDAP配置
     */
    void updateLdapConfig(LdapConfigRequest request);
    
    /**
     * 测试LDAP连接
     */
    boolean testLdapConnection(LdapConfigRequest request);
    
    /**
     * 是否启用LDAP
     */
    boolean isLdapEnabled();
}
