-- 插入数据脚本
-- 注意：执行此脚本前，请确保已执行 create_table_mysql.sql 创建所有表

-- 插入电影信息denormalization表数据
INSERT INTO movie_denormalization(movie_asin, movie_title, movie_score, movie_genre, release_year, release_month,
                                  release_day, release_quarter, release_weekday, actor_id_list, director_id_list,
                                  edition_list, actor_count, director_count, edition_count, review_count)
SELECT m.id               AS movie_asin,
       m.movie_title      AS movie_title,
       m.score            AS movie_score,
       g.genre            AS movie_genre,
       rd.year            AS release_year,
       rd.month           AS release_month,
       rd.day             AS release_day,
       CEIL(rd.month / 3) AS release_quarter,
       rd.weekday         AS release_weekday,
       a.actor_ids        AS actor_id_list,
       d.director_ids     AS director_id_list,
       e.edition_list     AS edition_list,
       a.actor_count      AS actor_count,
       d.director_count   AS director_count,
       e.edition_count    AS edition_count,
       m.review_num       AS review_count
FROM movies m
         LEFT JOIN movie_genres g ON m.id = g.movie_id
         LEFT JOIN release_dates rd ON rd.movie_id = m.id
         LEFT JOIN (SELECT movie_id,
                           GROUP_CONCAT(actor_id ORDER BY actor_id) AS actor_ids,
                           COUNT(*)                                 AS actor_count
                    FROM movie_actors
                    GROUP BY movie_id) a ON a.movie_id = m.id
         LEFT JOIN (SELECT movie_id,
                           GROUP_CONCAT(director_id ORDER BY director_id) AS director_ids,
                           COUNT(*)                                       AS director_count
                    FROM movie_directors
                    GROUP BY movie_id) d ON d.movie_id = m.id
         LEFT JOIN (SELECT movie_id,
                           GROUP_CONCAT(edition ORDER BY edition) AS edition_list,
                           COUNT(*)                               AS edition_count
                    FROM movie_editions
                    GROUP BY movie_id) e ON e.movie_id = m.id;

-- 插入演员合作表数据
INSERT INTO actors_cooperation (actor1_id, actor2_id, actor1_name, actor2_name, movie_num)
SELECT ma1.actor_id                 AS actor1_id,
       ma2.actor_id                 AS actor2_id,
       a1.name                      AS actor1_name,
       a2.name                      AS actor2_name,
       COUNT(DISTINCT ma1.movie_id) AS movie_num
FROM movie_actors ma1
         JOIN movie_actors ma2 ON ma1.actor_id < ma2.actor_id AND ma1.movie_id = ma2.movie_id
         JOIN actors a1 ON ma1.actor_id = a1.id
         JOIN actors a2 ON ma2.actor_id = a2.id
GROUP BY ma1.actor_id, ma2.actor_id;

-- 插入演员-导演合作表数据
INSERT INTO actor_director_cooperation (actor_id, director_id, actor_name, director_name, movie_num)
SELECT ma.actor_id                 AS actor1_id,
       md.director_id              AS director_id,
       a.name                      AS actor_name,
       d.name                      AS director_name,
       COUNT(DISTINCT ma.movie_id) AS movie_num
FROM movie_actors ma
         JOIN movie_directors md ON ma.movie_id = md.movie_id
         JOIN actors a ON ma.actor_id = a.id
         JOIN directors d ON md.director_id = d.id
GROUP BY ma.actor_id, md.director_id;

-- 插入导演维度电影统计表数据
INSERT INTO director_stats (director_id, director_name, movie_count, avg_score)
SELECT d.id,
       d.name,
       COUNT(DISTINCT md.movie_id),
       AVG(m.score)
FROM directors d
         JOIN movie_directors md ON md.director_id = d.id
         JOIN movies m ON m.id = md.movie_id
GROUP BY d.id, d.name;

-- 插入演员维度电影统计表数据
INSERT INTO actor_stats (actor_id, actor_name, movie_count, avg_score)
SELECT a.id,
       a.name,
       COUNT(DISTINCT ma.movie_id),
       AVG(m.score)
FROM actors a
         JOIN movie_actors ma ON ma.actor_id = a.id
         JOIN movies m ON m.id = ma.movie_id
GROUP BY a.id, a.name;

-- 插入电影年度统计表数据
INSERT INTO movie_yearly_stats(release_year, total_movies, avg_score)
SELECT rd.year            AS release_year,
       COUNT(rd.movie_id) AS total_movies,
       AVG(m.score)       AS avg_score
FROM release_dates rd
         JOIN movies m ON m.id = rd.movie_id
GROUP BY rd.year;

-- 插入电影月度统计表数据
INSERT INTO movie_monthly_stats(release_year, release_month, total_movies, avg_score)
SELECT rd.year            AS release_year,
       rd.month           AS release_month,
       COUNT(rd.movie_id) AS total_movies,
       AVG(m.score)       AS avg_score
FROM release_dates rd
         JOIN movies m ON m.id = rd.movie_id
GROUP BY rd.year, rd.month;

-- 插入电影星期统计表数据
INSERT INTO movie_weekday_stats (release_weekday, total_movies, avg_score)
SELECT rd.weekday,
       COUNT(*)     AS total_movies,
       AVG(m.score) AS avg_score
FROM release_dates rd
         JOIN movies m ON m.id = rd.movie_id
GROUP BY rd.weekday;

-- 插入电影风格统计表数据
INSERT INTO movie_genre_stats(genre, total_movies, average_score)
SELECT g.genre      AS genre,
       COUNT(m.id)  AS total_movies,
       AVG(m.score) AS average_score
FROM movies m
         JOIN movie_genres g ON m.id = g.movie_id
GROUP BY g.genre;

