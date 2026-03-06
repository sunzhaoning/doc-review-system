package com.docreview.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.docreview.dto.response.DocumentResponse;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.ReviewProgressResponse;
import com.docreview.entity.Document;
import com.docreview.entity.Review;
import com.docreview.entity.User;
import com.docreview.enums.DocumentStatus;
import com.docreview.exception.BusinessException;
import com.docreview.exception.ErrorCode;
import com.docreview.mapper.DocumentMapper;
import com.docreview.mapper.UserMapper;
import com.docreview.service.ArchiveService;
import com.docreview.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 归档服务实现
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Slf4j
@Service
public class ArchiveServiceImpl extends ServiceImpl<DocumentMapper, Document> implements ArchiveService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ReviewService reviewService;
    
    @Override
    @Transactional
    public void archiveDocument(Long id) {
        Document document = getById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        // 只有已通过的文档才能归档
        if (!DocumentStatus.APPROVED.getCode().equals(document.getStatus())) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "只有已通过的文档才能归档");
        }
        
        document.setArchived(true);
        document.setArchivedAt(LocalDateTime.now());
        updateById(document);
        
        log.info("归档文档: id={}", id);
    }
    
    @Override
    @Transactional
    public void unarchiveDocument(Long id) {
        Document document = getById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        document.setArchived(false);
        document.setArchivedAt(null);
        updateById(document);
        
        log.info("取消归档: id={}", id);
    }
    
    @Override
    public PageResult<DocumentResponse> searchArchived(Integer current, Integer size, 
                                                        String keyword, String reviewType,
                                                        String startDate, String endDate) {
        Page<Document> page = new Page<>(current, size);
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Document::getDeleted, 0);
        wrapper.eq(Document::getArchived, true);
        
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Document::getTitle, keyword)
                    .or().like(Document::getDescription, keyword));
        }
        
        if (StringUtils.hasText(reviewType)) {
            wrapper.eq(Document::getReviewType, reviewType);
        }
        
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Document::getCreatedAt, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        
        if (StringUtils.hasText(endDate)) {
            wrapper.le(Document::getCreatedAt, LocalDateTime.parse(endDate + "T23:59:59"));
        }
        
        wrapper.orderByDesc(Document::getArchivedAt);
        
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
    @Transactional
    public void batchArchive(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文档ID列表不能为空");
        }
        
        update(new LambdaUpdateWrapper<Document>()
                .in(Document::getId, ids)
                .eq(Document::getStatus, DocumentStatus.APPROVED.getCode())
                .eq(Document::getArchived, false)
                .eq(Document::getDeleted, 0)
                .set(Document::getArchived, true)
                .set(Document::getArchivedAt, LocalDateTime.now()));
        
        log.info("批量归档: ids={}", ids);
    }
    
    @Override
    public byte[] exportReviewReport(Long id) {
        Document document = getById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        // 获取评审进度
        ReviewProgressResponse progress = reviewService.getReviewProgress(id);
        
        // 构建报告内容
        StringBuilder report = new StringBuilder();
        report.append("# 文档评审报告\n\n");
        report.append("## 基本信息\n\n");
        report.append("| 项目 | 内容 |\n");
        report.append("|------|------|\n");
        report.append("| 文档标题 | ").append(document.getTitle()).append(" |\n");
        report.append("| 文档描述 | ").append(document.getDescription() != null ? document.getDescription() : "-").append(" |\n");
        report.append("| 文档类型 | ").append(document.getFileType()).append(" |\n");
        report.append("| 评审类型 | ").append(document.getReviewType()).append(" |\n");
        report.append("| 提交时间 | ").append(document.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append(" |\n");
        report.append("| 文档状态 | ").append(document.getStatus()).append(" |\n\n");
        
        report.append("## 评审统计\n\n");
        report.append("| 指标 | 数量 |\n");
        report.append("|------|------|\n");
        report.append("| 总评审者 | ").append(progress.getTotalReviewers()).append(" |\n");
        report.append("| 已完成 | ").append(progress.getCompletedReviewers()).append(" |\n");
        report.append("| 通过 | ").append(progress.getApprovedCount()).append(" |\n");
        report.append("| 拒绝 | ").append(progress.getRejectedCount()).append(" |\n");
        report.append("| 进度 | ").append(progress.getProgress()).append("% |\n\n");
        
        report.append("## 评审者详情\n\n");
        report.append("| 评审者 | 状态 | 决定 | 提交时间 |\n");
        report.append("|--------|------|------|----------|\n");
        for (ReviewProgressResponse.ReviewerDetail detail : progress.getReviewers()) {
            report.append("| ").append(detail.getReviewerName()).append(" ");
            report.append("| ").append(detail.getStatus()).append(" ");
            report.append("| ").append(detail.getDecision() != null ? detail.getDecision() : "-").append(" ");
            report.append("| ").append(detail.getSubmittedAt() != null ? 
                    detail.getSubmittedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "-").append(" |\n");
        }
        
        report.append("\n---\n");
        report.append("报告生成时间: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        
        return report.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public DocumentResponse getDetail(Long id) {
        Document document = getById(id);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        return convertToResponse(document);
    }
    
    /**
     * 转换为响应对象
     */
    private DocumentResponse convertToResponse(Document document) {
        DocumentResponse response = new DocumentResponse();
        BeanUtils.copyProperties(document, response);
        
        User submitter = userMapper.selectById(document.getSubmitterId());
        if (submitter != null) {
            response.setSubmitterName(submitter.getRealName());
        }
        
        return response;
    }
}
