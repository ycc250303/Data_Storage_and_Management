package com.query.mysql.mapper;

import com.query.mysql.entity.MovieMonthlyStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("SELECT release_year, release_month, total_movies, avg_score " +
            "FROM movie_monthly_stats WHERE release_year = #{year} AND release_month = #{month}")
    List<Map<String, Object>> getMovieCountByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Select("SELECT #{year} as release_year, #{quarter} as release_quarter, " +
            "SUM(total_movies) as total_movies " +
            "FROM movie_monthly_stats WHERE release_year = #{year} AND release_month <= #{quarter} * 3")
    List<Map<String, Object>> getMovieCountByYearAndQuarter(@Param("year") int year, @Param("quarter") int quarter);
}
