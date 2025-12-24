package com.query.mysql.mapper;

import com.query.mysql.entity.DirectorStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 导演维度电影统计表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface DirectorStatsMapper extends BaseMapper<DirectorStats> {

    /**
     * 模糊匹配查询导演导演电影数量
     */
    List<Map<String, Object>> getDirectorMovieCountByFuzzyName(@Param("directorName") String directorName);

}

