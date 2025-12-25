package com.query.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.query.common.model.QueryResponse;
import com.query.common.entity.QueryLog;
import com.query.common.service.QueryLogService;
import com.query.common.config.SqlExecutionTimeInterceptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AOP切面：拦截Controller方法，包装返回结果并添加耗时信息，同时记录查询日志
 */
@Aspect
@Component
public class QueryTimeAspect {

    @Autowired
    private QueryLogService queryLogService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 拦截所有 com.query 子包下的 Controller 方法
     */
    @Around("execution(* com.query.*.controller.*.*(..))")
    public Object aroundController(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录总开始时间
        long totalStartTime = System.currentTimeMillis();
        LocalDateTime queryTime = LocalDateTime.now();

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 获取查询类型（Controller类名 + 方法名），限制在128个字符内
        String queryType = className + "." + methodName;
        if (queryType.length() > 128) {
            // 如果太长，只使用方法名，如果方法名也超过128，则截断
            if (methodName.length() <= 128) {
                queryType = methodName;
            } else {
                queryType = methodName.substring(0, 128);
            }
        }

        // 获取请求参数（JSON格式）
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();
        String queryParams = formatParamsAsJson(paramNames, args);

        Object result = null;
        String queryResult = "成功";
        String errorMessage = null;

        try {
            // 执行Controller方法
            result = joinPoint.proceed();

            // 计算总执行时间
            long totalEndTime = System.currentTimeMillis();
            long totalTime = totalEndTime - totalStartTime;

            // 获取SQL执行时间和SQL语句
            Long sqlTime = SqlExecutionTimeInterceptor.getSqlExecutionTime();
            List<String> sqlStatements = SqlExecutionTimeInterceptor.getSqlStatements();
            String querySql = formatSqlStatements(sqlStatements);

            // 如果返回的是ResponseEntity，需要特殊处理
            if (result instanceof ResponseEntity) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) result;
                Object body = responseEntity.getBody();

                // 如果body已经是QueryResponse，直接返回
                if (body instanceof QueryResponse) {
                    // 保存日志
                    saveQueryLog(queryTime, querySql, queryParams, totalTime, queryType,
                            queryResult, errorMessage);
                    SqlExecutionTimeInterceptor.clear();
                    return result;
                }

                // 包装成QueryResponse
                QueryResponse<Object> queryResponse = QueryResponse.success(
                        body,
                        sqlTime,
                        totalTime);

                // 保存日志
                saveQueryLog(queryTime, querySql, queryParams, totalTime, queryType,
                        queryResult, errorMessage);

                // 清除ThreadLocal
                SqlExecutionTimeInterceptor.clear();

                return ResponseEntity.ok(queryResponse);
            }

            // 如果返回的不是ResponseEntity，直接包装
            QueryResponse<Object> queryResponse = QueryResponse.success(
                    result,
                    sqlTime,
                    totalTime);

            // 保存日志
            saveQueryLog(queryTime, querySql, queryParams, totalTime, queryType,
                    queryResult, errorMessage);

            // 清除ThreadLocal
            SqlExecutionTimeInterceptor.clear();

            return queryResponse;

        } catch (Throwable e) {
            // 即使出现异常，也记录时间和日志
            long totalEndTime = System.currentTimeMillis();
            long totalTime = totalEndTime - totalStartTime;
            queryResult = "失败";
            errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.length() > 1024) {
                errorMessage = errorMessage.substring(0, 1024);
            }

            // 获取SQL语句
            List<String> sqlStatements = SqlExecutionTimeInterceptor.getSqlStatements();
            String querySql = formatSqlStatements(sqlStatements);

            // 保存日志
            saveQueryLog(queryTime, querySql, queryParams, totalTime, queryType,
                    queryResult, errorMessage);

            // 清除ThreadLocal
            SqlExecutionTimeInterceptor.clear();

            // 重新抛出异常
            throw e;
        }
    }

    /**
     * 保存查询日志
     */
    private void saveQueryLog(LocalDateTime queryTime, String querySql, String queryParams,
            long totalTime, String queryType, String queryResult,
            String errorMessage) {
        QueryLog queryLog = new QueryLog();
        queryLog.setQueryTime(queryTime);
        queryLog.setQuerySql(querySql);
        queryLog.setQueryParams(queryParams);
        queryLog.setQueryDuration((float) totalTime); // 转换为Float，单位毫秒

        // 限制queryType长度为128个字符（数据库字段限制）
        if (queryType != null && queryType.length() > 128) {
            queryType = queryType.substring(0, 128);
        }
        queryLog.setQueryType(queryType);

        // 限制queryResult长度为32个字符（数据库字段限制）
        if (queryResult != null && queryResult.length() > 32) {
            queryResult = queryResult.substring(0, 32);
        }
        queryLog.setQueryResult(queryResult);

        queryLog.setErrorMessage(errorMessage);

        // 异步保存日志
        queryLogService.saveQueryLogAsync(queryLog);
    }

    /**
     * 格式化参数为JSON字符串
     */
    private String formatParamsAsJson(String[] paramNames, Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        try {
            Map<String, Object> paramMap = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                String paramName = (paramNames != null && i < paramNames.length)
                        ? paramNames[i]
                        : "arg" + i;

                // 过滤掉Spring的内部参数（如HttpServletRequest等）
                if (args[i] != null) {
                    String className = args[i].getClass().getName();
                    if (!className.startsWith("javax.") &&
                            !className.startsWith("jakarta.") &&
                            !className.startsWith("org.springframework.")) {
                        paramMap.put(paramName, args[i]);
                    }
                }
            }

            if (paramMap.isEmpty()) {
                return null;
            }

            // 转换为JSON字符串
            // 注意：这里需要考虑循环引用等复杂情况，简单起见直接转换
            String json = objectMapper.writeValueAsString(paramMap);

            // 限制总长度
            if (json.length() > 1024) {
                json = json.substring(0, 1021) + "...";
            }

            return json;
        } catch (Exception e) {
            // JSON序列化失败，返回简单格式
            return formatParamsSimple(args);
        }
    }

    /**
     * 简单格式化参数（JSON序列化失败时的备用方案）
     */
    private String formatParamsSimple(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            if (args[i] != null) {
                String paramStr = args[i].toString();
                if (paramStr.length() > 200) {
                    paramStr = paramStr.substring(0, 200) + "...";
                }
                sb.append(paramStr);
            } else {
                sb.append("null");
            }
        }
        sb.append("]");
        String result = sb.toString();
        if (result.length() > 1024) {
            result = result.substring(0, 1024);
        }
        return result;
    }

    /**
     * 格式化SQL语句列表为字符串
     */
    private String formatSqlStatements(List<String> sqlStatements) {
        if (sqlStatements == null || sqlStatements.isEmpty()) {
            return null;
        }
        if (sqlStatements.size() == 1) {
            String sql = sqlStatements.get(0);
            // 限制SQL长度
            if (sql != null && sql.length() > 1024) {
                sql = sql.substring(0, 1021) + "...";
            }
            return sql;
        }
        // 多个SQL用分号分隔
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sqlStatements.size(); i++) {
            if (i > 0) {
                sb.append("; ");
            }
            String sql = sqlStatements.get(i);
            if (sql != null) {
                sb.append(sql);
            }
        }
        String result = sb.toString();
        // 限制总长度
        if (result.length() > 1024) {
            result = result.substring(0, 1021) + "...";
        }
        return result;
    }
}

