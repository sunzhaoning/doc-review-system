package com.docreview.enums;

/**
 * 评论类型枚举
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public enum CommentType {
    
    /**
     * 问题
     */
    ISSUE("ISSUE", "问题"),
    
    /**
     * 建议
     */
    SUGGESTION("SUGGESTION", "建议"),
    
    /**
     * 疑问
     */
    QUESTION("QUESTION", "疑问");
    
    private final String code;
    private final String desc;
    
    CommentType(String code, String desc) {
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
