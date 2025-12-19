package com.query.mysql.service.impl;

import com.query.mysql.entity.Movies;
import com.query.mysql.mapper.MoviesMapper;
import com.query.mysql.service.MovieService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 电影主表 服务实现类
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Service
public class MovieServiceImpl extends ServiceImpl<MoviesMapper, Movies> implements MovieService {
    @Override
    public List<Map<String, Object>> getMovieEditions(String movieTitle) {
        List<Map<String, Object>> result =
    }
}
