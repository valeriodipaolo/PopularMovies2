package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.support.v4.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.android.popularmovies.data.FavoriteContract;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.model.Review;
import com.example.android.popularmovies.model.Trailer;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DetailActivity extends AppCompatActivity implements TrailerAdapter.TrailersItemClickListener {

    private static final String TAG = "DetailActivity";
    private final static int VISIBLE_THRESHOLD = 5;
    private final static String YOUTUBE_BASE_URI = "https://www.youtube.com/watch";

    private View dataView;
    private TextView error;
    private ProgressBar progressBar;
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter adapterReviews;


    private long movieId = -1;
    private boolean isLoadingReviews = false;
    private int pageReviews = 1;
    private int totalPagesReviews;

    private boolean sharing = false;
    private Intent shareIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //data, progress bar and error
        dataView = findViewById(R.id.detail_data);
        error = (TextView)findViewById(R.id.detail_error);
        progressBar = (ProgressBar)findViewById(R.id.detail_progress);

        Intent intent = getIntent();

        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT))
            movieId = getIntent().getLongExtra(Intent.EXTRA_TEXT, -1);

    }

    @Override
    protected void onStart() {
        super.onStart();

        //for shareActionProvider to be not null the RetrieveDetailAsyncTask must be done
        //after onCreateOptionsMenu
        if(movieId!=-1){
            RetrieveDetailAsyncTask task = new RetrieveDetailAsyncTask();
            task.execute(movieId);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu,menu);

        MenuItem itemShare = menu.findItem(R.id.action_share);
        itemShare.setVisible(false);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if(shareIntent==null)
            return true;

        MenuItem itemShare = menu.findItem(R.id.action_share);
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(itemShare);
        shareActionProvider.setShareIntent(shareIntent);
        itemShare.setVisible(true);
        return true;
    }

    private void showData(){
        dataView.setVisibility(View.VISIBLE);
        error.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar(){
        dataView.setVisibility(View.INVISIBLE);
        error.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showError(){
        dataView.setVisibility(View.INVISIBLE);
        error.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(String key) {
        Uri uri = Uri.parse(YOUTUBE_BASE_URI).buildUpon().appendQueryParameter("v",key).build();
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        String title = getResources().getString(R.string.chooser_title);
        Intent chooser = Intent.createChooser(intent, title);
        startActivity(chooser);
    }

    //retrieve a movie details
    private class RetrieveDetailAsyncTask extends AsyncTask<Long,Void, Movie>{

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

        @Override
        protected Movie doInBackground(Long... params) {
            long id = params[0];
            Movie movie = null;
            Cursor cursor = null;
            boolean favorite = false;

            try{
                cursor = getContentResolver().query(FavoriteContract.FavoriteEntry.buildFavoriteUriWithId(id),
                        null,
                        null,
                        null,
                        null);
                favorite = cursor.moveToNext();

                //movie detail
                movie = NetworkUtils.getMovieDetailById(id);

                //trailers
                List<Trailer> trailers = NetworkUtils.getTrailersByMovieId(id);

                //reviews
                Pair<Integer, List<Review>> reviews = NetworkUtils.getReviewsAndPagesByMovieId(id);
                totalPagesReviews = reviews==null?0:reviews.first;

                movie.setFavorite(favorite);
                movie.setTrailers(trailers);
                movie.setReviews(reviews==null?null:reviews.second);


                //sharing
                if(trailers!=null && trailers.size()>0){
                    String key = trailers.get(0).getKey();
                    String url = YOUTUBE_BASE_URI + "?v=" + key;

                    shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, url);

                    sharing = true;

                }

            }catch (Exception e){
                Log.e(TAG,"Error while retrieving movie details");
                e.printStackTrace();
                movie = null;
            }

            //offline
            if(movie==null && favorite){
                String title = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_TITLE));
                String poster = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_POSTER));
                String release = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_RELEASE));
                String plot = cursor.getString(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_PLOT));
                double rating = cursor.getDouble(cursor.getColumnIndex(FavoriteContract.FavoriteEntry.COLUMN_REVIEW));
                cursor.close();

                movie = new Movie(id, poster, title, rating, release, plot, true);
            }

            return movie;
        }

        @Override
        protected void onPostExecute(final Movie movie) {
            super.onPostExecute(movie);

            if(movie != null) {

                movieId = movie.getId();

                TextView titleView = (TextView) findViewById(R.id.detail_title);
                ImageView imageView = (ImageView) findViewById(R.id.detail_image);
                TextView ratingView = (TextView) findViewById(R.id.detail_rating);
                TextView releaseView = (TextView) findViewById(R.id.detail_release);
                TextView plotView = (TextView) findViewById(R.id.detail_plot);

                titleView.setText(movie.getTitle());
                Picasso.with(DetailActivity.this).load(NetworkUtils.BASE_POSTER_URL + movie.getPosterPath()).into(imageView);
                ratingView.setText(String.format("%.1f/10", movie.getRating()));
                releaseView.setText(movie.getRelease());
                plotView.setText(movie.getPlot());

                //favourite
                //TODO se si verificano errori nel toogle o se si fa troppo velocemente?
                ToggleButton favoriteToggle = (ToggleButton) findViewById(R.id.detail_mark_favourite);

                if(movie.isFavorite() && !favoriteToggle.isChecked())
                    favoriteToggle.toggle();

                favoriteToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {

                        if (isChecked)
                                movie.setFavorite(true);
                        else
                            movie.setFavorite(false);

                        new FavoriteAsyncTask().execute(movie);
                    }
                });

                //recycler views
                //stage 2
                //trailer
                RecyclerView recyclerViewTrailers = (RecyclerView) findViewById(R.id.detail_trailers);

                LinearLayoutManager layoutManagerTrailer =
                        new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.VERTICAL, false);
                recyclerViewTrailers.setLayoutManager(layoutManagerTrailer);
                recyclerViewTrailers.setHasFixedSize(true);

                TrailerAdapter adapterTrailer = new TrailerAdapter(movie.getTrailers(), DetailActivity.this);
                recyclerViewTrailers.setAdapter(adapterTrailer);

                //reviews
                recyclerViewReviews = (RecyclerView) findViewById(R.id.detail_reviews);

                final LinearLayoutManager layoutManagerReview =
                        new LinearLayoutManager(DetailActivity.this, LinearLayoutManager.VERTICAL, false);
                recyclerViewReviews.setLayoutManager(layoutManagerReview);
                recyclerViewReviews.setHasFixedSize(false);

                adapterReviews = new ReviewAdapter(movie.getReviews());
                recyclerViewReviews.setAdapter(adapterReviews);

                //load more reviews
                recyclerViewReviews.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        int totalItemCount = layoutManagerReview.getItemCount();
                        int lastVisibleItem = layoutManagerReview.findLastVisibleItemPosition();

                        if (!isLoadingReviews && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD) &&
                                pageReviews < totalPagesReviews) {
                            isLoadingReviews = true;
                            new Handler().post(new Runnable(){
                                public void run(){
                                    new DetailActivity.LoaderAdapterAsyncTask().execute();  //set a delayed load of data
                                }
                            });

                        }
                    }
                });

                if(sharing)
                    invalidateOptionsMenu();

                showData();
            }
            else
                showError();
        }
    }

    //load more reviews
    private class FavoriteAsyncTask extends AsyncTask<Movie, Void, Void> {

        @Override
        protected Void doInBackground(Movie... params) {

            Movie movie = params[0];
            try {
                if (movie.isFavorite()) {

                    ContentValues values = new ContentValues();
                    values.put(FavoriteContract.FavoriteEntry._ID, movie.getId());
                    values.put(FavoriteContract.FavoriteEntry.COLUMN_PLOT, movie.getPlot());
                    values.put(FavoriteContract.FavoriteEntry.COLUMN_POSTER, movie.getPosterPath());
                    values.put(FavoriteContract.FavoriteEntry.COLUMN_RELEASE, movie.getRelease());
                    values.put(FavoriteContract.FavoriteEntry.COLUMN_REVIEW, movie.getRating());
                    values.put(FavoriteContract.FavoriteEntry.COLUMN_TITLE, movie.getTitle());
                    DetailActivity.this.getContentResolver()
                            .insert(FavoriteContract.FavoriteEntry.CONTENT_URI, values);
                } else {
                    DetailActivity.this.getContentResolver().delete(
                            FavoriteContract.FavoriteEntry.buildFavoriteUriWithId(movieId),
                            null,
                            null);
                }
            } catch (Exception e) {
                Log.e(TAG,"Error update favorite");
                e.printStackTrace();
            }

            return null;
        }
    }

    //load more reviews
    private class LoaderAdapterAsyncTask extends AsyncTask<Void, Void, Void> {

        private List<Review> reviews;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                pageReviews++;
                reviews = NetworkUtils.getReviewsByMovieId(movieId, pageReviews);
            } catch (Exception e) {
                Log.e(TAG,"Error while loading more reviews");
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if(reviews==null) {
                isLoadingReviews = false;
                return;
            }

            try {
                isLoadingReviews = false;
                adapterReviews.addReviews(reviews);
            } catch (Exception e) {
                Log.e(TAG,"Error while loading more movies");
                e.printStackTrace();
            }
        }
    }

}
