package com.docreview.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.docreview.dto.request.CommentCreateRequest;
import com.docreview.dto.response.CommentResponse;
import com.docreview.dto.response.PageResult;
import com.docreview.entity.Comment;

import java.util.List;

/**
 * 评论服务接口
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
public interface CommentService extends IService<Comment> {
    
    /**
     * 获取文档评论列表
     */
    List<CommentResponse> getCommentsByDocumentId(Long documentId);
    
    /**
     * 创建评论
     */
    Long createComment(CommentCreateRequest request);
    
    /**
     * 更新评论
     */
    void updateComment(Long id, CommentCreateRequest request);
    
    /**
     * 删除评论
     */
    void deleteComment(Long id);
    
    /**
     * 回复评论
     */
    Long replyComment(Long parentId, CommentCreateRequest request);
    
    /**
     * 获取评论的回复列表
     */
    List<CommentResponse> getReplies(Long commentId);
}
