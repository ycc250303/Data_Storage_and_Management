package com.query.mysql.mapper;

import com.query.mysql.entity.MovieWeekdayStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 电影星期统计表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface MovieWeekdayStatsMapper extends BaseMapper<MovieWeekdayStats> {

    @Select("SELECT total_movies, " +
            "CASE release_weekday " +
            "    WHEN 0 THEN '周一' " +
            "    WHEN 1 THEN '周二' " +
            "    WHEN 2 THEN '周三' " +
            "    WHEN 3 THEN '周四' " +
            "    WHEN 4 THEN '周五' " +
            "    WHEN 5 THEN '周六' " +
            "    WHEN 6 THEN '周日' " +
            "END as weekday_name " +
            "FROM movie_weekday_stats WHERE release_weekday = #{weekday}")
    List<Map<String, Object>> getMovieCountByWeekday(@Param("weekday") int weekday);

}

