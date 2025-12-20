package com.query.mysql.controller;

import com.query.mysql.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/time")
public class TimeController {
    @Autowired
    private TimeService timeService;

    @GetMapping("/year-count")
    public ResponseEntity<List<Map<String, Object>>> getMovieCountByYear(@RequestParam int year) {
        return ResponseEntity.ok(timeService.getMovieCountByYear(year));
    }

    @GetMapping("/year-month-count")
    public ResponseEntity<List<Map<String, Object>>> getMovieCountByYearAndMonth(@RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(timeService.getMovieCountByYearAndMonth(year, month));
    }

    @GetMapping("/year-quarter-count")
    public ResponseEntity<List<Map<String, Object>>> getMovieCountByYearAndQuarter(@RequestParam int year,
            @RequestParam int quarter) {
        return ResponseEntity.ok(timeService.getMovieCountByYearAndQuarter(year, quarter));
    }

    @GetMapping("/weekday-count")
    public ResponseEntity<List<Map<String, Object>>> getMovieCountByWeekday(@RequestParam int weekday) {
        return ResponseEntity.ok(timeService.getMovieCountByWeekday(weekday));
    }
}
