package com.docreview.enums;

/**
 * 评审决定枚举
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public enum ReviewDecision {
    
    /**
     * 通过
     */
    APPROVED("APPROVED", "通过"),
    
    /**
     * 拒绝
     */
    REJECTED("REJECTED", "拒绝"),
    
    /**
     * 需要修改
     */
    REVISION_REQUIRED("REVISION_REQUIRED", "需要修改");
    
    private final String code;
    private final String desc;
    
    ReviewDecision(String code, String desc) {
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
