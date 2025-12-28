package com.query.mysql.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.query.mysql.entity.Movies;
import com.query.mysql.mapper.MoviesMapper;
import com.query.mysql.mapper.ReleaseDatesMapper;
import com.query.mysql.service.TimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TimeServiceImpl extends ServiceImpl<MoviesMapper, Movies> implements TimeService {

    @Autowired
    private ReleaseDatesMapper releaseDatesMapper;

    @Override
    public List<Map<String, Object>> getMovieCountByYear(int year) {
        return releaseDatesMapper.getMovieCountByYear(year);
    }

    @Override
    public List<Map<String, Object>> getMovieCountByYearAndMonth(int year, int month) {
        return releaseDatesMapper.getMovieCountByYearAndMonth(year, month);
    }

    @Override
    public List<Map<String, Object>> getMovieCountByYearAndQuarter(int year, int quarter) {
        return releaseDatesMapper.getMovieCountByYearAndQuarter(year, quarter);
    }

    @Override
    public List<Map<String, Object>> getMovieCountByWeekday(int weekday) {
        return releaseDatesMapper.getMovieCountByWeekday(weekday);
    }
}