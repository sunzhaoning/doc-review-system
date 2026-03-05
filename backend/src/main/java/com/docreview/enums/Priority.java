package com.docreview.enums;

/**
 * 优先级枚举
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public enum Priority {
    
    HIGH("HIGH", "高"),
    MEDIUM("MEDIUM", "中"),
    LOW("LOW", "低");
    
    private final String code;
    private final String desc;
    
    Priority(String code, String desc) {
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
