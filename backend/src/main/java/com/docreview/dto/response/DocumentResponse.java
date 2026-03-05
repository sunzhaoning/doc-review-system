package com.docreview.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档响应
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
public class DocumentResponse {
    
    private Long id;
    
    private String title;
    
    private String description;
    
    private String fileName;
    
    private Long fileSize;
    
    private String fileType;
    
    private String reviewType;
    
    private String status;
    
    private Long submitterId;
    
    private String submitterName;
    
    private Long deptId;
    
    private LocalDateTime deadline;
    
    private String version;
    
    private Boolean archived;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    /**
     * 评审进度
     */
    private ReviewProgress reviewProgress;
    
    @Data
    public static class ReviewProgress {
        private Integer total;
        private Integer completed;
        private Integer approved;
        private Integer rejected;
    }
}
