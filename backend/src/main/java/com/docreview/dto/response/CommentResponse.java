package com.docreview.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论响应
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
public class CommentResponse {
    
    private Long id;
    
    private Long documentId;
    
    private Long reviewId;
    
    private Long authorId;
    
    private String authorName;
    
    private String authorAvatar;
    
    private String content;
    
    private String type;
    
    private String priority;
    
    private String position;
    
    private Long parentId;
    
    private Integer replyCount;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
