package com.docreview.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.docreview.dto.request.ReviewSubmitRequest;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.Result;
import com.docreview.dto.response.ReviewProgressResponse;
import com.docreview.dto.response.ReviewResponse;
import com.docreview.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评审控制器
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Tag(name = "评审管理")
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Operation(summary = "获取待评审列表")
    @SaCheckPermission("review:list")
    @GetMapping("/pending")
    public Result<PageResult<ReviewResponse>> getPendingReviews(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.success(reviewService.getPendingReviews(current, size, status));
    }
    
    @Operation(summary = "获取我的评审历史")
    @GetMapping("/history")
    public Result<PageResult<ReviewResponse>> getMyReviewHistory(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String decision) {
        return Result.success(reviewService.getMyReviewHistory(current, size, decision));
    }
    
    @Operation(summary = "获取评审详情")
    @SaCheckPermission("review:query")
    @GetMapping("/{id}")
    public Result<ReviewResponse> getReviewDetail(@PathVariable Long id) {
        return Result.success(reviewService.getReviewDetail(id));
    }
    
    @Operation(summary = "获取文档的评审记录")
    @SaCheckPermission("review:query")
    @GetMapping("/document/{documentId}")
    public Result<List<ReviewResponse>> getReviewsByDocumentId(@PathVariable Long documentId) {
        return Result.success(reviewService.getReviewsByDocumentId(documentId));
    }
    
    @Operation(summary = "获取评审进度")
    @SaCheckPermission("review:query")
    @GetMapping("/progress/{documentId}")
    public Result<ReviewProgressResponse> getReviewProgress(@PathVariable Long documentId) {
        return Result.success(reviewService.getReviewProgress(documentId));
    }
    
    @Operation(summary = "开始评审")
    @SaCheckPermission("review:query")
    @PostMapping("/start/{documentId}")
    public Result<Void> startReview(@PathVariable Long documentId) {
        reviewService.startReview(documentId);
        return Result.success();
    }
    
    @Operation(summary = "提交评审意见")
    @SaCheckPermission("review:submit")
    @PostMapping("/submit/{documentId}")
    public Result<Void> submitReview(@PathVariable Long documentId, 
                                      @Valid @RequestBody ReviewSubmitRequest request) {
        reviewService.submitReview(documentId, request);
        return Result.success();
    }
}
