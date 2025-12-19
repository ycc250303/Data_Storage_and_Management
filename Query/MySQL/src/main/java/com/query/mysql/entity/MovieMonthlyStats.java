package com.query.mysql.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 电影月度统计表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("movie_monthly_stats")
public class MovieMonthlyStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 上映年份
     */
    @TableField("release_year")
    private Integer releaseYear;

    /**
     * 上映月份
     */
    @TableField("release_month")
    private Integer releaseMonth;

    /**
     * 上映电影数
     */
    private Integer totalMovies;

    /**
     * 平均评分
     */
    private Float avgScore;
}

