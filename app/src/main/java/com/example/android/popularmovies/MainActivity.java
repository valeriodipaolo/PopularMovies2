package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.popularmovies.data.FavoriteContract;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.utilities.NetworkUtils;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements PopularMovieItemClickListener, LoaderManager.LoaderCallbacks<Cursor>{

    private final static String TAG = "MainActivity";
    private final static int VISIBLE_THRESHOLD = 5;
    private final static int WIDTH_IMAGE = 185;
    public  final static String FAVORITE = "favorite";
    private  final static String SORTBY_KEY = "sortBy";
    private  final static String PAGE_KEY = "page";
    private  final static String POSITION_KEY = "position";
    private  final static String TOTAL_PAGES_KEY = "totalPages";

    private RecyclerView recyclerView;
    private PopularMovieAdapter adapter;
    private TextView error;
    private ProgressBar progressBar;

    private boolean isInit = true;
    private boolean isLoading = false;
    private String sortBy = NetworkUtils.SORT_BY_POPULARITY;
    private int page = 1;
    private int previousPage = 1;
    private int currentPosition = 1;
    private int totalPages;

    private static final int ID_FAVORITE_LOADER = 44;

    //load more pages
    private LoaderAdapterAsyncTask loadMore = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView)findViewById(R.id.poster_grid);

        //provide an indicative width, in pixels, to the FlexibleGridLayoutManager object,
        //so that it can determine the number of column to use, based on screen size
        float density = getResources().getDisplayMetrics().density;
        float dp = WIDTH_IMAGE*density;
        final GridLayoutManager layoutManager = new FlexibleGridLayoutManager(this, dp);
        recyclerView.setLayoutManager(layoutManager);

        //error textView e progressBar
        error = (TextView)findViewById(R.id.error_view);
        progressBar = (ProgressBar)findViewById(R.id.progress_view);

        //load more items
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();

                int totalItemCount = layoutManager.getItemCount();
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (sortBy!=FAVORITE && !isLoading && totalItemCount <= (lastVisibleItem + VISIBLE_THRESHOLD) &&
                        page < totalPages) {
                    isLoading = true;
                    new Handler().post(new Runnable(){
                        public void run(){
                            loadMore = new LoaderAdapterAsyncTask();
                            loadMore.execute();  //set a delayed load of data
                        }
                    });

                }
            }
        });

        if(savedInstanceState!=null &&
                savedInstanceState.containsKey(SORTBY_KEY) &&
                savedInstanceState.containsKey(PAGE_KEY) &&
                savedInstanceState.containsKey(POSITION_KEY) &&
                savedInstanceState.containsKey(TOTAL_PAGES_KEY)){
            sortBy = savedInstanceState.getString(SORTBY_KEY);
            previousPage = savedInstanceState.getInt(PAGE_KEY);
            currentPosition = savedInstanceState.getInt(POSITION_KEY);
            totalPages = savedInstanceState.getInt(TOTAL_PAGES_KEY);

        }
        //adapter
        if(sortBy==FAVORITE) {
            getSupportLoaderManager().initLoader(ID_FAVORITE_LOADER, null, this);
            isInit=false;
        }
        else
            new InitAdapterAsyncTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //no selection allowed while loading
        if(isInit || isLoading)
            return true;

        int id = item.getItemId();

        if(id==R.id.action_popular) {
            if (!sortBy.equals(NetworkUtils.SORT_BY_POPULARITY)) {
                sortBy = NetworkUtils.SORT_BY_POPULARITY;

                //stop loadMore asynchTask
                if(isLoading && loadMore!=null) {
                    loadMore.cancel(true);
                    loadMore=null;
                    isLoading=true;
                }
                new UpdateAdapterAsyncTask().execute(sortBy);
            }

            return true;
        }


        if(id==R.id.action_rated) {
            if (!sortBy.equals(NetworkUtils.SORT_BY_RANKING)) {
                sortBy = NetworkUtils.SORT_BY_RANKING;

                //stop loadMore asynchTask
                if(isLoading && loadMore!=null) {
                    loadMore.cancel(true);
                    loadMore=null;
                    isLoading=true;
                }
                new UpdateAdapterAsyncTask().execute(sortBy);
            }

            return true;
        }

        if(id==R.id.action_favorite) {
            if (!sortBy.equals(FAVORITE)) {
                sortBy = FAVORITE;

                //stop loadMore asynchTask
                if(isLoading && loadMore!=null) {
                    loadMore.cancel(true);
                    loadMore=null;
                    isLoading=true;
                }

                getSupportLoaderManager().initLoader(ID_FAVORITE_LOADER, null, this);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //when a film is selected, show DetailActivity
    @Override
    public void onClick(long id) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Intent.EXTRA_TEXT, id);
        startActivity(intent);
    }

    //show movie posters
    private void showData(){
        recyclerView.setVisibility(View.VISIBLE);
        error.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    //data loading
    private void showProgressBar(){
        recyclerView.setVisibility(View.INVISIBLE);
        error.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    //show error
    private void showError(){
        recyclerView.setVisibility(View.INVISIBLE);
        error.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(sortBy!=FAVORITE)
            return null;

        switch (id) {

            case ID_FAVORITE_LOADER:

                return new CursorLoader(this,
                        FavoriteContract.FavoriteEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        FavoriteContract.FavoriteEntry.COLUMN_TIMESTAMP);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(sortBy!=FAVORITE)
            return;

        recyclerView.setAdapter(new PopularMovieCursorAdapter(data, this));
        isLoading=false;
        showData();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if(!isInit) {
            int pageToRestore = page > previousPage ? page : previousPage;
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            int currentPos = currentPosition > 1 ?
                    currentPosition : layoutManager.findFirstCompletelyVisibleItemPosition();
            currentPos = currentPos == RecyclerView.NO_POSITION ?
                    layoutManager.findFirstVisibleItemPosition():currentPos;
            outState.putString(SORTBY_KEY, sortBy);
            outState.putInt(PAGE_KEY, pageToRestore);
            outState.putInt(POSITION_KEY, currentPos);
            outState.putInt(TOTAL_PAGES_KEY, totalPages);
        }
        super.onSaveInstanceState(outState);
    }

    //create adapter and complete the initialization
    private class InitAdapterAsyncTask extends AsyncTask<Void, Void, Void>{

        private List<Movie> movies;

        @Override
        protected void onPreExecute() {

            showProgressBar();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Pair<Integer, List<Movie>> pair = NetworkUtils.getSortedMoviesPosterAndPages(sortBy);
                totalPages = pair.first;
                movies = pair.second;
            }catch (Exception e){
                Log.e(TAG,"Error during initialization");
                e.printStackTrace();
                movies = null;
            }
                return  null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if(movies==null) {
                showError();
                isInit=false;
                return;
            }

            try {
                //create adapter and set it in recycler view
                adapter = new PopularMovieAdapter(movies, MainActivity.this);
                recyclerView.setAdapter(adapter);

                //change orientation. Restore position
                if(previousPage>page)
                    new LoaderAdapterAsyncTask().execute();
                else
                    showData();

            }catch (Exception e){
                Log.e(TAG,"Error during initialization");
                e.printStackTrace();
                showError();
            }finally{
                isInit=false;
            }
        }
    }

    //load more movies
    private class LoaderAdapterAsyncTask extends AsyncTask<Void, Void, Void> {

        private List<Movie> movies;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                page++;
                movies = NetworkUtils.getSortedMoviesPoster(sortBy, page);
            } catch (Exception e) {
                Log.e(TAG,"Error while loading more movies");
                e.printStackTrace();
                movies = null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(movies==null) {
                showError();
                isLoading = false;
                return;
            }

            try {
                adapter.addMovies(movies);
                isLoading = false;

                //change orientation. Restore position
                if(previousPage>1)
                    if(previousPage>page)
                        new LoaderAdapterAsyncTask().execute();
                    else {
                        previousPage=1;
                        GridLayoutManager layoutManager = (GridLayoutManager)recyclerView.getLayoutManager();
                        layoutManager.scrollToPositionWithOffset(currentPosition,0);
                        currentPosition=1;
                        showData();
                    }

            } catch (Exception e) {
                Log.e(TAG,"Error while loading more movies");
                e.printStackTrace();
                showError();
            }
        }
    }

    //manage the change of sorting criteria
    private class UpdateAdapterAsyncTask extends AsyncTask<String, Void, Void> {

        private List<Movie> movies;

        @Override
        protected void onPreExecute() {
            showProgressBar();
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                Pair<Integer, List<Movie>> pair = NetworkUtils.getSortedMoviesPosterAndPages(params[0]);
                totalPages = pair.first;
                movies = pair.second;
            } catch (Exception e) {
                Log.e(TAG,"Error while changing sorting criteria");
                e.printStackTrace();
                movies = null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if(movies==null) {
                showError();
                isLoading=false;
                page = 1;
                return;
            }

            try {
                adapter = new PopularMovieAdapter(movies, MainActivity.this);
                recyclerView.setAdapter(adapter);
                page = 1;
                showData();
            } catch (Exception e) {
                Log.e(TAG,"Error while changing sorting criteria");
                e.printStackTrace();
                showError();
            }finally{
                isLoading=false;
            }
        }
    }
}
