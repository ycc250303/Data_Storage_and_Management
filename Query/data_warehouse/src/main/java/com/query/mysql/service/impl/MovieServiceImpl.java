package com.query.mysql.service.impl;

import com.query.mysql.mapper.*;
import com.query.mysql.entity.MovieEditions;
import com.query.mysql.entity.Movies;
import com.query.mysql.service.MovieService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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