package com.query.hive.controller;

import com.query.hive.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hive/actor")
public class ActorsController {

    @Autowired
    private MovieService movieService;

    /**
     * 获取演员合作统计
     * 对应 Neo4j 的 /api/neo4j/stats/collaborations
     */
    @GetMapping("/collaborations")
    public ResponseEntity<List<Map<String, Object>>> getActorCollaborations(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String genre) {
        if (genre != null && !genre.isEmpty()) {
            return ResponseEntity.ok(movieService.getActorCollaborationsByGenre(genre, limit));
        }
        return ResponseEntity.ok(movieService.getActorCollaborations(limit));
    }

    /**
     * 获取导演-演员合作统计（合作次数）
     */
    @GetMapping("/director-collaborations")
    public ResponseEntity<List<Map<String, Object>>> getDirectorActorCollaborations(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String genre) {
        return ResponseEntity.ok(movieService.getDirectorActorCollaborations(genre, limit));
    }

    /**
     * 获取受关注的两人演员组合（评论最多）
     */
    @GetMapping("/collaborations-reviews")
    public ResponseEntity<List<Map<String, Object>>> getActorCollaborationsByReviews(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String genre) {
        return ResponseEntity.ok(movieService.getActorCollaborationsByReviews(genre, limit));
    }

    /**
     * 获取受关注的导演-演员组合（评论最多）
     */
    @GetMapping("/director-collaborations-reviews")
    public ResponseEntity<List<Map<String, Object>>> getDirectorActorCollaborationsByReviews(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(required = false) String genre) {
        return ResponseEntity.ok(movieService.getDirectorActorCollaborationsByReviews(genre, limit));
    }
}
