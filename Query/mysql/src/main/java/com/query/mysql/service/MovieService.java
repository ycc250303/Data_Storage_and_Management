package com.query.mysql.service;

import com.query.mysql.dto.MovieDetailDto;
import com.query.mysql.dto.MovieSearchDto;
import com.query.mysql.entity.Movies;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 电影主表 服务类
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
public interface MovieService extends IService<Movies> {
    /**
     * 获取电影版本列表
     *
     * @param movieTitle
     * @return
     */
    public List<Map<String, Object>> getMovieEditions(String movieTitle);

    /**
     * 获取电影版本数量
     *
     * @param movieTitle
     * @return
     */
    public List<Map<String, Object>> getMovieEditionCount(String movieTitle);

    public List<Map<String, Object>> getMovieCountByGenre(String movieGenre);

    /**
     * 根据电影类别名称获取电影名称列表
     *
     * @param movieGenre 电影类别名称
     * @return 电影名称列表
     */
    List<Map<String, Object>> getMoviesByGenreName(String movieGenre);

    /**
     * 组合查询电影信息
     *
     * @param dto 查询条件
     * @return 电影详情列表
     */
    List<MovieDetailDto> getMoviesByCombinedConditions(MovieSearchDto dto);

    /**
     * 利用宽表进行组合查询（极速版）
     *
     * @param dto 查询条件
     * @return 电影详情列表
     */
    List<MovieDetailDto> searchMoviesByWideTable(MovieSearchDto dto);

    /**
     * 根据ASIN/ID获取电影详情
     */
    MovieDetailDto getMovieDetailById(String id);
}
