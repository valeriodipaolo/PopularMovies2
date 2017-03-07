package com.example.android.popularmovies.model;

import java.util.List;

/**
 * Created by v.dipaolo on 23/01/2017.
 */

public class Movie{

    private long id;
    private String posterPath;
    private String title;
    private double rating;
    private String release;
    private String plot;
    private boolean favorite;
    private List<Trailer> trailers;
    private List<Review> reviews;

    public Movie(long id, String path){
        this.id = id;
        this.posterPath = path;
    }

    public Movie(long id, String posterPath, String title, double rating, String release, String plot, boolean favorite) {
        this.id = id;
        this.posterPath = posterPath;
        this.title = title;
        this.rating = rating;
        this.release = release;
        this.plot = plot;
        this.favorite = favorite;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public List<Trailer> getTrailers() {
        return trailers;
    }

    public void setTrailers(List<Trailer> trailers) {
        this.trailers = trailers;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }
}
