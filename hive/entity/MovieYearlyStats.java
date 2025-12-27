package com.query.hive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 电影年度统计表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("movie_yearly_stats")
public class MovieYearlyStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 上映年份
     */
    private Integer releaseYear;

    /**
     * 上映电影数
     */
    private Integer totalMovies;

    /**
     * 平均评分
     */
    private Float avgScore;
}

