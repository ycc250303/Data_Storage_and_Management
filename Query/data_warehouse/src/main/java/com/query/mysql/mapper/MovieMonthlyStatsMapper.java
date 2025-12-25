package com.query.mysql.mapper;

import com.query.mysql.entity.MovieMonthlyStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 电影月度统计表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface MovieMonthlyStatsMapper extends BaseMapper<MovieMonthlyStats> {
    List<Map<String, Object>> getMovieCountByYearAndMonth(@Param("year") int year, @Param("month") int month);

    List<Map<String, Object>> getMovieCountByYearAndQuarter(@Param("year") int year, @Param("quarter") int quarter);
}
