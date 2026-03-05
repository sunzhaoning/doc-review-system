package com.docreview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.docreview.dto.response.DocumentResponse;
import com.docreview.dto.response.PageResult;
import com.docreview.entity.Document;

/**
 * 归档服务接口
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public interface ArchiveService extends IService<Document> {
    
    /**
     * 归档文档
     */
    void archiveDocument(Long id);
    
    /**
     * 取消归档
     */
    void unarchiveDocument(Long id);
    
    /**
     * 搜索归档文档
     */
    PageResult<DocumentResponse> searchArchived(Integer current, Integer size, 
                                                  String keyword, String reviewType, 
                                                  String startDate, String endDate);
    
    /**
     * 批量归档
     */
    void batchArchive(java.util.List<Long> ids);
    
    /**
     * 导出评审报告
     */
    byte[] exportReviewReport(Long id);
}
