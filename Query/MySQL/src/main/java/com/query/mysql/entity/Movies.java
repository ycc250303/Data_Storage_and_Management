package com.query.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 电影主表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("movies")
public class Movies implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 电影 ASIN（唯一业务主键）
     */
    private String movieAsin;

    /**
     * 电影标题
     */
    private String movieTitle;

    /**
     * 平均评分（0-5）
     */
    private Double score;

    /**
     * 评论数统计（缓存）
     */
    private Integer reviewNum;
}
