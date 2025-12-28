package com.query.mysql.service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 演员服务类
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
public interface ActorService {
    /**
     * 模糊匹配查询演员参演电影数量
     *
     * @param actorName 演员名称
     * @return 演员名称和参演电影数量列表
     */
    List<Map<String, Object>> getActorMovieCount(String actorName);

    /**
     * 精确匹配查询演员参演的电影列表
     *
     * @param actorName 演员名称
     * @return 电影名称列表
     */
    List<Map<String, Object>> getActorMovies(String actorName);
}
