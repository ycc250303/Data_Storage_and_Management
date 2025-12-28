package com.query.mysql.aspect;

/**
 * 用于在不同层之间传递 SQL 信息和返回行数
 */
public class SqlContext {
    private static final ThreadLocal<String> SQL_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<Integer> ROWS_HOLDER = new ThreadLocal<>();

    public static void setSql(String sql) {
        SQL_HOLDER.set(sql);
    }

    public static String getSql() {
        return SQL_HOLDER.get();
    }

    public static void setRows(Integer rows) {
        ROWS_HOLDER.set(rows);
    }

    public static Integer getRows() {
        return ROWS_HOLDER.get();
    }

    public static void clear() {
        SQL_HOLDER.remove();
        ROWS_HOLDER.remove();
    }
}

