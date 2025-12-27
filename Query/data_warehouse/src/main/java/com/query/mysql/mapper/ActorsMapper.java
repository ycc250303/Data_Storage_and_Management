package com.query.mysql.mapper;

import com.query.mysql.entity.Actors;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
    List<Map<String, Object>> getActorMoviesByExactName(@Param("actorName") String actorName);

    /**
     * 模糊匹配查询演员参演电影数量
     */
    List<Map<String, Object>> getActorMovieCountByActorName(@Param("actorName") String actorName);
}
