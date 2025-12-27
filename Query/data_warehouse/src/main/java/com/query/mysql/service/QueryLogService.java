package com.query.mysql.service;

import com.query.mysql.entity.QueryLog;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.scheduling.annotation.Async;

/**
 * 查询日志服务接口
 */
public interface QueryLogService extends IService<QueryLog> {
    /**
     * 异步保存查询日志
     */
    void saveQueryLogAsync(QueryLog queryLog);
}
