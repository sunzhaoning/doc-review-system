package com.docreview.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档上传请求
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
public class DocumentUploadRequest {
    
    @NotBlank(message = "文档标题不能为空")
    private String title;
    
    private String description;
    
    @NotBlank(message = "评审类型不能为空")
    private String reviewType;
    
    /**
     * 评审者ID列表
     */
    private List<Long> reviewerIds;
    
    /**
     * 评审截止时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime deadline;
}
