package com.query.mysql.service.impl;

import com.query.mysql.entity.QueryLog;
import com.query.mysql.mapper.QueryLogMapper;
import com.query.mysql.service.QueryLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 查询日志服务实现类
 */
@Service
public class QueryLogServiceImpl extends ServiceImpl<QueryLogMapper, QueryLog> implements QueryLogService {

    /**
     * 异步保存查询日志，避免影响主流程性能
     */
    @Override
    @Async
    public void saveQueryLogAsync(QueryLog queryLog) {
        try {
            this.save(queryLog);
        } catch (Exception e) {
            // 日志保存失败不影响主流程，只打印错误
            System.err.println("保存查询日志失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}