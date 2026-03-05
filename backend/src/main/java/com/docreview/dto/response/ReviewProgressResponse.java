package com.docreview.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 评审进度响应
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
public class ReviewProgressResponse {
    
    /**
     * 文档ID
     */
    private Long documentId;
    
    /**
     * 文档标题
     */
    private String documentTitle;
    
    /**
     * 文档状态
     */
    private String documentStatus;
    
    /**
     * 总评审者数量
     */
    private Integer totalReviewers;
    
    /**
     * 已完成评审数量
     */
    private Integer completedReviewers;
    
    /**
     * 已通过数量
     */
    private Integer approvedCount;
    
    /**
     * 已拒绝数量
     */
    private Integer rejectedCount;
    
    /**
     * 评审进度百分比
     */
    private Integer progress;
    
    /**
     * 评审者详情列表
     */
    private List<ReviewerDetail> reviewers;
    
    @Data
    public static class ReviewerDetail {
        private Long reviewerId;
        private String reviewerName;
        private String status;
        private String decision;
        private LocalDateTime submittedAt;
    }
}
