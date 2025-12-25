package com.query.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 演员维度电影统计表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("actor_stats")
public class ActorStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 演员 ID
     */
    @TableId(value = "actor_id", type = IdType.INPUT)
    private Long actorId;

    /**
     * 演员名称
     */
    private String actorName;

    /**
     * 参演电影数
     */
    private Integer movieCount;

    /**
     * 参演电影平均评分
     */
    private Float avgScore;
}

