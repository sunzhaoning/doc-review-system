package com.docreview.enums;

/**
 * 文档状态枚举
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public enum DocumentStatus {
    
    /**
     * 草稿
     */
    DRAFT("DRAFT", "草稿"),
    
    /**
     * 待评审
     */
    PENDING("PENDING", "待评审"),
    
    /**
     * 评审中
     */
    REVIEWING("REVIEWING", "评审中"),
    
    /**
     * 待修改
     */
    REVISION("REVISION", "待修改"),
    
    /**
     * 已通过
     */
    APPROVED("APPROVED", "已通过"),
    
    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝"),
    
    /**
     * 已归档
     */
    ARCHIVED("ARCHIVED", "已归档");
    
    private final String code;
    private final String desc;
    
    DocumentStatus(String code, String desc) {
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
