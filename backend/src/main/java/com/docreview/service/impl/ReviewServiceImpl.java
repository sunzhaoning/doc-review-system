package com.docreview.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.docreview.dto.request.ReviewSubmitRequest;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.ReviewProgressResponse;
import com.docreview.dto.response.ReviewResponse;
import com.docreview.entity.Document;
import com.docreview.entity.Review;
import com.docreview.entity.ReviewerAssignment;
import com.docreview.entity.User;
import com.docreview.enums.DocumentStatus;
import com.docreview.enums.ReviewDecision;
import com.docreview.enums.ReviewStatus;
import com.docreview.exception.BusinessException;
import com.docreview.exception.ErrorCode;
import com.docreview.mapper.DocumentMapper;
import com.docreview.mapper.ReviewMapper;
import com.docreview.mapper.ReviewerAssignmentMapper;
import com.docreview.mapper.UserMapper;
import com.docreview.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评审服务实现
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Slf4j
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {
    
    @Autowired
    private DocumentMapper documentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ReviewerAssignmentMapper reviewerAssignmentMapper;
    
    @Override
    public PageResult<ReviewResponse> getPendingReviews(Integer current, Integer size, String status) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Page<ReviewerAssignment> page = new Page<>(current, size);
        LambdaQueryWrapper<ReviewerAssignment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReviewerAssignment::getReviewerId, userId);
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ReviewerAssignment::getStatus, status);
        }
        
        wrapper.orderByDesc(ReviewerAssignment::getCreatedAt);
        
        IPage<ReviewerAssignment> assignmentPage = reviewerAssignmentMapper.selectPage(page, wrapper);
        
        // 获取文档信息
        List<ReviewResponse> records = assignmentPage.getRecords().stream()
                .map(assignment -> {
                    Document document = documentMapper.selectById(assignment.getDocumentId());
                    if (document == null) {
                        return null;
                    }
                    
                    ReviewResponse response = new ReviewResponse();
                    response.setDocumentId(document.getId());
                    response.setDocumentTitle(document.getTitle());
                    response.setStatus(assignment.getStatus());
                    response.setReviewerId(userId);
                    
                    User reviewer = userMapper.selectById(userId);
                    if (reviewer != null) {
                        response.setReviewerName(reviewer.getRealName());
                    }
                    
                    // 如果已提交评审，获取评审信息
                    if ("COMPLETED".equals(assignment.getStatus())) {
                        Review review = getOne(new LambdaQueryWrapper<Review>()
                                .eq(Review::getDocumentId, document.getId())
                                .eq(Review::getReviewerId, userId)
                                .eq(Review::getDeleted, 0));
                        if (review != null) {
                            response.setId(review.getId());
                            response.setDecision(review.getDecision());
                            response.setSubmittedAt(review.getSubmittedAt());
                        }
                    }
                    
                    return response;
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
        
        PageResult<ReviewResponse> result = new PageResult<>();
        result.setRecords(records);
        result.setTotal(assignmentPage.getTotal());
        result.setSize(assignmentPage.getSize());
        result.setCurrent(assignmentPage.getCurrent());
        result.setPages(assignmentPage.getPages());
        
        return result;
    }
    
    @Override
    public List<ReviewResponse> getReviewsByDocumentId(Long documentId) {
        List<Review> reviews = list(new LambdaQueryWrapper<Review>()
                .eq(Review::getDocumentId, documentId)
                .eq(Review::getDeleted, 0)
                .orderByDesc(Review::getSubmittedAt));
        
        return reviews.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public ReviewResponse getReviewDetail(Long id) {
        Review review = getById(id);
        if (review == null || review.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_FOUND);
        }
        return convertToResponse(review);
    }
    
    @Override
    @Transactional
    public void startReview(Long documentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查文档
        Document document = documentMapper.selectById(documentId);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        // 检查是否是评审者
        ReviewerAssignment assignment = reviewerAssignmentMapper.selectOne(
                new LambdaQueryWrapper<ReviewerAssignment>()
                        .eq(ReviewerAssignment::getDocumentId, documentId)
                        .eq(ReviewerAssignment::getReviewerId, userId));
        
        if (assignment == null) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_ASSIGNED, "您不是此文档的评审者");
        }
        
        // 更新分配状态为评审中
        if ("PENDING".equals(assignment.getStatus())) {
            assignment.setStatus("REVIEWING");
            reviewerAssignmentMapper.updateById(assignment);
        }
        
        // 更新文档状态为评审中
        if (DocumentStatus.PENDING.getCode().equals(document.getStatus())) {
            document.setStatus(DocumentStatus.REVIEWING.getCode());
            documentMapper.updateById(document);
        }
        
        // 创建评审记录
        Review review = getOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getDocumentId, documentId)
                .eq(Review::getReviewerId, userId)
                .eq(Review::getDeleted, 0));
        
        if (review == null) {
            review = new Review();
            review.setDocumentId(documentId);
            review.setReviewerId(userId);
            review.setStatus(ReviewStatus.IN_PROGRESS.getCode());
            save(review);
        } else if (ReviewStatus.PENDING.getCode().equals(review.getStatus())) {
            review.setStatus(ReviewStatus.IN_PROGRESS.getCode());
            updateById(review);
        }
        
        log.info("开始评审: documentId={}, reviewerId={}", documentId, userId);
    }
    
    @Override
    @Transactional
    public void submitReview(Long documentId, ReviewSubmitRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查文档
        Document document = documentMapper.selectById(documentId);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        // 检查是否是评审者
        ReviewerAssignment assignment = reviewerAssignmentMapper.selectOne(
                new LambdaQueryWrapper<ReviewerAssignment>()
                        .eq(ReviewerAssignment::getDocumentId, documentId)
                        .eq(ReviewerAssignment::getReviewerId, userId));
        
        if (assignment == null) {
            throw new BusinessException(ErrorCode.REVIEW_NOT_ASSIGNED, "您不是此文档的评审者");
        }
        
        // 检查是否已提交
        if ("COMPLETED".equals(assignment.getStatus())) {
            throw new BusinessException(ErrorCode.REVIEW_ALREADY_SUBMITTED, "您已提交过评审意见");
        }
        
        // 更新或创建评审记录
        Review review = getOne(new LambdaQueryWrapper<Review>()
                .eq(Review::getDocumentId, documentId)
                .eq(Review::getReviewerId, userId)
                .eq(Review::getDeleted, 0));
        
        if (review == null) {
            review = new Review();
            review.setDocumentId(documentId);
            review.setReviewerId(userId);
        }
        
        review.setDecision(request.getDecision());
        review.setOverallComment(request.getOverallComment());
        review.setPros(request.getPros());
        review.setCons(request.getCons());
        review.setSuggestions(request.getSuggestions());
        review.setStatus(ReviewStatus.SUBMITTED.getCode());
        review.setSubmittedAt(LocalDateTime.now());
        
        if (review.getId() == null) {
            save(review);
        } else {
            updateById(review);
        }
        
        // 更新分配状态
        assignment.setStatus("COMPLETED");
        reviewerAssignmentMapper.updateById(assignment);
        
        // 检查是否所有评审者都已提交
        updateDocumentStatus(documentId);
        
        log.info("提交评审: documentId={}, reviewerId={}, decision={}", documentId, userId, request.getDecision());
    }
    
    @Override
    public ReviewProgressResponse getReviewProgress(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        // 获取所有评审者分配
        List<ReviewerAssignment> assignments = reviewerAssignmentMapper.selectList(
                new LambdaQueryWrapper<ReviewerAssignment>()
                        .eq(ReviewerAssignment::getDocumentId, documentId));
        
        ReviewProgressResponse response = new ReviewProgressResponse();
        response.setDocumentId(documentId);
        response.setDocumentTitle(document.getTitle());
        response.setDocumentStatus(document.getStatus());
        response.setTotalReviewers(assignments.size());
        
        int completed = 0;
        int approved = 0;
        int rejected = 0;
        
        List<ReviewProgressResponse.ReviewerDetail> reviewerDetails = assignments.stream()
                .map(assignment -> {
                    ReviewProgressResponse.ReviewerDetail detail = new ReviewProgressResponse.ReviewerDetail();
                    detail.setReviewerId(assignment.getReviewerId());
                    detail.setStatus(assignment.getStatus());
                    
                    User reviewer = userMapper.selectById(assignment.getReviewerId());
                    if (reviewer != null) {
                        detail.setReviewerName(reviewer.getRealName());
                    }
                    
                    if ("COMPLETED".equals(assignment.getStatus())) {
                        Review review = getOne(new LambdaQueryWrapper<Review>()
                                .eq(Review::getDocumentId, documentId)
                                .eq(Review::getReviewerId, assignment.getReviewerId())
                                .eq(Review::getDeleted, 0));
                        if (review != null) {
                            detail.setDecision(review.getDecision());
                            detail.setSubmittedAt(review.getSubmittedAt());
                        }
                    }
                    
                    return detail;
                })
                .collect(Collectors.toList());
        
        response.setReviewers(reviewerDetails);
        
        // 统计
        for (ReviewerAssignment assignment : assignments) {
            if ("COMPLETED".equals(assignment.getStatus())) {
                completed++;
                Review review = getOne(new LambdaQueryWrapper<Review>()
                        .eq(Review::getDocumentId, documentId)
                        .eq(Review::getReviewerId, assignment.getReviewerId())
                        .eq(Review::getDeleted, 0));
                if (review != null) {
                    if (ReviewDecision.APPROVED.getCode().equals(review.getDecision())) {
                        approved++;
                    } else if (ReviewDecision.REJECTED.getCode().equals(review.getDecision())) {
                        rejected++;
                    }
                }
            }
        }
        
        response.setCompletedReviewers(completed);
        response.setApprovedCount(approved);
        response.setRejectedCount(rejected);
        
        // 计算进度百分比
        if (assignments.size() > 0) {
            response.setProgress((completed * 100) / assignments.size());
        } else {
            response.setProgress(0);
        }
        
        return response;
    }
    
    @Override
    public PageResult<ReviewResponse> getMyReviewHistory(Integer current, Integer size, String decision) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Page<Review> page = new Page<>(current, size);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getReviewerId, userId);
        wrapper.eq(Review::getStatus, ReviewStatus.SUBMITTED.getCode());
        wrapper.eq(Review::getDeleted, 0);
        
        if (decision != null && !decision.isEmpty()) {
            wrapper.eq(Review::getDecision, decision);
        }
        
        wrapper.orderByDesc(Review::getSubmittedAt);
        
        IPage<Review> reviewPage = page(page, wrapper);
        
        PageResult<ReviewResponse> result = new PageResult<>();
        result.setRecords(reviewPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList()));
        result.setTotal(reviewPage.getTotal());
        result.setSize(reviewPage.getSize());
        result.setCurrent(reviewPage.getCurrent());
        result.setPages(reviewPage.getPages());
        
        return result;
    }
    
    /**
     * 更新文档状态（根据评审结果）
     */
    private void updateDocumentStatus(Long documentId) {
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            return;
        }
        
        // 获取所有评审者分配
        List<ReviewerAssignment> assignments = reviewerAssignmentMapper.selectList(
                new LambdaQueryWrapper<ReviewerAssignment>()
                        .eq(ReviewerAssignment::getDocumentId, documentId));
        
        int total = assignments.size();
        int completed = 0;
        int approved = 0;
        
        for (ReviewerAssignment assignment : assignments) {
            if ("COMPLETED".equals(assignment.getStatus())) {
                completed++;
                Review review = getOne(new LambdaQueryWrapper<Review>()
                        .eq(Review::getDocumentId, documentId)
                        .eq(Review::getReviewerId, assignment.getReviewerId())
                        .eq(Review::getDeleted, 0));
                if (review != null && ReviewDecision.APPROVED.getCode().equals(review.getDecision())) {
                    approved++;
                }
            }
        }
        
        // 所有评审者都已提交
        if (completed == total && total > 0) {
            // TODO: 根据配置决定通过条件（全部通过、多数通过等）
            // 目前使用全部通过
            if (approved == total) {
                document.setStatus(DocumentStatus.APPROVED.getCode());
            } else {
                document.setStatus(DocumentStatus.REJECTED.getCode());
            }
            documentMapper.updateById(document);
            log.info("更新文档状态: documentId={}, status={}", documentId, document.getStatus());
        }
    }
    
    /**
     * 转换为响应对象
     */
    private ReviewResponse convertToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        BeanUtils.copyProperties(review, response);
        
        Document document = documentMapper.selectById(review.getDocumentId());
        if (document != null) {
            response.setDocumentTitle(document.getTitle());
        }
        
        User reviewer = userMapper.selectById(review.getReviewerId());
        if (reviewer != null) {
            response.setReviewerName(reviewer.getRealName());
        }
        
        return response;
    }
}
