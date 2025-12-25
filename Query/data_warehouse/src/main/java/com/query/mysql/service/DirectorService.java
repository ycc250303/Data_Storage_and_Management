package com.query.mysql.service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 导演服务类
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
public interface DirectorService {
    /**
     * 模糊匹配查询导演导演电影数量
     *
     * @param directorName 导演名称
     * @return 导演名称和导演电影数量列表
     */
    List<Map<String, Object>> getDirectorMovieCount(String directorName);

    /**
     * 精确匹配查询导演导演的电影列表
     *
     * @param directorName 导演名称
     * @return 电影名称列表
     */
    List<Map<String, Object>> getDirectorMovies(String directorName);
}
