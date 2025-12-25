package com.query.mysql.controller;

import com.query.mysql.service.DirectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/director")
public class DirectorController {

    @Autowired
    private DirectorService directorService;

    /**
     * 模糊匹配查询导演导演电影数量
     */
    @GetMapping("/movie-count")
    public ResponseEntity<List<Map<String, Object>>> getDirectorMovieCount(
            @RequestParam String directorName) {
        return ResponseEntity.ok(directorService.getDirectorMovieCount(directorName));
    }

    /**
     * 精确匹配查询导演导演的电影列表
     */
    @GetMapping("/movies")
    public ResponseEntity<List<Map<String, Object>>> getDirectorMovies(
            @RequestParam String directorName) {
        return ResponseEntity.ok(directorService.getDirectorMovies(directorName));
    }
}
