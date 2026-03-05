package com.docreview.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文档实体
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doc_document")
public class Document extends BaseEntity {
    
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文档描述
     */
    private String description;
    
    /**
     * MinIO文件路径
     */
    private String filePath;
    
    /**
     * 原始文件名
     */
    private String fileName;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 评审类型：TECHNICAL/DESIGN/CODE
     */
    private String reviewType;
    
    /**
     * 状态：DRAFT/PENDING/REVIEWING/REVISION/APPROVED/REJECTED/ARCHIVED
     */
    private String status;
    
    /**
     * 提交者ID
     */
    private Long submitterId;
    
    /**
     * 部门ID
     */
    private Long deptId;
    
    /**
     * 评审截止时间
     */
    private LocalDateTime deadline;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 是否已归档
     */
    private Boolean archived;
    
    /**
     * 归档时间
     */
    private LocalDateTime archivedAt;
}
