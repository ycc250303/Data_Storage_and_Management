package com.query.hive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 演员-导演合作表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("actor_director_cooperation")
public class ActorDirectorCooperation implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 演员 ID
     */
    private Integer actorId;

    /**
     * 导演 ID
     */
    private Integer directorId;

    /**
     * 演员名称
     */
    private String actorName;

    /**
     * 导演名称
     */
    private String directorName;

    /**
     * 合作电影数
     */
    private Integer movieNum;
}

