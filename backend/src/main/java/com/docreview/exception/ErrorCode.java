package com.docreview.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {
    
    // 通用错误 1xxxx
    UNKNOWN_ERROR(10000, "系统错误"),
    PARAM_ERROR(10001, "参数错误"),
    DATA_NOT_FOUND(10002, "数据不存在"),
    DATA_EXISTS(10003, "数据已存在"),
    OPERATION_FAILED(10004, "操作失败"),
    
    // 认证错误 2xxxx
    UNAUTHORIZED(20001, "未登录"),
    TOKEN_EXPIRED(20002, "Token已过期"),
    TOKEN_INVALID(20003, "Token无效"),
    LOGIN_FAILED(20004, "登录失败"),
    PASSWORD_ERROR(20005, "密码错误"),
    ACCOUNT_DISABLED(20006, "账号已禁用"),
    
    // 权限错误 3xxxx
    FORBIDDEN(30001, "无权限"),
    PERMISSION_DENIED(30002, "权限不足"),
    
    // 用户错误 4xxxx
    USER_NOT_FOUND(40001, "用户不存在"),
    USER_EXISTS(40002, "用户已存在"),
    USERNAME_EXISTS(40003, "用户名已存在"),
    EMAIL_EXISTS(40004, "邮箱已存在"),
    
    // 文档错误 5xxxx
    DOCUMENT_NOT_FOUND(50001, "文档不存在"),
    DOCUMENT_UPLOADED(50002, "文档已上传"),
    DOCUMENT_DELETED(50003, "文档已删除"),
    DOCUMENT_REVIEWING(50004, "文档评审中"),
    FILE_UPLOAD_FAILED(50005, "文件上传失败"),
    FILE_TOO_LARGE(50006, "文件大小超过限制"),
    FILE_TYPE_NOT_ALLOWED(50007, "文件类型不允许"),
    
    // 评审错误 6xxxx
    REVIEW_NOT_FOUND(60001, "评审不存在"),
    REVIEW_ALREADY_SUBMITTED(60002, "评审已提交"),
    REVIEW_NOT_ASSIGNED(60003, "未分配评审"),
    
    // 系统错误 9xxxx
    SYSTEM_ERROR(90001, "系统错误"),
    DATABASE_ERROR(90002, "数据库错误"),
    NETWORK_ERROR(90003, "网络错误");
    
    private final Integer code;
    private final String message;
}
