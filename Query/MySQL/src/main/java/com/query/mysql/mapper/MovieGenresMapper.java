package com.query.mysql.mapper;

import com.query.mysql.entity.MovieGenres;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
    @Select("SELECT DISTINCT m.movie_title " +
            "FROM data_warehouse.movie_genres mg " +
            "JOIN data_warehouse.movies m ON mg.movie_id = m.id " +
            "WHERE mg.genre = #{movieGenre} " +
            "ORDER BY m.movie_title")
    List<Map<String, Object>> getMoviesByGenreName(@Param("movieGenre") String movieGenre);

}
