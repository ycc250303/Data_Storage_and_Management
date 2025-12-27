package com.query.mysql.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

@Data
public class MovieSearchDto {

    private String movieTitle;
    private String actorName;
    private String directorName;
    private String movieGenre;

    @Min(value = 0, message = "起始年份必须大于等于0")
    private int startYear;
    @Min(value = 0, message = "结束年份必须大于等于0")
    private int endYear;
    @Min(value = 1, message = "月份必须大于等于1")
    private int month;
    @Min(value = 0, message = "星期几必须大于等于0")
    private int weekday;
    @Min(value = 1, message = "日期必须大于等于1")
    private int day;
    @Min(value = 0, message = "最低分数必须大于等于0")
    @Max(value = 5, message = "最高分数必须小于等于5")
    private float minScore;
    @Min(value = 0, message = "最高分数必须大于等于0")
    @Max(value = 5, message = "最高分数必须小于等于5")
    private float maxScore;
    @Min(value = -1, message = "页码必须大于等于-1")
    @Max(value = 1000, message = "页码必须小于等于1000")
    private int page = -1;
    @Min(value = 1, message = "每页大小必须大于等于1")
    @Max(value = 100, message = "每页大小必须小于等于100")
    private int size = 20;
}
