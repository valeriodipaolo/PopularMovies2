package com.example.android.popularmovies.model;

/**
 * Created by abde3445 on 02/03/2017.
 */

public class Review {
    private String author;
    private String body;

    public Review(String author, String body) {
        this.author = author;
        this.body = body;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
