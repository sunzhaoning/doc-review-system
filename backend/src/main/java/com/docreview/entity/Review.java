package com.docreview.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 评审实体
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_review")
public class Review extends BaseEntity {
    
    /**
     * 文档ID
     */
    private Long documentId;
    
    /**
     * 评审者ID
     */
    private Long reviewerId;
    
    /**
     * 状态：PENDING/IN_PROGRESS/SUBMITTED
     */
    private String status;
    
    /**
     * 决定：APPROVED/REJECTED/REVISION_REQUIRED
     */
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
    
    /**
     * 提交时间
     */
    private LocalDateTime submittedAt;
}
