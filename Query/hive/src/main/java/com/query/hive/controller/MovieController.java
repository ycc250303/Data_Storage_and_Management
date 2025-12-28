package com.query.hive.controller;

import com.query.hive.dto.MovieDetailDto;
import com.query.hive.dto.MovieSearchDto;
import com.query.hive.service.MovieService;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/api/hive/movie")
public class MovieController {

    @Autowired
    private MovieService movieService;

    /**
     * 获取电影版本列表
     */
    @GetMapping("/editions")
    public ResponseEntity<List<Map<String, Object>>> getMovieEditionsByMovieTitle(
            @RequestParam String movieTitle) {
        return ResponseEntity.ok(movieService.getMovieEditions(movieTitle));
    }

    /**
     * 获取电影版本数量
     */
    @GetMapping("/edition-count")
    public ResponseEntity<List<Map<String, Object>>> getMovieEditionCountByMovieTitle(
            @RequestParam String movieTitle) {
        return ResponseEntity.ok(movieService.getMovieEditionCount(movieTitle));
    }

    /**
     * 获取指定类型的电影数量
     */
    @GetMapping("/genre-count")
    public ResponseEntity<List<Map<String, Object>>> getMovieCountByGenre(@RequestParam String movieGenre) {
        return ResponseEntity.ok(movieService.getMovieCountByGenre(movieGenre));
    }

    /**
     * 根据电影类型获取电影名称列表
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

        try {
            List<MovieDetailDto> result = movieService.getMoviesByCombinedConditions(searchDto);
            // log.info("搜索结果数量: {}", result.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // log.error("搜索电影时发生错误", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 慢速查询接口，用于性能对比（查询 _ext 外部表）
     */
    @PostMapping("/search/slow")
    public ResponseEntity<List<MovieDetailDto>> slowSearch(@RequestBody MovieSearchDto movieSearchDto) {
        try {
            List<MovieDetailDto> movies = movieService.searchMoviesByExternalTables(movieSearchDto);
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            log.error("慢速搜索电影时发生错误", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/search/fast")
    public ResponseEntity<List<MovieDetailDto>> fastSearch(@RequestBody MovieSearchDto movieSearchDto) {
        List<MovieDetailDto> movies = movieService.searchMoviesByWideTable(movieSearchDto);
        return ResponseEntity.ok(movies);
    }
}
