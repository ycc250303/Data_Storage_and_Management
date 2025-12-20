package com.query.mysql.mapper;

import com.query.mysql.entity.Actors;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 演员表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface ActorsMapper extends BaseMapper<Actors> {

    /**
     * 精确匹配查询演员参演的电影列表
     */
    @Select("SELECT DISTINCT m.movie_title " +
            "FROM data_warehouse.actors a " +
            "JOIN data_warehouse.movie_actors ma ON a.id = ma.actor_id " +
            "JOIN data_warehouse.movies m ON ma.movie_id = m.id " +
            "WHERE a.name = #{actorName} " +
            "ORDER BY m.movie_title")
    List<Map<String, Object>> getActorMoviesByExactName(@Param("actorName") String actorName);

}
