package com.docreview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 评论创建请求
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
public class CommentCreateRequest {
    
    /**
     * 文档ID
     */
    @NotNull(message = "文档ID不能为空")
    private Long documentId;
    
    /**
     * 评审ID（可选）
     */
    private Long reviewId;
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    /**
     * 类型：ISSUE/SUGGESTION/QUESTION
     */
    private String type;
    
    /**
     * 优先级：HIGH/MEDIUM/LOW
     */
    private String priority;
    
    /**
     * 行内批注位置
     */
    private String position;
}
