package com.query.hive.service;

import com.query.hive.dto.MovieDetailDto;
import com.query.hive.dto.MovieSearchDto;

import java.util.List;
import java.util.Map;

/**
 * Hive电影服务接口
 */
public interface MovieService {
    /**
     * 获取电影版本列表
     */
    List<Map<String, Object>> getMovieEditions(String movieTitle);

    /**
     * 获取电影版本数量
     */
    List<Map<String, Object>> getMovieEditionCount(String movieTitle);

    /**
     * 获取指定类型的电影数量
     */
    List<Map<String, Object>> getMovieCountByGenre(String movieGenre);

    /**
     * 根据电影类型获取电影名称列表
     */
    List<Map<String, Object>> getMoviesByGenreName(String movieGenre);

    /**
     * 组合查询电影信息
     */
    List<MovieDetailDto> getMoviesByCombinedConditions(MovieSearchDto dto);

    /**
     * 利用宽表进行组合查询
     */
    List<MovieDetailDto> searchMoviesByWideTable(MovieSearchDto dto);
}
