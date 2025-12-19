package com.query.mysql.controller;

import com.query.mysql.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movie")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/versions")
    public ResponseEntity<List<Map<String, Object>>> getMovieEditions(
            @RequestParam String movieTitle
    ){
        return ResponseEntity.ok(movieService.getMovieEditions(movieTitle));
    };
}
