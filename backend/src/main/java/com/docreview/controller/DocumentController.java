package com.docreview.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.docreview.dto.request.DocumentUploadRequest;
import com.docreview.dto.response.DocumentResponse;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.Result;
import com.docreview.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 文档控制器
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Tag(name = "文档管理")
@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {
    
    @Autowired
    private DocumentService documentService;
    
    @Operation(summary = "上传文档")
    @SaCheckPermission("doc:create")
    @PostMapping
    public Result<Long> upload(
            @RequestParam("file") MultipartFile file,
            @Valid DocumentUploadRequest request) {
        Long id = documentService.uploadDocument(file, request);
        return Result.success("上传成功", id);
    }
    
    @Operation(summary = "获取文档列表")
    @SaCheckPermission("doc:list")
    @GetMapping
    public Result<PageResult<DocumentResponse>> getPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reviewType,
            @RequestParam(required = false) Long submitterId) {
        return Result.success(documentService.getPage(current, size, title, status, reviewType, submitterId));
    }
    
    @Operation(summary = "获取我的文档")
    @GetMapping("/my")
    public Result<PageResult<DocumentResponse>> getMyDocuments(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.success(documentService.getMyDocuments(current, size, status));
    }
    
    @Operation(summary = "获取文档统计数据")
    @GetMapping("/stats")
    public Result<java.util.Map<String, Object>> getStats() {
        return Result.success(documentService.getStats());
    }
    
    @Operation(summary = "获取文档详情")
    @SaCheckPermission("doc:query")
    @GetMapping("/{id}")
    public Result<DocumentResponse> getById(@PathVariable Long id) {
        return Result.success(documentService.getDetail(id));
    }
    
    @Operation(summary = "更新文档信息")
    @SaCheckPermission("doc:edit")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody DocumentUploadRequest request) {
        documentService.updateDocument(id, request);
        return Result.success();
    }
    
    @Operation(summary = "删除文档")
    @SaCheckPermission("doc:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return Result.success();
    }
    
    @Operation(summary = "提交评审")
    @SaCheckPermission("doc:submit")
    @PostMapping("/{id}/submit")
    public Result<Void> submitReview(@PathVariable Long id, @RequestBody Map<String, List<Long>> body) {
        List<Long> reviewerIds = body.get("reviewerIds");
        documentService.submitReview(id, reviewerIds);
        return Result.success();
    }
    
    @Operation(summary = "撤回评审")
    @SaCheckPermission("doc:withdraw")
    @PostMapping("/{id}/withdraw")
    public Result<Void> withdrawReview(@PathVariable Long id) {
        documentService.withdrawReview(id);
        return Result.success();
    }
    
    @Operation(summary = "获取文档预览URL")
    @SaCheckPermission("doc:query")
    @GetMapping("/{id}/preview")
    public Result<String> getPreviewUrl(@PathVariable Long id) {
        return Result.success(documentService.getPreviewUrl(id));
    }
    
    @Operation(summary = "下载文档")
    @SaCheckPermission("doc:download")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        DocumentResponse document = documentService.getDetail(id);
        byte[] content = documentService.downloadDocument(id);
        
        String fileName = URLEncoder.encode(document.getFileName(), StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }
}
