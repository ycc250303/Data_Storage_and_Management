package com.query.mysql.mapper;

import com.query.mysql.entity.MovieEditions;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 电影版本表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface MovieEditionsMapper extends BaseMapper<MovieEditions> {
    List<Map<String, Object>> getMovieEditionsByMovieTitle(@Param("movieTitle") String movieTitle);

    List<Map<String, Object>> getMovieEditionCountByMovieTitle(@Param("movieTitle") String movieTitle);

}
