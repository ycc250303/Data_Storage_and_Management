package com.query.mysql.mapper;

import com.query.mysql.dto.MovieSearchDto;
import com.query.mysql.entity.Movies;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 电影主表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface MoviesMapper extends BaseMapper<Movies> {

    /**
     * 组合查询电影信息
     */
    List<Map<String, Object>> getMoviesByCombinedConditions(@Param("dto") MovieSearchDto dto);

    /**
     * 统计符合条件的电影总数（用于分页）
     */
    int countMoviesByCombinedConditions(@Param("dto") MovieSearchDto dto);
}