package com.docreview.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论实体
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_comment")
public class Comment extends BaseEntity {
    
    /**
     * 文档ID
     */
    private Long documentId;
    
    /**
     * 评审ID
     */
    private Long reviewId;
    
    /**
     * 作者ID
     */
    private Long authorId;
    
    /**
     * 评论内容
     */
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
    
    /**
     * 父评论ID
     */
    private Long parentId;
}
