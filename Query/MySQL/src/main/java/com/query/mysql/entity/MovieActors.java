package com.query.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 电影-演员关联表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("movie_actors")
public class MovieActors implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 电影 ID
     */
    @TableId(value = "movie_id", type = IdType.AUTO)
    private Long movieId;

    /**
     * 演员 ID
     */
    @TableField(value = "actor_id")
    private Long actorId;

    /**
     * 角色名
     */
    private String roleName;
}
