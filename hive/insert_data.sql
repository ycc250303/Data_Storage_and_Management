INSERT OVERWRITE TABLE movie_denormalization
SELECT m.id               AS movie_asin,
       m.movie_title      AS movie_title,
       m.score            AS movie_score,
       g.genre            AS movie_genre,
       rd.year            AS release_year,
       rd.month           AS release_month,
       rd.day             AS release_day,
       ceil(rd.month / 3) AS release_quarter,
       rd.weekday         AS release_weekday,
       a.actor_ids        AS actor_id_list,
       d.director_ids     AS director_id_list,
       e.edition_list     AS edition_list,
       a.actor_count      AS actor_count,
       d.director_count   AS director_count,
       e.edition_count    AS edition_count,
       m.review_num       AS review_count
FROM movies m
         LEFT JOIN movie_genres g
                   ON m.id = g.movie_id
         LEFT JOIN release_dates rd
                   ON rd.movie_id = m.id
         LEFT JOIN (SELECT movie_id,
                           concat_ws(',', collect_list(cast(actor_id AS string))) AS actor_ids,
                           count(*)                                               AS actor_count
                    FROM movie_actors
                    GROUP BY movie_id) a ON a.movie_id = m.id
         LEFT JOIN (SELECT movie_id,
                           concat_ws(',', collect_list(cast(director_id AS string))) AS director_ids,
                           count(*)                                                  AS director_count
                    FROM movie_directors
                    GROUP BY movie_id) d ON d.movie_id = m.id
         LEFT JOIN (SELECT movie_id,
                           concat_ws(',', collect_list(edition)) AS edition_list,
                           count(*)                              AS edition_count
                    FROM movie_editions
                    GROUP BY movie_id) e ON e.movie_id = m.id;

INSERT OVERWRITE TABLE actors_cooperation
SELECT ma1.actor_id                 AS actor1_id,
       ma2.actor_id                 AS actor2_id,
       a1.name                      AS actor1_name,
       a2.name                      AS actor2_name,
       count(distinct ma1.movie_id) AS movie_num
FROM movie_actors ma1
         JOIN movie_actors ma2
              ON ma1.movie_id = ma2.movie_id
                  AND ma1.actor_id < ma2.actor_id
         JOIN actors a1
              ON ma1.actor_id = a1.id
         JOIN actors a2
              ON ma2.actor_id = a2.id
GROUP BY ma1.actor_id, ma2.actor_id, a1.name, a2.name;

INSERT OVERWRITE TABLE actor_director_cooperation
SELECT ma.actor_id                 AS actor_id,
       md.director_id              AS director_id,
       a.name                      AS actor_name,
       d.name                      AS director_name,
       count(distinct ma.movie_id) AS movie_num
FROM movie_actors ma
         JOIN movie_directors md
              ON ma.movie_id = md.movie_id
         JOIN actors a
              ON ma.actor_id = a.id
         JOIN directors d
              ON md.director_id = d.id
GROUP BY ma.actor_id, md.director_id, a.name, d.name;

INSERT OVERWRITE TABLE director_stats
SELECT d.id,
       d.name,
       count(distinct md.movie_id) AS movie_count,
       avg(m.score)                AS avg_score
FROM directors d
         JOIN movie_directors md
              ON md.director_id = d.id
         JOIN movies m
              ON m.id = md.movie_id
GROUP BY d.id, d.name;

INSERT OVERWRITE TABLE actor_stats
SELECT a.id,
       a.name,
       count(distinct ma.movie_id) AS movie_count,
       avg(m.score)                AS avg_score
FROM actors a
         JOIN movie_actors ma
              ON ma.actor_id = a.id
         JOIN movies m
              ON m.id = ma.movie_id
GROUP BY a.id, a.name;

INSERT OVERWRITE TABLE movie_yearly_stats
SELECT rd.year            AS release_year,
       count(rd.movie_id) AS total_movies,
       avg(m.score)       AS avg_score
FROM release_dates rd
         JOIN movies m
              ON m.id = rd.movie_id
GROUP BY rd.year;

INSERT OVERWRITE TABLE movie_monthly_stats
SELECT rd.year            AS release_year,
       rd.month           AS release_month,
       count(rd.movie_id) AS total_movies,
       avg(m.score)       AS avg_score
FROM release_dates rd
         JOIN movies m
              ON m.id = rd.movie_id
GROUP BY rd.year, rd.month;

INSERT OVERWRITE TABLE movie_weekday_stats
SELECT rd.weekday,
       count(*)     AS total_movies,
       avg(m.score) AS avg_score
FROM release_dates rd
         JOIN movies m
              ON m.id = rd.movie_id
GROUP BY rd.weekday;

INSERT OVERWRITE TABLE movie_genre_stats
SELECT g.genre,
       count(m.id)  AS total_movies,
       avg(m.score) AS average_score
FROM movies m
         JOIN movie_genres g
              ON m.id = g.movie_id
GROUP BY g.genre;
