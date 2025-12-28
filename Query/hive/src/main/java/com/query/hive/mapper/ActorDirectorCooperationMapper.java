package com.query.hive.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 演员-导演合作表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface ActorDirectorCooperationMapper {

    /**
     * 获取导演-演员合作统计（从预计算汇总表读取）
     */
    List<Map<String, Object>> getBaseDirectorActorCollaborations(@Param("limit") int limit);

    /**
     * 获取导演-演员合作统计（带类别筛选，从明细表实时计算）
     */
    List<Map<String, Object>> getDirectorActorCollaborations(@Param("genre") String genre, @Param("limit") int limit);

    /**
     * 获取导演-演员组合按评论数统计
     */
    List<Map<String, Object>> getDirectorActorCollaborationsByReviews(@Param("genre") String genre, @Param("limit") int limit);

}
