package com.query.hive.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 演员合作表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("actors_cooperation")
public class ActorsCooperation implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 演员1 ID
     */
    private Integer actor1Id;

    /**
     * 演员2 ID
     */
    private Integer actor2Id;

    /**
     * 演员1名称
     */
    private String actor1Name;

    /**
     * 演员2名称
     */
    private String actor2Name;

    /**
     * 合作电影数
     */
    private Integer movieNum;
}

