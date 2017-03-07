package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.model.Review;

import java.util.List;

/**
 * Created by v.dipaolo on 23/01/2017.
 */

public class ReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final String TAG = "ReviewAdapter";

    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews){
        this.reviews = reviews;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //get context
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View itemView = inflater.inflate(R.layout.review_item, parent, false);
        return new ReviewAdapter.ReviewViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof ReviewViewHolder) {
            ReviewViewHolder viewHolder = (ReviewViewHolder)holder;
            viewHolder.bindData(position);
        }
    }

    @Override
    public int getItemCount() {

        if(reviews == null)
            return 0;
        else
            return reviews.size();
    }

    //add new items to the list
    public void addReviews(List<Review> reviewsLoaded){

        if(reviewsLoaded==null)
            return;

        reviews.addAll(reviewsLoaded);
        notifyDataSetChanged();
    }

    //view holder of data items. Represents reviews
    public class ReviewViewHolder extends RecyclerView.ViewHolder{

        private TextView author;
        private TextView content;

        public ReviewViewHolder(View itemView) {
            super(itemView);
            author = (TextView)itemView.findViewById(R.id.review_author);
            content = (TextView)itemView.findViewById(R.id.review_body);
        }

        public void bindData(int position){
            Review review = reviews.get(position);
            author.setText(review.getAuthor());
            content.setText(review.getBody());
        }
    }

}
