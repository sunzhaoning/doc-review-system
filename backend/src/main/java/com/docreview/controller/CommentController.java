package com.docreview.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.docreview.dto.request.CommentCreateRequest;
import com.docreview.dto.response.CommentResponse;
import com.docreview.dto.response.Result;
import com.docreview.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论控制器
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Tag(name = "评论管理")
@RestController
@RequestMapping("/api/v1")
public class CommentController {
    
    @Autowired
    private CommentService commentService;
    
    @Operation(summary = "获取文档评论列表")
    @SaCheckPermission("comment:list")
    @GetMapping("/documents/{documentId}/comments")
    public Result<List<CommentResponse>> getComments(@PathVariable Long documentId) {
        return Result.success(commentService.getCommentsByDocumentId(documentId));
    }
    
    @Operation(summary = "创建评论")
    @SaCheckPermission("comment:create")
    @PostMapping("/documents/{documentId}/comments")
    public Result<Long> createComment(@PathVariable Long documentId,
                                       @Valid @RequestBody CommentCreateRequest request) {
        request.setDocumentId(documentId);
        Long id = commentService.createComment(request);
        return Result.success("创建成功", id);
    }
    
    @Operation(summary = "更新评论")
    @SaCheckPermission("comment:edit")
    @PutMapping("/comments/{id}")
    public Result<Void> updateComment(@PathVariable Long id,
                                       @RequestBody CommentCreateRequest request) {
        commentService.updateComment(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除评论")
    @SaCheckPermission("comment:delete")
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return Result.success();
    }
    
    @Operation(summary = "回复评论")
    @SaCheckPermission("comment:create")
    @PostMapping("/comments/{parentId}/reply")
    public Result<Long> replyComment(@PathVariable Long parentId,
                                      @Valid @RequestBody CommentCreateRequest request) {
        Long id = commentService.replyComment(parentId, request);
        return Result.success("回复成功", id);
    }
    
    @Operation(summary = "获取评论的回复列表")
    @GetMapping("/comments/{commentId}/replies")
    public Result<List<CommentResponse>> getReplies(@PathVariable Long commentId) {
        return Result.success(commentService.getReplies(commentId));
    }
}
