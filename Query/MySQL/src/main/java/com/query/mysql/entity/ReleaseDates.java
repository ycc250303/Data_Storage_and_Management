package com.query.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 上映/发行日期表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("release_dates")
public class ReleaseDates implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属电影 ID
     */
    private String movieId;

    /**
     * 上映年份
     */
    private Short year;

    /**
     * 上映月份（1-12）
     */
    private Byte month;

    /**
     * 上映日（1-31）
     */
    private Byte day;

    /**
     * 上映星期（0=周一）
     */
    private Byte weekday;
}
