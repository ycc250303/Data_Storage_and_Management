package com.query.mysql.mapper;

import com.query.mysql.entity.MovieYearlyStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 电影年度统计表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface MovieYearlyStatsMapper extends BaseMapper<MovieYearlyStats> {

    List<Map<String, Object>> getMovieCountByYear(@Param("year") int year);


}

