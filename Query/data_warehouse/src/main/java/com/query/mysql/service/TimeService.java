package com.query.mysql.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.query.mysql.entity.Movies;

import java.util.List;
import java.util.Map;

public interface TimeService extends IService<Movies> {
    public List<Map<String, Object>> getMovieCountByYear(int year);

    public List<Map<String, Object>> getMovieCountByYearAndMonth(int year, int month);

    public List<Map<String, Object>> getMovieCountByYearAndQuarter(int year, int quarter);

    public List<Map<String, Object>> getMovieCountByWeekday(int weekday);
}
