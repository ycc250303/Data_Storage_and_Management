package com.query.mysql.controller;

import com.query.mysql.service.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/actor")
public class ActorController {

    @Autowired
    private ActorService actorService;

    /**
     * 模糊匹配查询演员参演电影数量
     */
    @GetMapping("/movie-count")
    public ResponseEntity<List<Map<String, Object>>> getActorMovieCount(
            @RequestParam String actorName) {
        return ResponseEntity.ok(actorService.getActorMovieCount(actorName));
    }

    /**
     * 精确匹配查询演员参演的电影列表
     */
    @GetMapping("/movies")
    public ResponseEntity<List<Map<String, Object>>> getActorMovies(
            @RequestParam String actorName) {
        return ResponseEntity.ok(actorService.getActorMovies(actorName));
    }
}
