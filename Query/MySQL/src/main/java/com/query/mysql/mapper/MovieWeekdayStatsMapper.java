package com.query.mysql.mapper;

import com.query.mysql.entity.MovieWeekdayStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    List<Map<String, Object>> getMovieCountByWeekday(@Param("weekday") int weekday);

}

