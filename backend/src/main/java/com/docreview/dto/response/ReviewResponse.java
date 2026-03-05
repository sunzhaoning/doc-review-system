package com.docreview.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评审响应
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
public class ReviewResponse {
    
    private Long id;
    
    private Long documentId;
    
    private String documentTitle;
    
    private Long reviewerId;
    
    private String reviewerName;
    
    private String status;
    
    private String decision;
    
    private String overallComment;
    
    private String pros;
    
    private String cons;
    
    private String suggestions;
    
    private LocalDateTime submittedAt;
    
    private LocalDateTime createdAt;
}
