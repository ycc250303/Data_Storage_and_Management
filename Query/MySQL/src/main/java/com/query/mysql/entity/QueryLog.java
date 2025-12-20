package com.query.mysql.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * <p>
 * 查询日志表
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Data
@TableName("query_log")
public class QueryLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 日志ID
     */
    @TableId(value = "log_id", type = IdType.AUTO)
    private Integer logId;

    /**
     * 用户查询时的时间
     */
    private LocalDateTime queryTime;

    /**
     * 用户执行的查询语句
     */
    private String querySql;

    /**
     * 用户查询时传递的参数
     */
    private String queryParams;

    /**
     * 查询耗时
     */
    private Float queryDuration;

    /**
     * 查询类型
     */
    private String queryType;

    /**
     * 查询结果（成功、失败等）
     */
    private String queryResult;

    /**
     * 查询失败时的错误信息
     */
    private String errorMessage;
}
