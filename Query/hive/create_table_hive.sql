--------------------------------------------------
-- ODS 外部表（CSV / TEXTFILE，统一 _ext 后缀）
--------------------------------------------------

DROP TABLE IF EXISTS actors_ext;
CREATE EXTERNAL TABLE actors_ext (
                                     id BIGINT,
                                     name STRING
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar"=",","quoteChar"="\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/actors/'
    TBLPROPERTIES ("skip.header.line.count"="1");


DROP TABLE IF EXISTS directors_ext;
CREATE EXTERNAL TABLE directors_ext (
                                        id BIGINT,
                                        name STRING
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar"=",","quoteChar"="\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/directors/'
    TBLPROPERTIES ("skip.header.line.count"="1");


DROP TABLE IF EXISTS movies_ext;
CREATE EXTERNAL TABLE movies_ext (
                                     id BIGINT,
                                     movie_asin STRING,
                                     movie_title STRING,
                                     score FLOAT,
                                     rated STRING,
                                     language STRING,
                                     review_num INT
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar"=",","quoteChar"="\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/movies/'
    TBLPROPERTIES ("skip.header.line.count"="1");


DROP TABLE IF EXISTS movie_actors_ext;
CREATE EXTERNAL TABLE movie_actors_ext (
                                           movie_id BIGINT,
                                           actor_id BIGINT
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar"=",","quoteChar"="\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/movie_actors/'
    TBLPROPERTIES ("skip.header.line.count"="1");


DROP TABLE IF EXISTS movie_directors_ext;
CREATE EXTERNAL TABLE movie_directors_ext (
                                              movie_id BIGINT,
                                              director_id BIGINT
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar"=",","quoteChar"="\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/movie_directors/'
    TBLPROPERTIES ("skip.header.line.count"="1");


DROP TABLE IF EXISTS movie_genres_ext;
CREATE EXTERNAL TABLE movie_genres_ext (
                                           id BIGINT,
                                           movie_id BIGINT,
                                           genre STRING
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar"=",","quoteChar"="\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/movie_genres/'
    TBLPROPERTIES ("skip.header.line.count"="1");


DROP TABLE IF EXISTS movie_editions_ext;
CREATE EXTERNAL TABLE movie_editions_ext (
                                             id BIGINT,
                                             movie_id BIGINT,
                                             edition STRING
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar"=",","quoteChar"="\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/movie_editions/'
    TBLPROPERTIES ("skip.header.line.count"="1");


DROP TABLE IF EXISTS release_dates_ext;
CREATE EXTERNAL TABLE release_dates_ext (
                                            id BIGINT,
                                            movie_id BIGINT,
                                            year INT,
                                            month TINYINT,
                                            day TINYINT,
                                            weekday TINYINT
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar"=",","quoteChar"="\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/release_dates/'
    TBLPROPERTIES ("skip.header.line.count"="1");


DROP TABLE IF EXISTS reviews_ext;
CREATE EXTERNAL TABLE reviews_ext (
                                      id BIGINT,
                                      review_uuid STRING,
                                      movie_id BIGINT,
                                      helpfulness STRING,
                                      profile_name STRING,
                                      score TINYINT,
                                      created_ts BIGINT,
                                      summary STRING,
                                      content STRING,
                                      user_id STRING
)
    ROW FORMAT SERDE 'org.apache.hadoop.hive.serde2.OpenCSVSerde'
        WITH SERDEPROPERTIES ("separatorChar"=",","quoteChar"="\"")
    STORED AS TEXTFILE
    LOCATION 'file:///opt/hive/external/reviews/'
    TBLPROPERTIES ("skip.header.line.count"="1");

--------------------------------------------------
-- DWD 内部表（ORC，名称保持原名）
--------------------------------------------------

DROP TABLE IF EXISTS actors;
CREATE TABLE actors (
                        id BIGINT,
                        name STRING
)
    STORED AS ORC
    TBLPROPERTIES ('orc.compress'='SNAPPY');
INSERT OVERWRITE TABLE actors SELECT * FROM actors_ext;


DROP TABLE IF EXISTS directors;
CREATE TABLE directors (
                           id BIGINT,
                           name STRING
)
    STORED AS ORC
    TBLPROPERTIES ('orc.compress'='SNAPPY');
INSERT OVERWRITE TABLE directors SELECT * FROM directors_ext;


DROP TABLE IF EXISTS movies;
CREATE TABLE movies (
                        id BIGINT,
                        movie_asin STRING,
                        movie_title STRING,
                        score FLOAT,
                        rated STRING,
                        language STRING,
                        review_num INT
)
    STORED AS ORC
    TBLPROPERTIES ('orc.compress'='SNAPPY');
INSERT OVERWRITE TABLE movies SELECT * FROM movies_ext;


DROP TABLE IF EXISTS movie_actors;
CREATE TABLE movie_actors (
                              movie_id BIGINT,
                              actor_id BIGINT
)
    STORED AS ORC
    TBLPROPERTIES ('orc.compress'='SNAPPY');
INSERT OVERWRITE TABLE movie_actors SELECT * FROM movie_actors_ext;


DROP TABLE IF EXISTS movie_directors;
CREATE TABLE movie_directors (
                                 movie_id BIGINT,
                                 director_id BIGINT
)
    STORED AS ORC
    TBLPROPERTIES ('orc.compress'='SNAPPY');
INSERT OVERWRITE TABLE movie_directors SELECT * FROM movie_directors_ext;


DROP TABLE IF EXISTS movie_genres;
CREATE TABLE movie_genres (
                              id BIGINT,
                              movie_id BIGINT,
                              genre STRING
)
    STORED AS ORC
    TBLPROPERTIES ('orc.compress'='SNAPPY');
INSERT OVERWRITE TABLE movie_genres SELECT * FROM movie_genres_ext;


DROP TABLE IF EXISTS movie_editions;
CREATE TABLE movie_editions (
                                id BIGINT,
                                movie_id BIGINT,
                                edition STRING
)
    STORED AS ORC
    TBLPROPERTIES ('orc.compress'='SNAPPY');
INSERT OVERWRITE TABLE movie_editions SELECT * FROM movie_editions_ext;


DROP TABLE IF EXISTS release_dates;
CREATE TABLE release_dates (
                               id BIGINT,
                               movie_id BIGINT,
                               year INT,
                               month TINYINT,
                               day TINYINT,
                               weekday TINYINT
)
    STORED AS ORC
    TBLPROPERTIES ('orc.compress'='SNAPPY');
INSERT OVERWRITE TABLE release_dates SELECT * FROM release_dates_ext;


SET hive.stats.autogather=false;
SET hive.stats.column.autogather=false;
DROP TABLE IF EXISTS reviews;
CREATE TABLE reviews (
                         id BIGINT,
                         review_uuid STRING,
                         movie_id BIGINT,
                         helpfulness STRING,
                         profile_name STRING,
                         score TINYINT,
                         created_ts BIGINT,
                         summary STRING,
                         content STRING,
                         user_id STRING
)
    STORED AS ORC
    TBLPROPERTIES ('orc.compress'='SNAPPY');
INSERT OVERWRITE TABLE reviews SELECT * FROM reviews_ext;

drop table if exists movie_denormalization;
CREATE TABLE IF NOT EXISTS movie_denormalization
(
    movie_id         BIGINT,           -- 内部ID
    movie_asin       STRING,           -- Amazon ASIN
    movie_title      STRING,           -- 标题
    movie_score      FLOAT,            -- 评分
    movie_genres     ARRAY<STRING>,    -- 类型列表（集合类型）
    release_date     DATE,             -- 发行日期
    release_month    TINYINT,
    release_day      TINYINT,
    release_quarter  TINYINT,
    release_weekday  TINYINT,
    actor_names      ARRAY<STRING>,    -- 演员姓名列表（冗余姓名，避免JOIN）
    director_names   ARRAY<STRING>,    -- 导演姓名列表（冗余姓名，避免JOIN）
    edition_list     ARRAY<STRING>,    -- 版本列表
    actor_count      SMALLINT,
    director_count   SMALLINT,
    edition_count    SMALLINT,
    review_count     INT,
    search_text      STRING            -- 搜索索引列（包含标题、演员、导演）
)
PARTITIONED BY (release_year INT)      -- 按年份分区
STORED AS ORC                          -- 列式存储
TBLPROPERTIES ('orc.compress'='SNAPPY');

drop table if exists actors_cooperation;
CREATE TABLE IF NOT EXISTS actors_cooperation
(
    actor1_id   BIGINT,
    actor2_id   BIGINT,
    actor1_name STRING,
    actor2_name STRING,
    movie_num   INT
)
CLUSTERED BY (actor1_id) INTO 8 BUCKETS -- 分桶优化
STORED AS ORC;

drop table if exists actor_director_cooperation;
CREATE TABLE IF NOT EXISTS actor_director_cooperation
(
    actor_id      BIGINT,
    director_id   BIGINT,
    actor_name    STRING,
    director_name STRING,
    movie_num     INT
)
CLUSTERED BY (actor_id) INTO 8 BUCKETS
STORED AS ORC;

drop table if exists actor_stats;
CREATE TABLE IF NOT EXISTS actor_stats
(
    actor_id    BIGINT,
    actor_name  STRING,
    movie_count INT,
    avg_score   FLOAT
)
STORED AS ORC;

drop table if exists director_stats;
CREATE TABLE IF NOT EXISTS director_stats
(
    director_id   BIGINT,
    director_name STRING,
    movie_count   INT,
    avg_score     FLOAT
)
STORED AS ORC;

drop table if exists movie_yearly_stats;
CREATE TABLE IF NOT EXISTS movie_yearly_stats
(
    release_year INT,
    total_movies INT,
    avg_score    FLOAT
)
STORED AS ORC;

drop table if exists movie_monthly_stats;
CREATE TABLE IF NOT EXISTS movie_monthly_stats
(
    release_year  INT,
    release_month INT,
    total_movies  INT,
    avg_score     FLOAT
)
STORED AS ORC;

drop table if exists movie_weekday_stats;
CREATE TABLE IF NOT EXISTS movie_weekday_stats
(
    release_weekday TINYINT,
    total_movies    INT,
    avg_score       FLOAT
)
STORED AS ORC;

drop table if exists movie_genre_stats;
CREATE TABLE IF NOT EXISTS movie_genre_stats
(
    genre         STRING,
    total_movies  INT,
    average_score FLOAT
)
STORED AS ORC;