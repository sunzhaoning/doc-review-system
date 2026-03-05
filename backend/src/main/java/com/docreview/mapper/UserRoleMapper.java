package com.docreview.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.docreview.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色关联 Mapper
 * 
 * @author fullstack-dev
 * @since 2026-03-05
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
    
    /**
     * 删除用户的所有角色
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 批量插入用户角色
     */
    int batchInsert(@Param("list") java.util.List<UserRole> list);
}
