package com.query.mysql.mapper;

import com.query.mysql.entity.ActorStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 演员维度电影统计表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface ActorStatsMapper extends BaseMapper<ActorStats> {

    /**
     * 模糊匹配查询演员参演电影数量
     */
    @Select("SELECT actor_name, movie_count,avg_score " +
            "FROM actor_stats " +
            "WHERE actor_name LIKE CONCAT('%', #{actorName}, '%')")
    List<Map<String, Object>> getActorMovieCountByFuzzyName(@Param("actorName") String actorName);

}

