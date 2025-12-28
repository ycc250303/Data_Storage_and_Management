package com.query.hive.mapper;

import com.query.hive.dto.MovieSearchDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * Hive电影Mapper接口
 */
@Mapper
public interface MoviesMapper {
    /**
     * 根据电影标题获取电影版本
     */
    List<Map<String, Object>> getMovieEditionsByMovieTitle(@Param("movieTitle") String movieTitle);

    /**
     * 根据电影标题获取电影版本数量
     */
    List<Map<String, Object>> getMovieEditionCountByMovieTitle(@Param("movieTitle") String movieTitle);

    /**
     * 根据类型获取电影数量
     */
    List<Map<String, Object>> getMovieCountByGenre(@Param("movieGenre") String movieGenre);

    /**
     * 根据类型获取电影名称列表
     */
    List<Map<String, Object>> getMoviesByGenreName(@Param("movieGenre") String movieGenre);

    /**
     * 组合查询电影信息
     */
    List<Map<String, Object>> getMoviesByCombinedConditions(@Param("dto") MovieSearchDto dto);

    /**
     * 第一步：根据条件查询符合条件的电影 ID 列表
     */
    List<Integer> selectMovieIdsByConditions(@Param("dto") MovieSearchDto dto);

    /**
     * 第二步：根据 ID 列表，批量获取电影的完整详细信息（旧方法，已废弃）
     */
    @Deprecated
    List<Map<String, Object>> selectMoviesBatch(@Param("movieIds") List<Integer> movieIds);

    /**
     * 查询电影基本信息（id, movie_asin, movie_title, score）
     */
    List<Map<String, Object>> selectMoviesBasicInfo(@Param("movieIds") List<Integer> movieIds);

    /**
     * 查询电影对应的演员列表
     */
    List<Map<String, Object>> selectActorsByMovieIds(@Param("movieIds") List<Integer> movieIds);

    /**
     * 查询电影对应的导演列表
     */
    List<Map<String, Object>> selectDirectorsByMovieIds(@Param("movieIds") List<Integer> movieIds);

    /**
     * 查询电影对应的类型列表
     */
    List<Map<String, Object>> selectGenresByMovieIds(@Param("movieIds") List<Integer> movieIds);

    /**
     * 查询电影对应的发行日期
     */
    List<Map<String, Object>> selectReleaseDatesByMovieIds(@Param("movieIds") List<Integer> movieIds);

    /**
     * 查询电影对应的版本信息
     */
    List<Map<String, Object>> selectEditionsByMovieIds(@Param("movieIds") List<Integer> movieIds);

    /**
     * 基于宽表的快速组合查询
     */
    List<Map<String, Object>> selectMoviesFromWideTable(@Param("dto") MovieSearchDto dto);

    /**
     * 基于外部表的慢速组合查询（用于性能对比）
     */
    List<Map<String, Object>> selectMoviesFromExternalTables(@Param("dto") MovieSearchDto dto);
}
