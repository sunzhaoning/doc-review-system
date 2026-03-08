package com.docreview.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.docreview.dto.request.DocumentUploadRequest;
import com.docreview.dto.response.DocumentResponse;
import com.docreview.dto.response.PageResult;
import com.docreview.entity.Document;
import com.docreview.entity.ReviewerAssignment;
import com.docreview.entity.User;
import com.docreview.enums.DocumentStatus;
import com.docreview.exception.BusinessException;
import com.docreview.exception.ErrorCode;
import com.docreview.mapper.DocumentMapper;
import com.docreview.mapper.ReviewerAssignmentMapper;
import com.docreview.mapper.UserMapper;
import com.docreview.service.DocumentService;
import com.docreview.service.FileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档服务实现
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Slf4j
@Service
public class DocumentServiceImpl extends ServiceImpl<DocumentMapper, Document> implements DocumentService {
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ReviewerAssignmentMapper reviewerAssignmentMapper;
    
    @Value("${file.max-size:52428800}")
    private Long maxFileSize;
    
    @Value("${file.allowed-types:pdf,docx,doc,xlsx,xls,pptx,ppt,txt,md}")
    private String allowedTypes;
    
    @Override
    @Transactional
    public Long uploadDocument(MultipartFile file, DocumentUploadRequest request) {
        // 校验文件
        validateFile(file);
        
        // 获取当前用户
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        
        // 生成文件路径
        String fileExtension = getFileExtension(file.getOriginalFilename());
        String objectName = String.format("documents/%s/%s.%s", 
                userId, IdUtil.simpleUUID(), fileExtension);
        
        try {
            // 上传文件到 MinIO
            String filePath = fileService.uploadFile(file, objectName);
            
            // 创建文档记录
            Document document = new Document();
            BeanUtils.copyProperties(request, document);
            document.setFilePath(filePath);
            document.setFileName(file.getOriginalFilename());
            document.setFileSize(file.getSize());
            document.setFileType(fileExtension);
            document.setStatus(DocumentStatus.DRAFT.getCode());
            document.setSubmitterId(userId);
            document.setDeptId(user.getDeptId());
            document.setVersion("v1.0");
            document.setArchived(false);
            
            save(document);
            
            log.info("上传文档成功: id={}, title={}", document.getId(), document.getTitle());
            return document.getId();
        } catch (Exception e) {
            log.error("上传文档失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
        }
    }
    
    @Override
    public PageResult<DocumentResponse> getPage(Integer current, Integer size, String title, 
                                                  String status, String reviewType, Long submitterId) {
        Page<Document> page = new Page<>(current, size);
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getDeleted, 0);
        
        if (StringUtils.hasText(title)) {
            wrapper.like(Document::getTitle, title);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Document::getStatus, status);
        }
        if (StringUtils.hasText(reviewType)) {
            wrapper.eq(Document::getReviewType, reviewType);
        }
        if (submitterId != null) {
            wrapper.eq(Document::getSubmitterId, submitterId);
        }
        
        wrapper.orderByDesc(Document::getCreatedAt);
        
        IPage<Document> documentPage = page(page, wrapper);
        
        PageResult<DocumentResponse> result = new PageResult<>();
        result.setRecords(documentPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
        result.setTotal(documentPage.getTotal());
        result.setSize(documentPage.getSize());
        result.setCurrent(documentPage.getCurrent());
        result.setPages(documentPage.getPages());
        
        return result;
    }
    
    @Override
    public DocumentResponse getDetail(Long id) {
        Document document = getById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        return convertToResponse(document);
    }
    
    @Override
    @Transactional
    public void updateDocument(Long id, DocumentUploadRequest request) {
        Document document = getById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        // 只有草稿状态才能编辑
        if (!DocumentStatus.DRAFT.getCode().equals(document.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "文档状态不允许编辑");
        }
        
        // 检查是否是提交者
        Long userId = StpUtil.getLoginIdAsLong();
        if (!document.getSubmitterId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权编辑此文档");
        }
        
        BeanUtils.copyProperties(request, document);
        updateById(document);
        
        log.info("更新文档成功: id={}", id);
    }
    
    @Override
    @Transactional
    public void deleteDocument(Long id) {
        Document document = getById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        // 检查是否是提交者
        Long userId = StpUtil.getLoginIdAsLong();
        if (!document.getSubmitterId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权删除此文档");
        }
        
        // 只有草稿状态才能删除
        if (!DocumentStatus.DRAFT.getCode().equals(document.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "文档状态不允许删除");
        }
        
        // 删除 MinIO 文件
        try {
            fileService.deleteFile(document.getFilePath());
        } catch (Exception e) {
            log.warn("删除文件失败: {}", document.getFilePath(), e);
        }
        
        // 逻辑删除
        document.setDeleted(1);
        updateById(document);
        
        log.info("删除文档成功: id={}", id);
    }
    
    @Override
    @Transactional
    public void submitReview(Long id, List<Long> reviewerIds) {
        Document document = getById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        // 检查是否是提交者
        Long userId = StpUtil.getLoginIdAsLong();
        if (!document.getSubmitterId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权提交此文档");
        }
        
        // 只有草稿或待修改状态才能提交
        if (!DocumentStatus.DRAFT.getCode().equals(document.getStatus()) 
                && !DocumentStatus.REVISION.getCode().equals(document.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "文档状态不允许提交");
        }
        
        // 检查评审者数量
        if (reviewerIds == null || reviewerIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "至少选择一位评审者");
        }
        
        // 更新文档状态
        document.setStatus(DocumentStatus.PENDING.getCode());
        updateById(document);
        
        // 创建评审者分配
        for (Long reviewerId : reviewerIds) {
            ReviewerAssignment assignment = new ReviewerAssignment();
            assignment.setDocumentId(id);
            assignment.setReviewerId(reviewerId);
            assignment.setStatus("PENDING");
            assignment.setCreatedAt(LocalDateTime.now());
            reviewerAssignmentMapper.insert(assignment);
        }
        
        log.info("提交评审成功: documentId={}, reviewers={}", id, reviewerIds);
    }
    
    @Override
    @Transactional
    public void withdrawReview(Long id) {
        Document document = getById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        // 检查是否是提交者
        Long userId = StpUtil.getLoginIdAsLong();
        if (!document.getSubmitterId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权撤回此文档");
        }
        
        // 只有待评审状态才能撤回
        if (!DocumentStatus.PENDING.getCode().equals(document.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "文档状态不允许撤回");
        }
        
        // 更新文档状态
        document.setStatus(DocumentStatus.DRAFT.getCode());
        updateById(document);
        
        // 删除评审者分配
        reviewerAssignmentMapper.delete(new LambdaQueryWrapper<ReviewerAssignment>()
                .eq(ReviewerAssignment::getDocumentId, id));
        
        log.info("撤回评审成功: documentId={}", id);
    }
    
    @Override
    public String getPreviewUrl(Long id) {
        Document document = getById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        try {
            return fileService.getPreviewUrl(document.getFilePath(), 3600);
        } catch (Exception e) {
            log.error("获取预览URL失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "获取预览URL失败");
        }
    }
    
    @Override
    public byte[] downloadDocument(Long id) {
        Document document = getById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        try (InputStream inputStream = fileService.downloadFile(document.getFilePath())) {
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("下载文档失败", e);
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "下载文档失败");
        }
    }
    
    @Override
    public PageResult<DocumentResponse> getMyDocuments(Integer current, Integer size, String status) {
        Long userId = StpUtil.getLoginIdAsLong();
        return getPage(current, size, null, status, null, userId);
    }
    
    @Override
    public java.util.Map<String, Object> getStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // 文档总数（未删除的）
        long totalDocuments = count(new LambdaQueryWrapper<Document>()
                .eq(Document::getDeleted, 0));
        stats.put("totalDocuments", totalDocuments);
        
        // 待评审数
        long pendingReviews = count(new LambdaQueryWrapper<Document>()
                .eq(Document::getDeleted, 0)
                .eq(Document::getStatus, DocumentStatus.PENDING.getCode()));
        stats.put("pendingReviews", pendingReviews);
        
        // 已完成评审数
        long completedReviews = count(new LambdaQueryWrapper<Document>()
                .eq(Document::getDeleted, 0)
                .in(Document::getStatus, DocumentStatus.APPROVED.getCode(), DocumentStatus.REJECTED.getCode()));
        stats.put("completedReviews", completedReviews);
        
        // 已归档数
        long archivedDocuments = count(new LambdaQueryWrapper<Document>()
                .eq(Document::getDeleted, 0)
                .eq(Document::getArchived, true));
        stats.put("archivedDocuments", archivedDocuments);
        
        return stats;
    }
    
    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE, 
                    "文件大小超过限制，最大允许 " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        // 检查文件类型
        String fileExtension = getFileExtension(file.getOriginalFilename());
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        if (!allowedTypeList.contains(fileExtension.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, 
                    "文件类型不允许，允许的类型: " + allowedTypes);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * 转换为响应对象
     */
    private DocumentResponse convertToResponse(Document document) {
        DocumentResponse response = new DocumentResponse();
        BeanUtils.copyProperties(document, response);
        
        // 获取提交者信息
        User submitter = userMapper.selectById(document.getSubmitterId());
        if (submitter != null) {
            response.setSubmitterName(submitter.getRealName());
        }
        
        return response;
    }
}
