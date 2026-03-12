package com.docreview.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.docreview.dto.response.Result;
import com.docreview.exception.BusinessException;
import com.docreview.exception.ErrorCode;
import com.docreview.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 * 
 * @author fullstack-dev
 * @since 2026-03-09
 */
@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/v1/upload")
public class UploadController {
    
    @Autowired
    private FileService fileService;
    
    @Value("${file.max-image-size:5242880}")
    private Long maxImageSize;
    
    @Value("${file.allowed-image-types:jpg,jpeg,png,gif,webp}")
    private String allowedImageTypes;
    
    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false, defaultValue = "comment") String type) {
        
        // 校验文件
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "文件不能为空");
        }
        
        // 校验文件大小
        if (file.getSize() > maxImageSize) {
            throw new BusinessException(ErrorCode.FILE_TOO_LARGE, 
                    "图片大小超过限制，最大允许 " + (maxImageSize / 1024 / 1024) + "MB");
        }
        
        // 校验文件类型
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        if (!java.util.Arrays.asList(allowedImageTypes.toLowerCase().split(","))
                .contains(fileExtension.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED, 
                    "图片类型不允许，允许的类型: " + allowedImageTypes);
        }
        
        try {
            // 获取当前用户ID
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 生成文件路径
            String objectName = String.format("images/%s/%s/%s.%s", 
                    type, userId, UUID.randomUUID().toString().replace("-", ""), fileExtension);
            
            // 上传文件
            String filePath = fileService.uploadFile(file, objectName);
            
            // 获取访问URL
            String url = fileService.getPreviewUrl(filePath, 86400 * 7); // 7天有效期
            
            Map<String, String> result = new HashMap<>();
            result.put("url", url);
            result.put("path", filePath);
            result.put("name", originalFilename);
            
            log.info("上传图片成功: userId={}, path={}", userId, filePath);
            return Result.success("上传成功", result);
        } catch (Exception e) {
            log.error("上传图片失败", e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "上传图片失败: " + e.getMessage());
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
}
