package com.query.hive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 电影-导演关联表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("movie_directors")
public class MovieDirectors implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 电影 ID
     */
    @TableId(value = "movie_id")
    private Long movieId;

    /**
     * 导演 ID
     */
    @TableField(value = "director_id")
    private Long directorId;
}
