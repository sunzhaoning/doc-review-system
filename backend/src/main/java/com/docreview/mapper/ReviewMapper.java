package com.docreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docreview.entity.Review;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评审 Mapper
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}
