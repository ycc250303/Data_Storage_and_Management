package com.query.mysql.service.impl;

import com.query.mysql.mapper.ActorsMapper;
import com.query.mysql.service.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 演员服务实现类
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Service
public class ActorServiceImpl implements ActorService {

    @Autowired
    ActorsMapper actorsMapper;

    @Override
    public List<Map<String, Object>> getActorMovieCount(String actorName) {
        List<Map<String, Object>> results = actorsMapper.getActorMovieCountByActorName(actorName);
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return results.stream()
                .map(record -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("avgScore", record.get("avg_score"));
                    map.put("movieCount", record.get("movie_count"));
                    map.put("actorName", record.get("actor_name"));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getActorMovies(String actorName) {
        List<Map<String, Object>> results = actorsMapper.getActorMoviesByExactName(actorName);
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
