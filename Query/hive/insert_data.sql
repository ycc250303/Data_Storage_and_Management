-- 1. 调整会话参数，防止聚合字符串过短
SET SESSION group_concat_max_len = 1048576;

-- 2. 插入宽表数据
TRUNCATE TABLE movie_denormalization;
INSERT INTO movie_denormalization (
    movie_asin, movie_title, movie_score, movie_genre, 
    release_year, release_month, release_day, release_quarter, release_weekday,
    actor_names, director_names, actor_id_list, director_id_list, edition_list,
    actor_count, director_count, review_count
)
SELECT 
    m.movie_asin, m.movie_title, m.score, g.genre_list,
    rd.year, rd.month, rd.day, CEIL(rd.month / 3), rd.weekday,
    a.names, d.names, a.ids, d.ids, e.editions,
    IFNULL(a.cnt, 0), IFNULL(d.cnt, 0), m.review_num
FROM movies m
LEFT JOIN (
    SELECT movie_id, GROUP_CONCAT(genre SEPARATOR ', ') as genre_list 
    FROM movie_genres GROUP BY movie_id
) g ON m.id = g.movie_id
LEFT JOIN release_dates rd ON m.id = rd.movie_id
LEFT JOIN (
    SELECT ma.movie_id, 
           GROUP_CONCAT(act.name SEPARATOR ', ') as names,
           JSON_ARRAYAGG(ma.actor_id) as ids,
           COUNT(*) as cnt
    FROM movie_actors ma JOIN actors act ON ma.actor_id = act.id GROUP BY ma.movie_id
) a ON m.id = a.movie_id
LEFT JOIN (
    SELECT md.movie_id, 
           GROUP_CONCAT(dir.name SEPARATOR ', ') as names,
           JSON_ARRAYAGG(md.director_id) as ids,
           COUNT(*) as cnt
    FROM movie_directors md JOIN directors dir ON md.director_id = dir.id GROUP BY md.movie_id
) d ON m.id = d.movie_id
LEFT JOIN (
    SELECT movie_id, GROUP_CONCAT(edition SEPARATOR ', ') as editions
    FROM movie_editions GROUP BY movie_id
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