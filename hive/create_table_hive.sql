-- 创建数据库
show tables;
-- actors 表
drop table if exists actors;
CREATE EXTERNAL TABLE IF NOT EXISTS actors
(
    id   BIGINT,
    name STRING
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar" = ",","quoteChar" = "\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/actors/'
    TBLPROPERTIES ("skip.header.line.count" = "1");
select count(*) from actors;



--------------------------------------------------
-- directors
--------------------------------------------------
drop table if exists directors;
CREATE EXTERNAL TABLE IF NOT EXISTS directors
(
    id   BIGINT,
    name STRING
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar" = ",","quoteChar" = "\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/directors/'
    TBLPROPERTIES ("skip.header.line.count" = "1");
select count(*) from directors;
--------------------------------------------------
-- movies
--------------------------------------------------
drop table if exists movies;
CREATE EXTERNAL TABLE IF NOT EXISTS movies
(
    id          BIGINT,
    movie_asin  STRING,
    movie_title STRING,
    score       FLOAT,
    rated       STRING,
    language    STRING,
    review_num  INT
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar" = ",","quoteChar" = "\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/movies/'
    TBLPROPERTIES ("skip.header.line.count" = "1");

--------------------------------------------------
-- movie_actors
--------------------------------------------------
drop table if exists movie_actors;
CREATE EXTERNAL TABLE IF NOT EXISTS movie_actors
(
    movie_id BIGINT,
    actor_id BIGINT
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar" = ",","quoteChar" = "\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/movie_actors/'
    TBLPROPERTIES ("skip.header.line.count" = "1");

--------------------------------------------------
-- movie_directors
--------------------------------------------------
drop table if exists movie_directors;
CREATE EXTERNAL TABLE IF NOT EXISTS movie_directors
(
    movie_id    BIGINT,
    director_id BIGINT
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar" = ",","quoteChar" = "\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/movie_directors/'
    TBLPROPERTIES ("skip.header.line.count" = "1");

--------------------------------------------------
-- movie_genres
--------------------------------------------------
drop table if exists movie_genres;
CREATE EXTERNAL TABLE IF NOT EXISTS movie_genres
(
    id       BIGINT,
    movie_id BIGINT,
    genre    STRING
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar" = ",","quoteChar" = "\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/movie_genres/'
    TBLPROPERTIES ("skip.header.line.count" = "1");

--------------------------------------------------
-- movie_editions
--------------------------------------------------
drop table if exists movie_editions;
CREATE EXTERNAL TABLE IF NOT EXISTS movie_editions
(
    id       BIGINT,
    movie_id BIGINT,
    edition  STRING
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar" = ",","quoteChar" = "\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/movie_editions/'
    TBLPROPERTIES ("skip.header.line.count" = "1");

--------------------------------------------------
-- release_dates
--------------------------------------------------
drop table if exists release_dates;
CREATE EXTERNAL TABLE IF NOT EXISTS release_dates
(
    id       BIGINT,
    movie_id BIGINT,
    year     INT,
    month    TINYINT,
    day      TINYINT,
    weekday  TINYINT
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar" = ",","quoteChar" = "\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/release_dates/'
    TBLPROPERTIES ("skip.header.line.count" = "1");

--------------------------------------------------
-- reviews
--------------------------------------------------
drop table if exists reviews;
CREATE EXTERNAL TABLE IF NOT EXISTS reviews
(
    id           BIGINT,
    review_uuid  STRING,
    movie_id     BIGINT,
    helpfulness  STRING,
    profile_name STRING,
    score        TINYINT,
    created_ts   BIGINT,
    summary      STRING,
    content      STRING,
    user_id      STRING
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar" = ",","quoteChar" = "\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/reviews/'
    TBLPROPERTIES ("skip.header.line.count" = "1");

drop table if exists movie_denormalization;
CREATE TABLE IF NOT EXISTS movie_denormalization
(
    movie_asin       STRING,
    movie_title      STRING,
    movie_score      FLOAT,
    movie_genre      STRING,
    release_year     INT,
    release_month    TINYINT,
    release_day      TINYINT,
    release_quarter  TINYINT,
    release_weekday  TINYINT,
    actor_id_list    STRING,
    director_id_list STRING,
    edition_list     STRING,
    actor_count      TINYINT,
    director_count   TINYINT,
    edition_count    TINYINT,
    review_count     INT
)
    STORED AS ORC;

drop table if exists actors_cooperation;
CREATE TABLE IF NOT EXISTS actors_cooperation
(
    actor1_id   INT,
    actor2_id   INT,
    actor1_name STRING,
    actor2_name STRING,
    movie_num   INT
)
    STORED AS ORC;

drop table if exists actor_director_cooperation;
CREATE TABLE IF NOT EXISTS actor_director_cooperation
(
    actor_id      INT,
    director_id   INT,
    actor_name    STRING,
    director_name STRING,
    movie_num     INT
)
    STORED AS ORC;

CREATE TABLE IF NOT EXISTS actor_stats
(
    actor_id    BIGINT,
    actor_name  STRING,
    movie_count INT,
    avg_score   FLOAT
)
    STORED AS ORC;


CREATE TABLE IF NOT EXISTS director_stats
(
    director_id   BIGINT,
    director_name STRING,
    movie_count   INT,
    avg_score     FLOAT
)
    STORED AS ORC;

CREATE TABLE IF NOT EXISTS movie_yearly_stats
(
    release_year INT,
    total_movies INT,
    avg_score    FLOAT
)
    STORED AS ORC;

CREATE TABLE IF NOT EXISTS movie_monthly_stats
(
    release_year  INT,
    release_month INT,
    total_movies  INT,
    avg_score     FLOAT
)
    STORED AS ORC;

CREATE TABLE IF NOT EXISTS movie_weekday_stats
(
    release_weekday TINYINT,
    total_movies    INT,
    avg_score       FLOAT
)
    STORED AS ORC;

CREATE TABLE IF NOT EXISTS movie_genre_stats
(
    genre         STRING,
    total_movies  INT,
    average_score FLOAT
)
    STORED AS ORC;


