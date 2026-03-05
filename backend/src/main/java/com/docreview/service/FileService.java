package com.docreview.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 文件服务
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Slf4j
@Service
public class FileService {
    
    @Autowired
    private MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    /**
     * 检查存储桶是否存在，不存在则创建
     */
    public void checkBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket(bucketName)
                .build());
        
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build());
            log.info("创建存储桶: {}", bucketName);
        }
    }
    
    /**
     * 上传文件
     * 
     * @param file 文件
     * @param objectName 对象名称
     * @return 文件路径
     */
    public String uploadFile(MultipartFile file, String objectName) throws Exception {
        checkBucket();
        
        try (InputStream inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            
            log.info("上传文件成功: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("上传文件失败: {}", objectName, e);
            throw new Exception("上传文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载文件
     * 
     * @param objectName 对象名称
     * @return 文件输入流
     */
    public InputStream downloadFile(String objectName) throws Exception {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("下载文件失败: {}", objectName, e);
            throw new Exception("下载文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除文件
     * 
     * @param objectName 对象名称
     */
    public void deleteFile(String objectName) throws Exception {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("删除文件成功: {}", objectName);
        } catch (Exception e) {
            log.error("删除文件失败: {}", objectName, e);
            throw new Exception("删除文件失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件预览URL
     * 
     * @param objectName 对象名称
     * @param expiry 过期时间（秒）
     * @return 预览URL
     */
    public String getPreviewUrl(String objectName, int expiry) throws Exception {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(expiry, TimeUnit.SECONDS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取预览URL失败: {}", objectName, e);
            throw new Exception("获取预览URL失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查文件是否存在
     * 
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean fileExists(String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取文件大小
     * 
     * @param objectName 对象名称
     * @return 文件大小
     */
    public long getFileSize(String objectName) throws Exception {
        try {
            StatObjectResponse response = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return response.size();
        } catch (Exception e) {
            log.error("获取文件大小失败: {}", objectName, e);
            throw new Exception("获取文件大小失败: " + e.getMessage());
        }
    }
}
