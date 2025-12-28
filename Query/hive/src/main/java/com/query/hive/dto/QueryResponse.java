package com.query.hive.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查询响应包装类，包含数据和总耗时信息
 */
@Data
@NoArgsConstructor
public class QueryResponse<T> {
    /**
     * 查询返回的数据
     */
    private T data;

    /**
     * 总执行时间（毫秒）- 从Controller方法开始到结束
     */
    private Long totalExecutionTime;

    /**
     * 自定义构造函数
     */
    public QueryResponse(T data, Long totalExecutionTime) {
        this.data = data;
        this.totalExecutionTime = totalExecutionTime;
    }

    /**
     * 成功响应的静态方法
     */
    public static <T> QueryResponse<T> success(T data, Long totalTime) {
        return new QueryResponse<>(data, totalTime);
    }
}
