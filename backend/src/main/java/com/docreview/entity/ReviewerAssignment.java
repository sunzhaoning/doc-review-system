package com.docreview.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评审者分配实体
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
@TableName("doc_reviewer_assignment")
public class ReviewerAssignment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 文档ID
     */
    private Long documentId;
    
    /**
     * 评审者ID
     */
    private Long reviewerId;
    
    /**
     * 状态：PENDING/REVIEWING/COMPLETED
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
