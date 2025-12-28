package com.query.mysql.service.impl;

import com.query.mysql.mapper.DirectorsMapper;
import com.query.mysql.service.DirectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 导演服务实现类
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Service
public class DirectorServiceImpl implements DirectorService {

    @Autowired
    DirectorsMapper directorsMapper;

    @Override
    public List<Map<String, Object>> getDirectorMovieCount(String directorName) {
        // 使用DirectorsMapper中的getDirectorMovieCountByFuzzyName方法，而不是已删除的DirectorStatsMapper
        List<Map<String, Object>> results = directorsMapper.getDirectorMovieCountByFuzzyName(directorName);
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return results.stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("avgScore", record.get("avg_score"));
                    map.put("directorName", record.get("director_name"));
                    map.put("movieCount", record.get("movie_count"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getDirectorMovies(String directorName) {
        List<Map<String, Object>> results = directorsMapper.getDirectorMoviesByExactName(directorName);
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
}