package com.query.mysql.mapper;

import com.query.mysql.entity.Directors;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 导演表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface DirectorsMapper extends BaseMapper<Directors> {

    /**
     * 精确匹配查询导演导演的电影列表
     */
    List<Map<String, Object>> getDirectorMoviesByExactName(@Param("directorName") String directorName);

}
