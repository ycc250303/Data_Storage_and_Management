package com.query.mysql.mapper;

import com.query.mysql.entity.MovieGenres;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 电影类型/风格表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface MovieGenresMapper extends BaseMapper<MovieGenres> {

    /**
     * 根据电影类别名称查询电影名称列表
     */
    List<Map<String, Object>> getMoviesByGenreName(@Param("movieGenre") String movieGenre);

}
