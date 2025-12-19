-- 清空所有表数据并重置自增主键
-- 注意：这些操作将永久删除所有数据，请谨慎使用！
-- 清空演员表
TRUNCATE TABLE data_warehouse.actors;
-- 清空导演表
TRUNCATE TABLE data_warehouse.directors;
-- 清空电影-演员关联表
TRUNCATE TABLE data_warehouse.movie_actors;
-- 清空电影-导演关联表
TRUNCATE TABLE data_warehouse.movie_directors;
-- 清空电影类型/风格表
TRUNCATE TABLE data_warehouse.movie_genres;
-- 清空电影版本表
TRUNCATE TABLE data_warehouse.movie_editions;
-- 清空电影主表
TRUNCATE TABLE data_warehouse.movies;
-- 清空查询日志表
TRUNCATE TABLE data_warehouse.query_log;
-- 清空上映/发行日期表
TRUNCATE TABLE data_warehouse.release_dates;
-- 清空电影评论表
TRUNCATE TABLE data_warehouse.reviews;
-- 清空查询日志表（第二个同名表）
TRUNCATE TABLE query_log;
-- 清空电影信息denormalization表
TRUNCATE TABLE movie_denormalization;
-- 清空演员合作表
TRUNCATE TABLE actors_cooperation;
-- 清空演员-导演合作表
TRUNCATE TABLE actor_director_cooperation;
-- 清空电影年度统计表
TRUNCATE TABLE movie_yearly_stats;
-- 清空电影月度统计表
TRUNCATE TABLE movie_monthly_stats;