package com.docreview.enums;

/**
 * 评审类型枚举
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public enum ReviewType {
    
    /**
     * 技术评审
     */
    TECHNICAL("TECHNICAL", "技术评审"),
    
    /**
     * 设计评审
     */
    DESIGN("DESIGN", "设计评审"),
    
    /**
     * 代码评审
     */
    CODE("CODE", "代码评审");
    
    private final String code;
    private final String desc;
    
    ReviewType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDesc() {
        return desc;
    }
}
