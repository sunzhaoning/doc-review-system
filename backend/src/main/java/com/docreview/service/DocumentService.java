package com.docreview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.docreview.dto.request.DocumentUploadRequest;
import com.docreview.dto.response.DocumentResponse;
import com.docreview.dto.response.PageResult;
import com.docreview.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档服务接口
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public interface DocumentService extends IService<Document> {
    
    /**
     * 上传文档
     */
    Long uploadDocument(MultipartFile file, DocumentUploadRequest request);
    
    /**
     * 分页查询文档
     */
    PageResult<DocumentResponse> getPage(Integer current, Integer size, String title, 
                                          String status, String reviewType, Long submitterId);
    
    /**
     * 获取文档详情
     */
    DocumentResponse getDetail(Long id);
    
    /**
     * 更新文档信息
     */
    void updateDocument(Long id, DocumentUploadRequest request);
    
    /**
     * 删除文档
     */
    void deleteDocument(Long id);
    
    /**
     * 提交评审
     */
    void submitReview(Long id, List<Long> reviewerIds);
    
    /**
     * 撤回评审
     */
    void withdrawReview(Long id);
    
    /**
     * 获取文档预览URL
     */
    String getPreviewUrl(Long id);
    
    /**
     * 下载文档
     */
    byte[] downloadDocument(Long id);
    
    /**
     * 获取我的文档
     */
    PageResult<DocumentResponse> getMyDocuments(Integer current, Integer size, String status);
}
