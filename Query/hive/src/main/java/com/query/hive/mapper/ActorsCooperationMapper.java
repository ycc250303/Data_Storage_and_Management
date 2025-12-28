package com.query.hive.mapper;

import com.query.hive.entity.ActorsCooperation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 演员合作表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface ActorsCooperationMapper {

    /**
     * 获取演员合作统计
     * 
     * @param limit 限制数量
     * @return 合作列表
     */
    List<ActorsCooperation> getCollaborations(@Param("limit") int limit);

    /**
     * 获取带类别的演员合作统计
     */
    List<Map<String, Object>> getCollaborationsByGenre(@Param("genre") String genre, @Param("limit") int limit);

    /**
     * 获取演员组合按评论数统计
     */
    List<Map<String, Object>> getCollaborationsByReviews(@Param("genre") String genre, @Param("limit") int limit);

}
