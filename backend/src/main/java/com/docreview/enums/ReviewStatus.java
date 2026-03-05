package com.docreview.enums;

/**
 * 评审状态枚举
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public enum ReviewStatus {
    
    /**
     * 待评审
     */
    PENDING("PENDING", "待评审"),
    
    /**
     * 评审中
     */
    IN_PROGRESS("IN_PROGRESS", "评审中"),
    
    /**
     * 已提交
     */
    SUBMITTED("SUBMITTED", "已提交");
    
    private final String code;
    private final String desc;
    
    ReviewStatus(String code, String desc) {
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
