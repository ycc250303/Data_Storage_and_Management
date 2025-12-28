package com.query.mysql.config;

import com.query.mysql.aspect.SqlContext;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Properties;

/**
 * MyBatis 拦截器：捕获执行的 SQL 和返回行数
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
@Component
public class MyBatisInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];
        BoundSql boundSql;
        if (args.length == 6) {
            boundSql = (BoundSql) args[5];
        } else {
            boundSql = ms.getBoundSql(parameter);
        }

        // 获取执行的 SQL
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();
        SqlContext.setSql(sql);

        // 执行查询
        Object result = invocation.proceed();

        // 统计返回行数
        if (result instanceof Collection) {
            SqlContext.setRows(((Collection<?>) result).size());
        } else if (result != null) {
            SqlContext.setRows(1);
        } else {
            SqlContext.setRows(0);
        }

        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
    }
}

