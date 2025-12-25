package com.query.mysql.service;

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
     * @param genreName 电影类别名称
     * @return 电影名称列表
     */
    List<Map<String, Object>> getMoviesByGenreName(String movieGenre);
}
