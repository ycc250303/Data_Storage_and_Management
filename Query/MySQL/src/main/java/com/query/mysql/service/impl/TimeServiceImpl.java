package com.query.mysql.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.query.mysql.entity.Movies;
import com.query.mysql.mapper.MovieMonthlyStatsMapper;
import com.query.mysql.mapper.MovieWeekdayStatsMapper;
import com.query.mysql.mapper.MovieYearlyStatsMapper;
import com.query.mysql.mapper.MoviesMapper;
import com.query.mysql.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TimeServiceImpl extends ServiceImpl<MoviesMapper, Movies> implements TimeService {

    @Autowired
    private MovieYearlyStatsMapper movieYearlyStatsMapper;

    @Autowired
    private MovieMonthlyStatsMapper movieMonthlyStatsMapper;

    @Autowired
    private MovieWeekdayStatsMapper movieWeekdayStatsMapper;

    @Override
    public List<Map<String, Object>> getMovieCountByYear(int year) {
        return movieYearlyStatsMapper.getMovieCountByYear(year);
    }

    @Override
    public List<Map<String, Object>> getMovieCountByYearAndMonth(int year, int month) {
        return movieMonthlyStatsMapper.getMovieCountByYearAndMonth(year, month);
    }

    @Override
    public List<Map<String, Object>> getMovieCountByYearAndQuarter(int year, int quarter) {
        return movieMonthlyStatsMapper.getMovieCountByYearAndQuarter(year, quarter);
    }

    @Override
    public List<Map<String, Object>> getMovieCountByWeekday(int weekday) {
        return movieWeekdayStatsMapper.getMovieCountByWeekday(weekday);
    }
}
