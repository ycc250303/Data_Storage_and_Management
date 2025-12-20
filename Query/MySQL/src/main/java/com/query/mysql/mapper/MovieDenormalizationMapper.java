package com.query.mysql.mapper;

import com.query.mysql.entity.MovieDenormalization;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 电影信息denormalization表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface MovieDenormalizationMapper extends BaseMapper<MovieDenormalization> {

    @Select("SELECT movie_title, edition_list " +
            "FROM movie_denormalization " +
            "WHERE movie_title LIKE CONCAT('%', #{movieTitle}, '%')")
    List<Map<String, Object>> getMovieEditionsByMovieTitle(@Param("movieTitle") String movieTitle);

    @Select("SELECT movie_title,edition_count " +
            "FROM movie_denormalization " +
            "WHERE movie_title=#{movieTitle}")
    List<Map<String, Object>> getMovieEditionCountByMovieTitle(@Param("movieTitle") String movieTitle);
}
