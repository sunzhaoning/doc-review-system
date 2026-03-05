package com.docreview.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.docreview.dto.request.CommentCreateRequest;
import com.docreview.dto.response.CommentResponse;
import com.docreview.entity.Comment;
import com.docreview.entity.Document;
import com.docreview.entity.User;
import com.docreview.exception.BusinessException;
import com.docreview.exception.ErrorCode;
import com.docreview.mapper.CommentMapper;
import com.docreview.mapper.DocumentMapper;
import com.docreview.mapper.UserMapper;
import com.docreview.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Slf4j
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {
    
    @Autowired
    private DocumentMapper documentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public List<CommentResponse> getCommentsByDocumentId(Long documentId) {
        // 检查文档是否存在
        Document document = documentMapper.selectById(documentId);
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        // 获取顶级评论（parentId为空）
        List<Comment> comments = list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getDocumentId, documentId)
                .isNull(Comment::getParentId)
                .eq(Comment::getDeleted, 0)
                .orderByDesc(Comment::getCreatedAt));
        
        return comments.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public Long createComment(CommentCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查文档是否存在
        Document document = documentMapper.selectById(request.getDocumentId());
        if (document == null || document.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND);
        }
        
        Comment comment = new Comment();
        BeanUtils.copyProperties(request, comment);
        comment.setAuthorId(userId);
        comment.setType(request.getType() != null ? request.getType() : "ISSUE");
        comment.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        
        save(comment);
        
        log.info("创建评论: id={}, documentId={}, authorId={}", comment.getId(), request.getDocumentId(), userId);
        return comment.getId();
    }
    
    @Override
    @Transactional
    public void updateComment(Long id, CommentCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Comment comment = getById(id);
        if (comment == null || comment.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "评论不存在");
        }
        
        // 检查是否是评论作者
        if (!comment.getAuthorId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权编辑此评论");
        }
        
        BeanUtils.copyProperties(request, comment);
        comment.setId(id);
        
        updateById(comment);
        
        log.info("更新评论: id={}", id);
    }
    
    @Override
    @Transactional
    public void deleteComment(Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        Comment comment = getById(id);
        if (comment == null || comment.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "评论不存在");
        }
        
        // 检查是否是评论作者
        if (!comment.getAuthorId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权删除此评论");
        }
        
        comment.setDeleted(1);
        updateById(comment);
        
        log.info("删除评论: id={}", id);
    }
    
    @Override
    @Transactional
    public Long replyComment(Long parentId, CommentCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查父评论是否存在
        Comment parentComment = getById(parentId);
        if (parentComment == null || parentComment.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "父评论不存在");
        }
        
        Comment comment = new Comment();
        BeanUtils.copyProperties(request, comment);
        comment.setAuthorId(userId);
        comment.setParentId(parentId);
        comment.setType(request.getType() != null ? request.getType() : "ISSUE");
        comment.setPriority(request.getPriority() != null ? request.getPriority() : "MEDIUM");
        
        save(comment);
        
        log.info("回复评论: id={}, parentId={}", comment.getId(), parentId);
        return comment.getId();
    }
    
    @Override
    public List<CommentResponse> getReplies(Long commentId) {
        List<Comment> replies = list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getParentId, commentId)
                .eq(Comment::getDeleted, 0)
                .orderByAsc(Comment::getCreatedAt));
        
        return replies.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为响应对象
     */
    private CommentResponse convertToResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        BeanUtils.copyProperties(comment, response);
        
        User author = userMapper.selectById(comment.getAuthorId());
        if (author != null) {
            response.setAuthorName(author.getRealName());
            response.setAuthorAvatar(author.getAvatar());
        }
        
        // 获取回复数量
        long replyCount = count(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getParentId, comment.getId())
                .eq(Comment::getDeleted, 0));
        response.setReplyCount((int) replyCount);
        
        return response;
    }
}
