package com.docreview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.docreview.dto.request.ReviewSubmitRequest;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.ReviewProgressResponse;
import com.docreview.dto.response.ReviewResponse;
import com.docreview.entity.Review;

import java.util.List;

/**
 * 评审服务接口
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public interface ReviewService extends IService<Review> {
    
    /**
     * 获取待评审列表
     */
    PageResult<ReviewResponse> getPendingReviews(Integer current, Integer size, String status);
    
    /**
     * 获取文档的评审记录
     */
    List<ReviewResponse> getReviewsByDocumentId(Long documentId);
    
    /**
     * 获取评审详情
     */
    ReviewResponse getReviewDetail(Long id);
    
    /**
     * 开始评审（评审者打开评审页面）
     */
    void startReview(Long documentId);
    
    /**
     * 提交评审意见
     */
    void submitReview(Long documentId, ReviewSubmitRequest request);
    
    /**
     * 获取评审进度
     */
    ReviewProgressResponse getReviewProgress(Long documentId);
    
    /**
     * 获取我的评审历史
     */
    PageResult<ReviewResponse> getMyReviewHistory(Integer current, Integer size, String decision);
}
