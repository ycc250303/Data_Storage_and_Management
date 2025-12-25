package com.query.common.service;

import com.query.common.entity.QueryLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 查询日志服务接口
 */
public interface QueryLogService extends IService<QueryLog> {
    /**
     * 异步保存查询日志
     */
    void saveQueryLogAsync(QueryLog queryLog);
}
