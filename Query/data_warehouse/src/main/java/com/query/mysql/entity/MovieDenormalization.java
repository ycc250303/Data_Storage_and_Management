package com.query.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 电影信息denormalization表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("movie_denormalization")
public class MovieDenormalization implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 电影ID
     */
    private Integer movieAsin;

    /**
     * 电影标题
     */
    private String movieTitle;

    /**
     * 平均评分（0-5）
     */
    private Float movieScore;

    /**
     * 电影风格
     */
    private String movieGenre;

    /**
     * 上映年份
     */
    private Short releaseYear;

    /**
     * 上映月份（1-12）
     */
    private Byte releaseMonth;

    /**
     * 上映日（1-31）
     */
    private Byte releaseDay;

    /**
     * 上映季度（1-4）
     */
    private Byte releaseQuarter;

    /**
     * 上映星期（0=周一）
     */
    private Byte releaseWeekday;

    /**
     * 演员ID列表(逗号分割)
     */
    private String actorIdList;

    /**
     * 导演ID列表(逗号分割)
     */
    private String directorIdList;

    /**
     * 电影版本列表
     */
    private String editionList;

    /**
     * 演员数
     */
    private Byte actorCount;

    /**
     * 导演数
     */
    private Byte directorCount;

    /**
     * 版本数
     */
    private Byte editionCount;

    /**
     * 评论数
     */
    private Integer reviewCount;
}

