package com.docreview.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.docreview.dto.request.LdapConfigRequest;
import com.docreview.entity.SystemConfig;
import com.docreview.mapper.SystemConfigMapper;
import com.docreview.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置服务实现
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Slf4j
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
    
    @Autowired
    private SystemConfigMapper systemConfigMapper;
    
    // LDAP配置键
    private static final String LDAP_ENABLED = "ldap.enabled";
    private static final String LDAP_URL = "ldap.url";
    private static final String LDAP_BASE_DN = "ldap.base-dn";
    private static final String LDAP_BIND_DN = "ldap.bind-dn";
    private static final String LDAP_BIND_PASSWORD = "ldap.bind-password";
    private static final String LDAP_USER_ATTRIBUTE = "ldap.user-attribute";
    private static final String LDAP_EMAIL_ATTRIBUTE = "ldap.email-attribute";
    private static final String LDAP_NAME_ATTRIBUTE = "ldap.name-attribute";
    
    // 密码加密密钥（实际项目中应从配置文件读取）
    private static final String SECRET_KEY = "doc-review-system-secret";
    
    @Override
    public String getConfig(String key) {
        SystemConfig config = getOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, key));
        return config != null ? config.getConfigValue() : null;
    }
    
    @Override
    @Transactional
    public void setConfig(String key, String value) {
        SystemConfig config = getOne(new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, key));
        
        if (config == null) {
            config = new SystemConfig();
            config.setConfigKey(key);
            config.setConfigValue(value);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            save(config);
        } else {
            config.setConfigValue(value);
            config.setUpdatedAt(LocalDateTime.now());
            updateById(config);
        }
    }
    
    @Override
    public Map<String, String> getAllConfigs() {
        List<SystemConfig> configs = list(new LambdaQueryWrapper<SystemConfig>()
                .orderByAsc(SystemConfig::getId));
        
        Map<String, String> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), config.getConfigValue());
        }
        return result;
    }
    
    @Override
    public LdapConfigRequest getLdapConfig() {
        LdapConfigRequest request = new LdapConfigRequest();
        request.setEnabled(Boolean.parseBoolean(getConfig(LDAP_ENABLED)));
        request.setUrl(getConfig(LDAP_URL));
        request.setBaseDn(getConfig(LDAP_BASE_DN));
        request.setBindDn(getConfig(LDAP_BIND_DN));
        request.setBindPassword(decryptPassword(getConfig(LDAP_BIND_PASSWORD)));
        request.setUserAttribute(getConfig(LDAP_USER_ATTRIBUTE));
        request.setEmailAttribute(getConfig(LDAP_EMAIL_ATTRIBUTE));
        request.setNameAttribute(getConfig(LDAP_NAME_ATTRIBUTE));
        return request;
    }
    
    @Override
    @Transactional
    public void updateLdapConfig(LdapConfigRequest request) {
        setConfig(LDAP_ENABLED, String.valueOf(request.getEnabled()));
        setConfig(LDAP_URL, request.getUrl());
        setConfig(LDAP_BASE_DN, request.getBaseDn());
        setConfig(LDAP_BIND_DN, request.getBindDn());
        setConfig(LDAP_BIND_PASSWORD, encryptPassword(request.getBindPassword()));
        setConfig(LDAP_USER_ATTRIBUTE, request.getUserAttribute());
        setConfig(LDAP_EMAIL_ATTRIBUTE, request.getEmailAttribute());
        setConfig(LDAP_NAME_ATTRIBUTE, request.getNameAttribute());
        
        log.info("更新LDAP配置: enabled={}", request.getEnabled());
    }
    
    @Override
    public boolean testLdapConnection(LdapConfigRequest request) {
        // TODO: 实际的LDAP连接测试
        // 这里简化实现，实际项目中应该使用LDAP SDK进行连接测试
        try {
            // 模拟测试
            if (request.getUrl() == null || request.getUrl().isEmpty()) {
                return false;
            }
            if (request.getBaseDn() == null || request.getBaseDn().isEmpty()) {
                return false;
            }
            
            log.info("测试LDAP连接: url={}, baseDn={}", request.getUrl(), request.getBaseDn());
            return true;
        } catch (Exception e) {
            log.error("LDAP连接测试失败", e);
            return false;
        }
    }
    
    @Override
    public boolean isLdapEnabled() {
        String enabled = getConfig(LDAP_ENABLED);
        return Boolean.parseBoolean(enabled);
    }
    
    /**
     * 加密密码
     */
    private String encryptPassword(String password) {
        if (password == null || password.isEmpty()) {
            return "";
        }
        try {
            return SecureUtil.aes(SECRET_KEY.getBytes()).encryptHex(password);
        } catch (Exception e) {
            log.error("密码加密失败", e);
            return password;
        }
    }
    
    /**
     * 解密密码
     */
    private String decryptPassword(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) {
            return "";
        }
        try {
            return SecureUtil.aes(SECRET_KEY.getBytes()).decryptStr(encrypted);
        } catch (Exception e) {
            log.error("密码解密失败", e);
            return encrypted;
        }
    }
}
