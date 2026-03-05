package com.docreview.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.docreview.dto.response.DocumentResponse;
import com.docreview.dto.response.PageResult;
import com.docreview.dto.response.Result;
import com.docreview.service.ArchiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 归档控制器
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Tag(name = "归档管理")
@RestController
@RequestMapping("/api/v1/archives")
public class ArchiveController {
    
    @Autowired
    private ArchiveService archiveService;
    
    @Operation(summary = "搜索归档文档")
    @SaCheckPermission("archive:list")
    @GetMapping
    public Result<PageResult<DocumentResponse>> search(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String reviewType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(archiveService.searchArchived(current, size, keyword, reviewType, startDate, endDate));
    }
    
    @Operation(summary = "归档文档")
    @SaCheckPermission("archive:create")
    @PostMapping("/documents/{id}")
    public Result<Void> archive(@PathVariable Long id) {
        archiveService.archiveDocument(id);
        return Result.success("归档成功");
    }
    
    @Operation(summary = "取消归档")
    @SaCheckPermission("archive:delete")
    @DeleteMapping("/{id}")
    public Result<Void> unarchive(@PathVariable Long id) {
        archiveService.unarchiveDocument(id);
        return Result.success("取消归档成功");
    }
    
    @Operation(summary = "批量归档")
    @SaCheckPermission("archive:create")
    @PostMapping("/batch")
    public Result<Void> batchArchive(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        archiveService.batchArchive(ids);
        return Result.success("批量归档成功");
    }
    
    @Operation(summary = "导出评审报告")
    @SaCheckPermission("archive:export")
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportReport(@PathVariable Long id) {
        DocumentResponse document = archiveService.getDetail(id);
        byte[] content = archiveService.exportReviewReport(id);
        
        String fileName = URLEncoder.encode(document.getTitle() + "_评审报告.md", StandardCharsets.UTF_8);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.TEXT_MARKDOWN)
                .body(content);
    }
}
