package com.query.hive.dto;

import lombok.Data;

@Data
public class MovieDetailDto {
    private String movieAsin;
    private String movieTitle;
    private float movieScore;
    private String actors;
    private String directors;
    private String movieGenre;
    private String date;
    private String edition;
}
