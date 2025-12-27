package com.query.hive.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Hive查询服务类
 */
@Service
public class HiveQueryService {

    @Autowired
    private JdbcTemplate hiveJdbcTemplate;

    /**
     * 执行查询并返回结果列表
     * @param sql SQL查询语句
     * @return 查询结果列表
     */
    public List<Map<String, Object>> executeQuery(String sql) {
        return hiveJdbcTemplate.queryForList(sql);
    }

    /**
     * 执行查询并返回单个值
     * @param sql SQL查询语句
     * @param clazz 返回值类型
     * @param <T> 泛型类型
     * @return 查询结果
     */
    public <T> T queryForObject(String sql, Class<T> clazz) {
        return hiveJdbcTemplate.queryForObject(sql, clazz);
    }

    /**
     * 执行Hive DDL操作
     * @param ddl DDL语句
     */
    public void executeDDL(String ddl) {
        hiveJdbcTemplate.execute(ddl);
    }

    /**
     * 获取表的所有数据
     * @param tableName 表名
     * @return 表数据
     */
    public List<Map<String, Object>> getAllDataFromTable(String tableName) {
        String sql = "SELECT * FROM " + tableName;
        return executeQuery(sql);
    }

    /**
     * 获取表的字段信息
     * @param tableName 表名
     * @return 表字段信息
     */
    public List<Map<String, Object>> getTableSchema(String tableName) {
        String sql = "DESCRIBE " + tableName;
        return executeQuery(sql);
    }

    /**
     * 获取所有表名
     * @return 表名列表
     */
    public List<String> getAllTables() {
        String sql = "SHOW TABLES";
        return hiveJdbcTemplate.queryForList(sql, String.class);
    }
}
