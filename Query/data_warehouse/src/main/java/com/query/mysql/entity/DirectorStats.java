package com.query.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 导演维度电影统计表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("director_stats")
public class DirectorStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 导演 ID
     */
    @TableId(value = "director_id", type = IdType.INPUT)
    private Long directorId;

    /**
     * 导演名称
     */
    private String directorName;

    /**
     * 导演电影数
     */
    private Integer movieCount;

    /**
     * 导演电影平均评分
     */
    private Float avgScore;
}

