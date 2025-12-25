package com.query.mysql.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 电影星期统计表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("movie_weekday_stats")
public class MovieWeekdayStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 上映星期（0=周一）
     */
    @TableField("release_weekday")
    private Byte releaseWeekday;

    /**
     * 新增电影数
     */
    @TableField("total_movies")
    private Integer totalMovies;

    /**
     * 平均评分
     */
    @TableField("avg_score")
    private Float avgScore;
}

