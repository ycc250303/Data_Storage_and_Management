-- 1. 基础设置
SET hive.exec.dynamic.partition.mode=nonstrict;
SET hive.exec.parallel=true;

-- 2. 执行插入（注意：如果你的表是按 release_year 分区的，必须带上 PARTITION 关键字）
INSERT OVERWRITE TABLE movie_denormalization PARTITION(release_year)
SELECT
    m.id               AS movie_id,
    m.movie_asin       AS movie_asin,
    m.movie_title      AS movie_title,
    m.score            AS movie_score,
    g.genres           AS movie_genres,
    -- Hive 拼接日期并转为 DATE 类型
    CAST(CONCAT(rd.year, '-', LPAD(rd.month, 2, '0'), '-', LPAD(rd.day, 2, '0')) AS DATE) AS release_date,
    rd.month           AS release_month,
    rd.day             AS release_day,
    CEIL(rd.month / 3) AS release_quarter,
    rd.weekday         AS release_weekday,
    a.actor_names      AS actor_names,
    d.director_names   AS director_names,
    e.edition_list     AS edition_list,
    -- Hive 使用 NVL 代替 IFNULL
    CAST(NVL(a.actor_count, 0) AS SMALLINT) AS actor_count,
    CAST(NVL(d.director_count, 0) AS SMALLINT) AS director_count,
    CAST(NVL(e.edition_count, 0) AS SMALLINT) AS edition_count,
    m.review_num       AS review_count,
    -- 预生成搜索索引列（Hive 的拼接方式）
    LOWER(CONCAT_WS(' ', m.movie_title, NVL(a.actor_names_str, ''), NVL(d.director_names_str, ''))) AS search_text,
    rd.year            AS release_year -- 分区字段必须放在最后
FROM movies m
         LEFT JOIN (
    SELECT movie_id, collect_set(genre) AS genres
    FROM movie_genres GROUP BY movie_id
) g ON m.id = g.movie_id
         LEFT JOIN release_dates rd ON m.id = rd.movie_id
         LEFT JOIN (
    -- 演员信息聚合
    SELECT ma.movie_id,
           collect_set(act.name) AS actor_names,
           concat_ws(' ', collect_set(act.name)) AS actor_names_str,
           count(DISTINCT ma.actor_id) AS actor_count
    FROM movie_actors ma
             JOIN actors act ON ma.actor_id = act.id
    GROUP BY ma.movie_id
) a ON m.id = a.movie_id
         LEFT JOIN (
    -- 导演信息聚合
    SELECT md.movie_id,
           collect_set(dir.name) AS director_names,
           concat_ws(' ', collect_set(dir.name)) AS director_names_str,
           count(DISTINCT md.director_id) AS director_count
    FROM movie_directors md
             JOIN directors dir ON md.director_id = dir.id
    GROUP BY md.movie_id
) d ON m.id = d.movie_id
         LEFT JOIN (
    -- 版本信息聚合
    SELECT movie_id,
           collect_set(edition) AS edition_list,
           count(DISTINCT edition) AS edition_count
    FROM movie_editions
    GROUP BY movie_id
) e ON m.id = e.movie_id;

-- 3. 利用宽表快速填充统计表 (零 JOIN，极速)
INSERT INTO movie_yearly_stats SELECT release_year, COUNT(*), AVG(movie_score) FROM movie_denormalization GROUP BY release_year;
INSERT INTO movie_monthly_stats SELECT release_year, release_month, COUNT(*), AVG(movie_score) FROM movie_denormalization GROUP BY release_year, release_month;
INSERT INTO movie_weekday_stats SELECT release_weekday, COUNT(*), AVG(movie_score) FROM movie_denormalization GROUP BY release_weekday;

-- 4. 合作关系与人物统计 (仍需基础表)
INSERT INTO actors_cooperation (actor1_id, actor2_id, actor1_name, actor2_name, movie_num)
SELECT ma1.actor_id, ma2.actor_id, a1.name, a2.name, COUNT(*)
FROM movie_actors ma1
JOIN movie_actors ma2 ON ma1.movie_id = ma2.movie_id AND ma1.actor_id < ma2.actor_id
JOIN actors a1 ON ma1.actor_id = a1.id
JOIN actors a2 ON ma2.actor_id = a2.id
GROUP BY ma1.actor_id, ma2.actor_id, a1.name, a2.name;

INSERT INTO actor_stats SELECT a.id, a.name, COUNT(ma.movie_id), AVG(m.score)
FROM actors a JOIN movie_actors ma ON a.id = ma.actor_id JOIN movies m ON ma.movie_id = m.id GROUP BY a.id, a.name;

INSERT INTO director_stats SELECT d.id, d.name, COUNT(md.movie_id), AVG(m.score)
FROM directors d JOIN movie_directors md ON d.id = md.director_id JOIN movies m ON md.movie_id = m.id GROUP BY d.id, d.name;