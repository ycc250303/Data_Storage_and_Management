package com.query.mysql.dto;

import lombok.Data;

@Data
public class MovieSearchDto {
    private String movieTitle;
    private String actorName;
    private String directorName;
    private String movieGenre;

}
