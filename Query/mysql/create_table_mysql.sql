create table data_warehouse.actors
(
    id   bigint unsigned auto_increment comment '自增主键' primary key,
    name varchar(256) null comment '演员姓名',
    constraint uk_actor_name UNIQUE (name)
) comment '演员表';
create table data_warehouse.directors
(
    id   bigint unsigned auto_increment comment '自增主键' primary key,
    name varchar(256) null comment '导演名称',
    constraint uk_director_name unique (name)
) comment '导演表';
create table data_warehouse.movie_actors
(
    movie_id bigint unsigned not null comment '电影 ID',
    actor_id bigint unsigned not null comment '演员 ID',
    primary key (movie_id, actor_id)
) comment '电影-演员关联表';
create index idx_actor_movie on data_warehouse.movie_actors (actor_id, movie_id);
create table data_warehouse.movie_directors
(
    movie_id    bigint unsigned not null comment '电影 ID',
    director_id bigint unsigned not null comment '导演 ID',
    primary key (movie_id, director_id)
) comment '电影-导演关联表';
create index idx_director_movie on data_warehouse.movie_directors (director_id, movie_id);
create table data_warehouse.movie_genres
(
    id       bigint unsigned auto_increment comment '自增主键' primary key,
    movie_id bigint unsigned not null comment '电影 ID',
    genre    varchar(64)     not null comment '电影风格',
    constraint uk_movie_genre unique (movie_id, genre)
) comment '电影类型/风格表';
create table data_warehouse.movie_editions
(
    id       bigint unsigned auto_increment comment '自增主键' primary key,
    movie_id bigint unsigned not null comment '电影 ID',
    edition  varchar(64)     not null comment '版本名称',
    constraint uk_movie_edition unique (movie_id, edition)
) comment '电影版本表';
create table data_warehouse.movies
(
    id          bigint unsigned auto_increment comment '自增主键' primary key,
    movie_asin  varchar(16)              not null comment '电影 ASIN（唯一业务主键）',
    movie_title varchar(512)             null comment '电影标题',
    score       float                    null comment '平均评分（0-5）',
    rated       varchar(16)              null comment '电影评级',
    language    varchar(256)             null comment '电影语言',
    review_num  int unsigned default '0' not null comment '评论数统计',
    constraint uk_movie_asin unique (movie_asin)
) comment '电影主表';
create index idx_movie_score on data_warehouse.movies (score);
create table data_warehouse.query_log
(
    log_id         int unsigned auto_increment comment '日志ID' primary key,
    query_time     datetime      null comment '用户查询时的时间',
    query_sql      varchar(1024) null comment '用户执行的查询语句',
    query_params   varchar(1024) null comment '用户查询时传递的参数',
    query_duration float         null comment '查询耗时',
    query_type     varchar(32)   null comment '查询类型',
    query_result   varchar(32)   null comment '查询结果（成功、失败等）',
    error_message  varchar(1024) null comment '查询失败时的错误信息',
    rows_returned  int unsigned  null comment '返回的行数'
) comment '查询日志表';
create table data_warehouse.release_dates
(
    id       bigint unsigned auto_increment comment '自增主键' primary key,
    movie_id bigint   not null comment '所属电影 ID',
    year     smallint null comment '上映年份',
    month    tinyint  null comment '上映月份（1-12）',
    day      tinyint  null comment '上映日（1-31）',
    weekday  tinyint  null comment '上映星期（0=周一）',
    constraint uk_release_movie unique (movie_id)
) comment '上映/发行日期表';
create index idx_release_movie on data_warehouse.release_dates (movie_id);
create index idx_release_year_month on data_warehouse.release_dates (year, month);
create table data_warehouse.reviews
(
    id           bigint unsigned auto_increment comment '自增主键' primary key,
    review_uuid  binary(16)       not null comment '评论 UUID（唯一）',
    movie_id     bigint unsigned  not null comment '电影 ID（外键 movies.id）',
    helpfulness  varchar(16)      null comment '有用票数，如 2/3',
    profile_name varchar(64)      null comment '评论者昵称',
    score        tinyint unsigned null comment '评分（1-5）',
    created_ts   bigint unsigned  null comment '评论时间戳',
    summary      varchar(128)     null comment '评论摘要',
    content      text             null comment '评论内容',
    user_id      varchar(32)      null comment '评论者用户ID',
    constraint uk_review_uuid unique (review_uuid)
) comment '电影评论表';
create index idx_movie_score on data_warehouse.reviews (movie_id, score);
create index idx_movie_time on data_warehouse.reviews (movie_id, created_ts);
create index idx_user on data_warehouse.reviews (user_id);
create table query_log
(
    log_id         int unsigned not null auto_increment primary key comment '日志ID',
    query_time     datetime comment '用户查询时的时间',
    query_sql      varchar(1024) comment '用户执行的查询语句',
    query_params   varchar(1024) comment '用户查询时传递的参数',
    query_duration float comment '查询耗时',
    query_type     varchar(128) comment '查询类型',
    query_result   varchar(32) comment '查询结果（成功、失败等）',
    error_message  varchar(1024) comment '查询失败时的错误信息',
    rows_returned  int unsigned comment '返回的行数'
) COMMENT '查询日志表';
-- 优化后的宽表
DROP TABLE IF EXISTS movie_denormalization;
CREATE TABLE movie_denormalization
(
    id               INT UNSIGNED  NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    movie_asin       VARCHAR(20)   NOT NULL COMMENT '电影ASIN (修正为字符串)',
    movie_title      VARCHAR(512)  NOT NULL COMMENT '电影标题',
    movie_score      FLOAT         NULL COMMENT '评分',
    movie_genre      VARCHAR(256)  NULL COMMENT '电影风格',
    release_year     SMALLINT      COMMENT '年份',
    release_month    TINYINT       NULL,
    release_day      TINYINT       NULL,
    release_quarter  TINYINT       NULL,
    release_weekday  TINYINT       NULL,
    -- 冗余姓名列表，实现零 JOIN
    actor_names      TEXT          NULL COMMENT '演员姓名列表 (逗号分隔)',
    director_names   TEXT          NULL COMMENT '导演姓名列表',
    -- 使用 JSON 存储 ID 列表，支持更精准的查询
    actor_id_list    JSON          NULL COMMENT '演员ID数组',
    director_id_list JSON          NULL COMMENT '导演ID数组',
    edition_list     TEXT          NULL COMMENT '电影版本列表',
    actor_count      TINYINT       NULL,
    director_count   TINYINT       NULL,
    edition_count    TINYINT       NULL,
    review_count     INT UNSIGNED  NULL,

    PRIMARY KEY (id),
    INDEX idx_year (release_year),
    INDEX idx_month (release_month),
    INDEX idx_score (movie_score)
)
    ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='电影信息全宽表';

create table actors_cooperation
(
    id          int unsigned not null auto_increment primary key comment '自增主键',
    actor1_id   int unsigned not null comment '演员1 ID',
    actor2_id   int unsigned not null comment '演员2 ID',
    actor1_name varchar(256) not null comment '演员1名称',
    actor2_name varchar(256) not null comment '演员2名称',
    movie_num   int unsigned not null comment '合作电影数'
) comment '演员合作表';
create table actor_director_cooperation
(
    id            int unsigned not null auto_increment primary key comment '自增主键',
    actor_id      int unsigned not null comment '演员 ID',
    director_id   int unsigned not null comment '导演 ID',
    actor_name    varchar(256) not null comment '演员名称',
    director_name varchar(256) not null comment '导演名称',
    movie_num     int unsigned not null comment '合作电影数'
) comment '演员-导演合作表';
CREATE TABLE actor_stats
(
    actor_id    BIGINT UNSIGNED NOT NULL COMMENT '演员 ID',
    actor_name  VARCHAR(256)    NOT NULL COMMENT '演员名称',
    movie_count INT UNSIGNED    NOT NULL COMMENT '参演电影数',
    avg_score   FLOAT           NULL COMMENT '参演电影平均评分',
    PRIMARY KEY (actor_id)
) COMMENT '演员维度电影统计表';
CREATE TABLE director_stats
(
    director_id   BIGINT UNSIGNED NOT NULL COMMENT '导演 ID',
    director_name VARCHAR(256)    NOT NULL COMMENT '导演名称',
    movie_count   INT UNSIGNED    NOT NULL COMMENT '导演电影数',
    avg_score     FLOAT           NULL COMMENT '导演电影平均评分',
    PRIMARY KEY (director_id)
) COMMENT '导演维度电影统计表';
CREATE TABLE movie_yearly_stats
(
    release_year INT COMMENT '上映年份',
    total_movies INT   NOT NULL COMMENT '上映电影数',
    avg_score    FLOAT NOT NULL COMMENT '平均评分',
    PRIMARY KEY (release_year, total_movies)
) COMMENT '电影年度统计表';
CREATE TABLE movie_monthly_stats
(
    release_year  INT COMMENT '上映年份',
    release_month INT COMMENT '上映月份',
    total_movies  INT   NOT NULL COMMENT '上映电影数',
    avg_score     FLOAT NOT NULL COMMENT '平均评分',
    PRIMARY KEY (release_year, release_month, total_movies)
) COMMENT '电影年度统计表';
CREATE TABLE movie_weekday_stats
(
    release_weekday TINYINT NOT NULL COMMENT '上映星期（0=周一）',
    total_movies    INT     NOT NULL COMMENT '新增电影数',
    avg_score       FLOAT   NULL COMMENT '平均评分',
    PRIMARY KEY (release_weekday, total_movies)
) COMMENT '电影星期统计表';
CREATE TABLE movie_genre_stats
(
    genre         varchar(64) not null comment '电影风格',
    total_movies  int         not null comment '电影数',
    average_score float       not null comment '平均评分'
) comment '电影风格统计表';

LOAD DATA INFILE '/var/lib/mysql-files/hive/actors/actors.csv'
    INTO TABLE actors
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 ROWS;

drop table if exists directors;
LOAD DATA INFILE '/var/lib/mysql-files/hive/directors/directors.csv'
    INTO TABLE directors
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 ROWS;

LOAD DATA INFILE '/var/lib/mysql-files/hive/movie_actors/movie_actors.csv'
    INTO TABLE movie_actors
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 ROWS;

LOAD DATA INFILE '/var/lib/mysql-files/hive/movie_directors/movie_directors.csv'
    INTO TABLE movie_directors
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 ROWS;

LOAD DATA INFILE '/var/lib/mysql-files/hive/movie_editions/movie_editions.csv'
    INTO TABLE movie_editions
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 ROWS;

LOAD DATA INFILE '/var/lib/mysql-files/hive/movie_genres/movie_genres.csv'
    INTO TABLE movie_genres
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 ROWS;

LOAD DATA INFILE '/var/lib/mysql-files/hive/movies/movies.csv'
    INTO TABLE movies
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 ROWS;

LOAD DATA INFILE '/var/lib/mysql-files/hive/release_dates/release_dates.csv'
    INTO TABLE release_dates
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 ROWS;

LOAD DATA INFILE '/var/lib/mysql-files/hive/reviews/reviews.csv'
    INTO TABLE reviews
    CHARACTER SET latin1
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 ROWS;
