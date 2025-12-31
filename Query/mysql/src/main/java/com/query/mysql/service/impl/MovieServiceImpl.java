package com.query.mysql.service.impl;

import com.query.mysql.dto.MovieDetailDto;
import com.query.mysql.dto.MovieSearchDto;
import com.query.mysql.mapper.*;
import com.query.mysql.entity.MovieEditions;
import com.query.mysql.entity.Movies;
import com.query.mysql.service.MovieService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 电影主表 服务实现类
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Slf4j
@Service
public class MovieServiceImpl extends ServiceImpl<MoviesMapper, Movies> implements MovieService {
    @Autowired
    MovieEditionsMapper movieEditionsMapper;

    @Autowired
    MovieGenresMapper movieGenresMapper;

    @Override
    public List<Map<String, Object>> getMovieEditions(String movieTitle) {
        // 由于我们只支持MySQL，不再需要传递databaseType参数
        List<Map<String, Object>> results = movieEditionsMapper.getMovieEditionsByMovieTitle(movieTitle);
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        return results.stream()
                .flatMap(this::expandEditions)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getMovieEditionCount(String movieTitle) {
        List<Map<String, Object>> results = movieEditionsMapper.getMovieEditionCountByMovieTitle(movieTitle);
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        return results.stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("movieTitle", record.get("movie_title"));
                    map.put("editionCount", record.get("edition_count"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getMovieCountByGenre(String movieGenre) {
        return movieGenresMapper.getMovieCountByGenre(movieGenre);
    }

    @Override
    public List<Map<String, Object>> getMoviesByGenreName(String movieGenre) {
        List<Map<String, Object>> results = movieGenresMapper.getMoviesByGenreName(movieGenre);
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return results.stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("movieTitle", record.get("movie_title"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<MovieDetailDto> getMoviesByCombinedConditions(MovieSearchDto dto) {
        // 创建一个新的DTO对象，用于传递给mapper，避免修改原始DTO
        MovieSearchDto searchDto = new MovieSearchDto();
        searchDto.setMovieTitle(dto.getMovieTitle() != null ? dto.getMovieTitle().trim() : null);
        searchDto.setActorName(dto.getActorName() != null ? dto.getActorName().trim() : null);
        searchDto.setDirectorName(dto.getDirectorName() != null ? dto.getDirectorName().trim() : null);
        searchDto.setMovieGenre(dto.getMovieGenre() != null ? dto.getMovieGenre().trim() : null);
        searchDto.setStartYear(dto.getStartYear());
        searchDto.setEndYear(dto.getEndYear());
        searchDto.setMonth(dto.getMonth());
        searchDto.setWeekday(dto.getWeekday());
        searchDto.setDay(dto.getDay());
        searchDto.setMinScore(dto.getMinScore());
        searchDto.setMaxScore(dto.getMaxScore());

        // 特殊处理：当page为-1时，表示查询全部结果
        if (dto.getPage() == -1) {
            // 查询全部结果，不分页
            searchDto.setPage(0);
            searchDto.setSize(2147483647); // 设置一个很大的值，确保获取所有结果，与XML中的条件一致
        } else {
            // 普通分页查询
            int offset = dto.getPage() * dto.getSize();
            searchDto.setPage(offset);
            searchDto.setSize(dto.getSize());
        }

        List<Map<String, Object>> results = baseMapper.getMoviesByCombinedConditions(searchDto);
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        // 使用传统for循环替代lambda表达式，便于调试
        List<MovieDetailDto> movieDetailDtoList = new ArrayList<>();

        for (Map<String, Object> record : results) {
            MovieDetailDto movieDetail = new MovieDetailDto();
            // 增加空值检查
            log.info("Processing record: {}", record);
            log.info("MovieAsin: {}", record.get("movieAsin"));
            movieDetail.setMovieAsin(record.get("movieAsin") != null ? record.get("movieAsin").toString()
                    : "" +
                            "");
            movieDetail.setMovieTitle(record.get("movieTitle") != null ? (String) record.get("movieTitle") : "");
            movieDetail.setMovieScore(record.get("movieScore") != null ? (float) record.get("movieScore") : 0.0f);
            movieDetail.setActors(record.get("actors") != null ? (String) record.get("actors") : "");
            movieDetail.setDirectors(record.get("directors") != null ? (String) record.get("directors") : "");
            movieDetail.setMovieGenre(record.get("movieGenre") != null ? (String) record.get("movieGenre") : "");
            movieDetail.setDate(record.get("date") != null ? (String) record.get("date") : "");
            movieDetail.setEdition(record.get("edition") != null ? (String) record.get("edition") : "");
            movieDetailDtoList.add(movieDetail);
        }

        log.info("Final result list size: {}", movieDetailDtoList.size());
        return movieDetailDtoList;
    }

    @Override
    public List<MovieDetailDto> searchMoviesByWideTable(MovieSearchDto dto) {
        log.info("开始执行MySQL宽表极速查询。参数：{}", dto);

        // 创建一个新的DTO对象，用于传递给mapper，避免修改原始DTO
        MovieSearchDto searchDto = new MovieSearchDto();
        searchDto.setMovieTitle(dto.getMovieTitle() != null ? dto.getMovieTitle().trim() : null);
        searchDto.setActorName(dto.getActorName() != null ? dto.getActorName().trim() : null);
        searchDto.setDirectorName(dto.getDirectorName() != null ? dto.getDirectorName().trim() : null);
        searchDto.setMovieGenre(dto.getMovieGenre() != null ? dto.getMovieGenre().trim() : null);
        searchDto.setStartYear(dto.getStartYear());
        searchDto.setEndYear(dto.getEndYear());
        searchDto.setMonth(dto.getMonth());
        searchDto.setWeekday(dto.getWeekday());
        searchDto.setDay(dto.getDay());
        searchDto.setMinScore(dto.getMinScore());
        searchDto.setMaxScore(dto.getMaxScore());

        // 处理分页逻辑
        if (dto.getPage() == -1) {
            searchDto.setPage(0);
            searchDto.setSize(10000);
        } else {
            int offset = dto.getPage() * dto.getSize();
            searchDto.setPage(offset);
            searchDto.setSize(dto.getSize());
        }

        // 一步到位，直接查询宽表
        List<Map<String, Object>> results = baseMapper.selectMoviesFromWideTable(searchDto);

        List<MovieDetailDto> movieDetailDtoList = new ArrayList<>();
        if (results != null) {
            for (Map<String, Object> record : results) {
                MovieDetailDto movieDetail = new MovieDetailDto();
                movieDetail.setMovieAsin(record.get("movieAsin") != null ? record.get("movieAsin").toString() : "");
                movieDetail.setMovieTitle(record.get("movieTitle") != null ? record.get("movieTitle").toString() : "");
                movieDetail.setMovieScore(record.get("movieScore") != null ? Float.parseFloat(record.get("movieScore").toString()) : 0.0f);
                movieDetail.setActors(record.get("actors") != null ? record.get("actors").toString() : "");
                movieDetail.setDirectors(record.get("directors") != null ? record.get("directors").toString() : "");
                movieDetail.setMovieGenre(record.get("movieGenre") != null ? record.get("movieGenre").toString() : "");
                movieDetail.setDate(record.get("release_date") != null ? record.get("release_date").toString() : "");
                movieDetail.setEdition(record.get("edition") != null ? record.get("edition").toString() : "");
                movieDetailDtoList.add(movieDetail);
            }
        }

        log.info("MySQL宽表查询完成，返回记录数：{}", movieDetailDtoList.size());
        return movieDetailDtoList;
    }

    @Override
    public MovieDetailDto getMovieDetailById(String id) {
        MovieSearchDto dto = new MovieSearchDto();
        dto.setMovieTitle(id); // 这里假设可以使用asin或ID查询，复用之前的逻辑
        dto.setPage(0);
        dto.setSize(1);
        List<MovieDetailDto> results = getMoviesByCombinedConditions(dto);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 展开版本列表，为每个版本创建一条记录
     */
    private Stream<Map<String, Object>> expandEditions(Map<String, Object> record) {
        String title = (String) record.get("movie_title");
        String editionListStr = (String) record.get("edition_list");

        if (editionListStr == null || editionListStr.isEmpty()) {
            Map<String, Object> map = new HashMap<>();
            map.put("movieTitle", title);
            map.put("edition", "");
            return Stream.of(map);
        }

        return Arrays.stream(editionListStr.split(","))
                .map(edition -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("movieTitle", title);
                    map.put("edition", edition.trim());
                    return map;
                });
    }
}