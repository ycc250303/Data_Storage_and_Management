package com.query.mysql.config;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * MyBatis拦截器：统计SQL执行时间并记录SQL语句
 */
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {
                MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
        }),
        @Signature(type = Executor.class, method = "update", args = {
                MappedStatement.class, Object.class
        })
})
public class SqlExecutionTimeInterceptor implements Interceptor {

    /**
     * ThreadLocal存储SQL执行时间（毫秒）
     */
    private static final ThreadLocal<Long> SQL_EXECUTION_TIME = new ThreadLocal<>();

    /**
     * ThreadLocal存储SQL语句列表
     */
    private static final ThreadLocal<List<String>> SQL_STATEMENTS = new ThreadLocal<>();

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];

        try {
            // 获取SQL语句
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            String sql = boundSql.getSql();

            // 过滤掉保存日志的SQL（避免递归记录）
            String mappedStatementId = mappedStatement.getId();
            boolean isQueryLogInsert = mappedStatementId != null &&
                    mappedStatementId.contains("QueryLogMapper");

            // 记录SQL语句（排除QueryLog的插入操作）
            if (!isQueryLogInsert) {
                List<String> sqlList = SQL_STATEMENTS.get();
                if (sqlList == null) {
                    sqlList = new ArrayList<>();
                    SQL_STATEMENTS.set(sqlList);
                }
                // 清理SQL中的多余空白字符
                String cleanedSql = sql.replaceAll("\\s+", " ").trim();
                sqlList.add(cleanedSql);
            }

            // 执行SQL
            Object result = invocation.proceed();

            // 计算SQL执行时间
            long endTime = System.currentTimeMillis();
            long sqlTime = endTime - startTime;

            // 累加SQL执行时间（可能一次请求有多个SQL）
            Long existingTime = SQL_EXECUTION_TIME.get();
            if (existingTime == null) {
                SQL_EXECUTION_TIME.set(sqlTime);
            } else {
                SQL_EXECUTION_TIME.set(existingTime + sqlTime);
            }

            return result;
        } catch (Throwable e) {
            // 即使SQL执行失败，也记录时间和SQL
            long endTime = System.currentTimeMillis();
            long sqlTime = endTime - startTime;
            Long existingTime = SQL_EXECUTION_TIME.get();
            if (existingTime == null) {
                SQL_EXECUTION_TIME.set(sqlTime);
            } else {
                SQL_EXECUTION_TIME.set(existingTime + sqlTime);
            }
            throw e;
        }
    }

    /**
     * 获取当前线程的SQL执行时间
     */
    public static Long getSqlExecutionTime() {
        Long time = SQL_EXECUTION_TIME.get();
        return time == null ? 0L : time;
    }

    /**
     * 获取当前线程的SQL语句列表
     */
    public static List<String> getSqlStatements() {
        List<String> sqlList = SQL_STATEMENTS.get();
        return sqlList == null ? new ArrayList<>() : new ArrayList<>(sqlList);
    }

    /**
     * 清除当前线程的SQL执行时间和SQL语句
     */
    public static void clear() {
        SQL_EXECUTION_TIME.remove();
        SQL_STATEMENTS.remove();
    }
}