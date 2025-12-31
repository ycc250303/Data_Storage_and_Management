package com.query.mysql.controller;

import com.query.mysql.dto.MovieDetailDto;
import com.query.mysql.dto.MovieSearchDto;
import com.query.mysql.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mysql/movie")
public class MovieController {

    @Autowired
    private MovieService movieService;

    @GetMapping("/editions")
    public ResponseEntity<List<Map<String, Object>>> getMovieEditionsByMovieTitle(
            @RequestParam String movieTitle) {
        return ResponseEntity.ok(movieService.getMovieEditions(movieTitle));
    }

    @GetMapping("/edition-count")
    public ResponseEntity<List<Map<String, Object>>> getMovieEditionCountByMovieTitle(
            @RequestParam String movieTitle) {
        return ResponseEntity.ok(movieService.getMovieEditionCount(movieTitle));
    }

    @GetMapping("/genre-count")
    public ResponseEntity<List<Map<String, Object>>> getMovieCountByGenre(@RequestParam String movieGenre) {
        return ResponseEntity.ok(movieService.getMovieCountByGenre(movieGenre));
    }

    /**
     * 根据电影类别名称获取电影名称列表
     */
    @GetMapping("/by-genre")
    public ResponseEntity<List<Map<String, Object>>> getMoviesByGenreName(@RequestParam String movieGenre) {
        return ResponseEntity.ok(movieService.getMoviesByGenreName(movieGenre));
    }

    /**
     * 组合查询电影信息
     * 支持多条件查询：电影标题、导演、演员、类型、日期范围、评分范围等
     */
    @PostMapping("/search")
    public ResponseEntity<List<MovieDetailDto>> getMoviesByCombinedConditions(
            @RequestBody MovieSearchDto searchDto) {

        return ResponseEntity.ok(movieService.getMoviesByCombinedConditions(searchDto));
    }

    /**
     * 利用宽表进行组合查询
     */
    @PostMapping("/search/fast")
    public ResponseEntity<List<MovieDetailDto>> searchMoviesByWideTable(
            @RequestBody MovieSearchDto searchDto) {
        return ResponseEntity.ok(movieService.searchMoviesByWideTable(searchDto));
    }

    /**
     * 根据ID获取电影详情 (仿Neo4j接口)
     */
    @GetMapping("/detail")
    public ResponseEntity<MovieDetailDto> getMovieDetailById(@RequestParam String id) {
        return ResponseEntity.ok(movieService.getMovieDetailById(id));
    }
}
