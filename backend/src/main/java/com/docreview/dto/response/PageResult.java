package com.docreview.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Data
public class PageResult<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 每页大小
     */
    private Long size;
    
    /**
     * 当前页
     */
    private Long current;
    
    /**
     * 总页数
     */
    private Long pages;
}
