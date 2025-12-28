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