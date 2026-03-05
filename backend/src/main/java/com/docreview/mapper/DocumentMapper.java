package com.docreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docreview.entity.Document;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档 Mapper
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
}
