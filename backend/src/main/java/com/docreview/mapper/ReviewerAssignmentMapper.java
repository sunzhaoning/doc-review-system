package com.docreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docreview.entity.ReviewerAssignment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评审者分配 Mapper
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Mapper
public interface ReviewerAssignmentMapper extends BaseMapper<ReviewerAssignment> {
}
