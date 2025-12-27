SELECT id, name
INTO OUTFILE '/var/lib/mysql-files/actors.csv'
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
FROM data_warehouse.actors;

SELECT id, name
INTO OUTFILE '/var/lib/mysql-files/directors.csv'
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
FROM data_warehouse.directors;

SELECT
    id,
    movie_asin,
    movie_title,
    score,
    rated,
    language,
    review_num
INTO OUTFILE '/var/lib/mysql-files/movies.csv'
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
FROM data_warehouse.movies;

SELECT movie_id, actor_id
INTO OUTFILE '/var/lib/mysql-files/movie_actors.csv'
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
FROM data_warehouse.movie_actors;

SELECT movie_id, director_id
INTO OUTFILE '/var/lib/mysql-files/movie_directors.csv'
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
FROM data_warehouse.movie_directors;

SELECT id, movie_id, genre
INTO OUTFILE '/var/lib/mysql-files/movie_genres.csv'
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
FROM data_warehouse.movie_genres;

SELECT id, movie_id, edition
INTO OUTFILE '/var/lib/mysql-files/movie_editions.csv'
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
FROM data_warehouse.movie_editions;

SELECT id,movie_id,year,month,day,weekday
INTO OUTFILE '/var/lib/mysql-files/release_date.csv'
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
FROM data_warehouse.release_dates;

SELECT id,review_uuid,movie_id,helpfulness,profile_name,score,created_ts,summary,content,user_id
INTO OUTFILE '/var/lib/mysql-files/reviews.csv'
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
FROM data_warehouse.reviews;

