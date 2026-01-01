package com.example.moviesrecommendation;

import java.io.Serializable;

public class Movie implements Serializable {
    private int id;
    private String title;
    private String name; // For TV Series
    private String overview;
    private String poster_path;
    private double vote_average;
    private String release_date;
    private String first_air_date; // For TV Series
    private String original_language;
    private String media_type; // "movie" or "tv"

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title != null ? title : name;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return poster_path;
    }

    public double getVoteAverage() {
        return vote_average;
    }

    public String getReleaseDate() {
        return release_date != null ? release_date : first_air_date;
    }

    public String getOriginalLanguage() {
        return original_language;
    }
    
    public String getMediaType() {
        return media_type;
    }
    
    public void setMediaType(String mediaType) {
        this.media_type = mediaType;
    }
}