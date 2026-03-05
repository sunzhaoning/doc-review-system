package com.docreview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评审提交请求
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
public class ReviewSubmitRequest {
    
    /**
     * 评审决定：APPROVED/REJECTED/REVISION_REQUIRED
     */
    @NotBlank(message = "评审决定不能为空")
    private String decision;
    
    /**
     * 总体评价
     */
    private String overallComment;
    
    /**
     * 优点
     */
    private String pros;
    
    /**
     * 问题
     */
    private String cons;
    
    /**
     * 建议
     */
    private String suggestions;
}
