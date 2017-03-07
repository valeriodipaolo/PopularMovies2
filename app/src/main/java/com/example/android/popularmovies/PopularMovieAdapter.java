package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by v.dipaolo on 23/01/2017.
 */

public class PopularMovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final String TAG = "PopularMovieAdapter";

    private List<Movie> movies;
    private PopularMovieItemClickListener listener;

    public PopularMovieAdapter(List<Movie> movies, PopularMovieItemClickListener listener){
        this.movies = movies;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //get context
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View itemView = inflater.inflate(R.layout.item, parent, false);
        return new PopularMovieViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof PopularMovieViewHolder) {
            PopularMovieViewHolder viewHolder = (PopularMovieViewHolder)holder;
            viewHolder.bindData(position);
        }
    }

    @Override
    public int getItemCount() {

        if(movies == null)
            return 0;
        else
            return movies.size();
    }

    //change the list of items
    public void setMoviesArray(List<Movie> movies){

        //remove all items
        this.movies.clear();
        notifyDataSetChanged();

        //load new items
        this.movies = movies;
        notifyDataSetChanged();
    }

    //add new items to the list
    public void addMovies(List<Movie> moviesLoaded){

        if(moviesLoaded==null)
            return;

        movies.addAll(moviesLoaded);
        notifyDataSetChanged();
    }

    //view holder of data items. Represents movies
    public class PopularMovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView imageView;

        public PopularMovieViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.poster_imageView);

            itemView.setOnClickListener(this);
        }

        public void bindData(int position){
            Picasso.with(imageView.getContext()).load(NetworkUtils.BASE_POSTER_URL + movies.get(position).getPosterPath()).into(imageView);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            long id = movies.get(position).getId();
            listener.onClick(id);
        }
    }
}
