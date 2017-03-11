package com.example.android.popularmovies.utilities;

import android.util.Log;

import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.model.Review;
import com.example.android.popularmovies.model.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by v.dipaolo on 23/01/2017.
 */

//traslate json in java objects
public class JsonUtils {

    private static final String TAG = "JsonUtils";

    /**
     * Translate a json representing a collection of reviews in the list of the corresponding java objects.
     * Only review author and content are taken into account.
     *
     * @param json A json representing a collection of reviews
     * @return The list of java object representing the collection of reviews
     */
    public static List<Review> getReviewsArray(String json){

        if(json == null)
            return null;

        List<Review> reviews = null;

        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonResultsArray = jsonObject.getJSONArray("results");

            //number of reviews
            int resultNum = jsonResultsArray.length();
            reviews = new ArrayList<Review>();

            for (int i = 0; i < resultNum; i++) {
                JSONObject currReview = jsonResultsArray.getJSONObject(i);
                String author = currReview.getString("author");
                String content = currReview.getString("content");
                reviews.add(new Review(author, content));
            }
        }catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
            return  null;
        }

        return reviews;
    }

    /**
     * Translate a json representing a collection of trailers in the list of the corresponding java objects.
     * Only trailer name and key are taken into account.
     *
     * @param json A json representing a collection of trailers
     * @return The list of java object representing the collection of trailers
     */
    public static List<Trailer> getTrailersArray(String json){

        if(json == null)
            return null;

        List<Trailer> trailers = null;

        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonResultsArray = jsonObject.getJSONArray("results");

            //number of trailers in the first page
            int resultNum = jsonResultsArray.length();
            trailers = new ArrayList<Trailer>();

            for (int i = 0; i < resultNum; i++) {
                JSONObject currTrailer = jsonResultsArray.getJSONObject(i);
                String name = currTrailer.getString("name");
                String key = currTrailer.getString("key");
                trailers.add(new Trailer(name, key));
            }
        }catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
            return  null;
        }

        return trailers;
    }



    /**
     * Translate a json representing a collection of movies in the list of the corresponding java objects.
     * Only movie ids and movie poster paths are taken into account.
     *
     * @param json A json representing a collection of movies
     * @return The list of java object representing the collection of movies
     */
    public static List<Movie> getImagesArray(String json){

        if(json == null)
            return null;

        List<Movie> moviesPath = null;

        try {

            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonResultsArray = jsonObject.getJSONArray("results");

            //number of movies in the first page
            int resultNum = jsonResultsArray.length();
            moviesPath = new ArrayList<Movie>();

            for (int i = 0; i < resultNum; i++) {
                JSONObject currMovie = jsonResultsArray.getJSONObject(i);
                String currPosterPath = currMovie.getString("poster_path");
                long currId = currMovie.getLong("id");
                moviesPath.add(new Movie(currId, currPosterPath));
            }
        }catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
            return  null;
        }

        return moviesPath;
    }

    /**
     * Translate a json representing a movie in the corresponding java object
     *
     * @param json A json representing a movie
     * @return The java object representing the movie
     */
    public static Movie getMovieDetailsFromJson(String json){


        if(json == null)
            return null;

        Movie movie = null;

        try {

            JSONObject jsonObject = new JSONObject(json);
            long id = jsonObject.getLong("id");
            String path = jsonObject.getString("poster_path");
            String title = jsonObject.getString("title");
            String releaseDateRaw = jsonObject.getString("release_date");
            String releaseDate;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = sdf.parse(releaseDateRaw);
                releaseDate = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
                        .format(date);
            }catch (ParseException e){
                releaseDate = "";
            }
            double rating = jsonObject.getDouble("vote_average");
            String plot = jsonObject.getString("overview");

            //remove escape character
            if(path.startsWith("\\"))
                path = path.substring(1);

            movie = new Movie(id, path);

            movie.setTitle(title);
            movie.setRelease(releaseDate);
            movie.setRating(rating);
            movie.setPlot(plot);

        }catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
            return null;
        }

        return movie;
    }

    /**
     * return the total number of result pages
     *
     * @param json A json representing a collection of movies
     * @return The number of the result pages
     */
    public static int getPages(String json){

        if(json == null)
            return 0;

        try {
            JSONObject jsonObject = new JSONObject(json);
            int totalPages = jsonObject.getInt("total_pages");
            return totalPages;
        }catch (JSONException e){
            Log.e(TAG, "Error parsing JSON");
            e.printStackTrace();
            return 0;
        }
    }
}
