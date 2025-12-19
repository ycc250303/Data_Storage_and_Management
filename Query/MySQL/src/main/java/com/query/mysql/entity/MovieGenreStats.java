package com.query.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 电影风格统计表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("movie_genre_stats")
public class MovieGenreStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 电影风格
     */
    @TableId(value = "genre", type = IdType.INPUT)
    private String genre;

    /**
     * 电影数
     */
    private Integer totalMovies;

    /**
     * 平均评分
     */
    private Float averageScore;
}

