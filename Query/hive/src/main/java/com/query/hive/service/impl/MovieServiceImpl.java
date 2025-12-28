package com.query.hive.service.impl;

import com.query.hive.dto.MovieDetailDto;
import com.query.hive.dto.MovieSearchDto;
import com.query.hive.mapper.MoviesMapper;
import com.query.hive.service.MovieService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hive电影服务实现类
 */
@Slf4j
@Service
public class MovieServiceImpl implements MovieService {

    @Autowired
    private MoviesMapper moviesMapper;

    @Override
    public List<Map<String, Object>> getMovieEditions(String movieTitle) {
        List<Map<String, Object>> results = moviesMapper.getMovieEditionsByMovieTitle(movieTitle);
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return results;
    }

    @Override
    public List<Map<String, Object>> getMovieEditionCount(String movieTitle) {
        List<Map<String, Object>> results = moviesMapper.getMovieEditionCountByMovieTitle(movieTitle);
        if (results == null || results.isEmpty()) {
            return List.of();
        }
        return results;
    }

    @Override
    public List<Map<String, Object>> getMovieCountByGenre(String movieGenre) {
        return moviesMapper.getMovieCountByGenre(movieGenre);
    }

    @Override
    public List<Map<String, Object>> getMoviesByGenreName(String movieGenre) {
        List<Map<String, Object>> results = moviesMapper.getMoviesByGenreName(movieGenre);
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        List<Map<String, Object>> movieList = new ArrayList<>();
        for (Map<String, Object> record : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("movieTitle", record.get("movie_title"));
            movieList.add(map);
        }
        return movieList;
    }

    @Override
    public List<MovieDetailDto> getMoviesByCombinedConditions(MovieSearchDto dto) {
        MovieSearchDto searchDto = new MovieSearchDto();
        // 复制属性并预处理小写（减少 SQL 负担）
        searchDto.setMovieTitle(dto.getMovieTitle() != null ? dto.getMovieTitle().toLowerCase() : null);
        searchDto.setActorName(dto.getActorName() != null ? dto.getActorName().toLowerCase() : null);
        searchDto.setDirectorName(dto.getDirectorName() != null ? dto.getDirectorName().toLowerCase() : null);
        searchDto.setMovieGenre(dto.getMovieGenre() != null ? dto.getMovieGenre().toLowerCase() : null);
        searchDto.setStartYear(dto.getStartYear());
        searchDto.setEndYear(dto.getEndYear());
        searchDto.setMonth(dto.getMonth());
        searchDto.setWeekday(dto.getWeekday());
        searchDto.setDay(dto.getDay());
        searchDto.setMinScore(dto.getMinScore());
        searchDto.setMaxScore(dto.getMaxScore());

        // 处理分页逻辑
        if (dto.getPage() == -1) {
            searchDto.setPage(0);
            searchDto.setSize(10000); // 默认限制 10000 条
        } else {
            int page = dto.getPage() < 0 ? 0 : dto.getPage();
            int size = dto.getSize() <= 0 ? 20 : dto.getSize();
            searchDto.setPage(page * size);
            searchDto.setSize(size);
        }

        // 2. 第一步：先查询符合条件的电影 ID 列表（轻量级查询）
        log.info("开始执行第一步：筛选电影ID。查询参数：{}", searchDto);
        List<Integer> movieIds = moviesMapper.selectMovieIdsByConditions(searchDto);
        log.info("第一步查询完成，获取到的电影ID数量：{}", movieIds != null ? movieIds.size() : 0);

        if (movieIds == null || movieIds.isEmpty()) {
            log.warn("未找到符合条件的电影ID，请检查查询条件或数据库中是否有匹配数据");
            return new ArrayList<>();
        }
        log.debug("电影ID列表：{}", movieIds);

        // 3. 第二步：分步查询各部分信息（避免大表JOIN）
        log.info("开始执行第二步：分步查询电影详细信息");

        // 3.1 查询电影基本信息（movie_asin, movie_title, score）
        List<Map<String, Object>> basicInfo = moviesMapper.selectMoviesBasicInfo(movieIds);
        log.info("查询电影基本信息完成，数量：{}", basicInfo != null ? basicInfo.size() : 0);
        log.info("电影基本信息:{}", basicInfo);

        if (basicInfo == null || basicInfo.isEmpty()) {
            log.error("警告：找到了ID列表但无法获取基本信息");
            return new ArrayList<>();
        }

        // 3.2 查询演员列表
        Map<Integer, String> actorsMap = new HashMap<>();
        try {
            log.info("开始查询演员列表...");
            List<Map<String, Object>> actorsList = moviesMapper.selectActorsByMovieIds(movieIds);
            actorsMap = convertListToMap(actorsList);
            log.info("查询演员信息完成，数量：{}", actorsMap.size());
            log.info("演员列表：{}", actorsMap);
        } catch (Exception e) {
            log.error("查询演员信息失败", e);
        }

        // 3.3 查询导演列表
        Map<Integer, String> directorsMap = new HashMap<>();
        try {
            log.info("开始查询导演列表...");
            List<Map<String, Object>> directorsList = moviesMapper.selectDirectorsByMovieIds(movieIds);
            directorsMap = convertListToMap(directorsList);
            log.info("查询导演信息完成，数量：{}", directorsMap.size());
            log.info("导演列表：{}", directorsMap);
        } catch (Exception e) {
            log.error("查询导演信息失败", e);
        }

        // 3.4 查询类型列表
        Map<Integer, String> genresMap = new HashMap<>();
        try {
            log.info("开始查询类型列表...");
            List<Map<String, Object>> genresList = moviesMapper.selectGenresByMovieIds(movieIds);
            genresMap = convertListToMap(genresList);
            log.info("查询类型信息完成，数量：{}", genresMap.size());
            log.info("类型列表：{}", genresMap);
        } catch (Exception e) {
            log.error("查询类型信息失败", e);
        }

        // 3.5 查询发行日期
        Map<Integer, String> datesMap = new HashMap<>();
        try {
            log.info("开始查询发行日期...");
            List<Map<String, Object>> datesList = moviesMapper.selectReleaseDatesByMovieIds(movieIds);
            datesMap = convertListToMap(datesList);
            log.info("查询发行日期完成，数量：{}", datesMap.size());
            log.info("发行日期列表：{}", datesMap);
        } catch (Exception e) {
            log.error("查询发行日期失败", e);
        }

        // 3.6 查询版本信息
        Map<Integer, String> editionsMap = new HashMap<>();
        try {
            log.info("开始查询版本信息...");
            List<Map<String, Object>> editionsList = moviesMapper.selectEditionsByMovieIds(movieIds);
            editionsMap = convertListToMap(editionsList);
            log.info("查询版本信息完成，数量：{}", editionsMap.size());
            log.info("版本列表：{}", editionsMap);
        } catch (Exception e) {
            log.error("查询版本信息失败", e);
        }

        // 4. 封装结果
        List<MovieDetailDto> movieDetailDtoList = new ArrayList<>();
        for (Map<String, Object> record : basicInfo) {
            Integer movieId = record.get("id") != null ? Integer.parseInt(record.get("id").toString()) : null;
            if (movieId == null)
                continue;

            MovieDetailDto movieDetail = new MovieDetailDto();
            movieDetail.setMovieAsin(record.get("movieasin") != null ? record.get("movieasin").toString() : "");
            movieDetail.setMovieTitle(record.get("movietitle") != null ? record.get("movietitle").toString() : "");
            movieDetail.setMovieScore(
                    record.get("moviescore") != null ? Float.parseFloat(record.get("moviescore").toString()) : 0.0f);

            // 从各个 Map 中获取关联信息
            movieDetail.setActors(actorsMap.getOrDefault(movieId, ""));
            movieDetail.setDirectors(directorsMap.getOrDefault(movieId, ""));
            movieDetail.setMovieGenre(genresMap.getOrDefault(movieId, ""));
            movieDetail.setDate(datesMap.getOrDefault(movieId, ""));
            movieDetail.setEdition(editionsMap.getOrDefault(movieId, ""));

            movieDetailDtoList.add(movieDetail);
        }
        log.info("查询结果封装完成，共 {} 条记录", movieDetailDtoList.size());

        return movieDetailDtoList;
    }

    /**
     * 将 List<Map<String, Object>> 转换为 Map<Integer, String>
     * MyBatis 返回的结果格式：[{key=123, value="actor1,actor2"}, ...]
     */
    private Map<Integer, String> convertListToMap(List<Map<String, Object>> list) {
        Map<Integer, String> resultMap = new HashMap<>();
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> item : list) {
                Object keyObj = item.get("key");
                Object valueObj = item.get("value");
                if (keyObj != null && valueObj != null) {
                    Integer key = Integer.parseInt(keyObj.toString());
                    String value = valueObj.toString();
                    resultMap.put(key, value);
                }
            }
        }
        return resultMap;
    }

    @Override
    public List<MovieDetailDto> searchMoviesByWideTable(MovieSearchDto dto) {
        log.info("开始执行宽表极速查询。参数：{}", dto);

        // 分页处理
        if (dto.getPage() == -1) {
            dto.setPage(0);
            dto.setSize(10000);
        } else {
            dto.setPage(dto.getPage() * dto.getSize());
        }

        // 一步到位，直接查询宽表
        List<Map<String, Object>> results = moviesMapper.selectMoviesFromWideTable(dto);

        List<MovieDetailDto> movieDetailDtoList = new ArrayList<>();
        if (results != null) {
            for (Map<String, Object> record : results) {
                MovieDetailDto movieDetail = new MovieDetailDto();
                movieDetail.setMovieAsin(record.get("movieasin") != null ? record.get("movieasin").toString() : "");
                movieDetail.setMovieTitle(record.get("movietitle") != null ? record.get("movietitle").toString() : "");
                movieDetail.setMovieScore(
                        record.get("moviescore") != null ? Float.parseFloat(record.get("moviescore").toString())
                                : 0.0f);
                movieDetail.setActors(record.get("actors") != null ? record.get("actors").toString() : "");
                movieDetail.setDirectors(record.get("directors") != null ? record.get("directors").toString() : "");
                movieDetail.setMovieGenre(record.get("moviegenre") != null ? record.get("moviegenre").toString() : "");
                movieDetail.setDate(record.get("release_date") != null ? record.get("release_date").toString() : "");
                movieDetail.setEdition(record.get("edition") != null ? record.get("edition").toString() : "");
                movieDetailDtoList.add(movieDetail);
            }
        }

        log.info("宽表查询完成，返回记录数：{}", movieDetailDtoList.size());
        return movieDetailDtoList;
    }
}
