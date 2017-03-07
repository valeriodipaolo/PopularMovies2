package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.model.Trailer;

import java.util.List;

/**
 * Created by v.dipaolo on 23/01/2017.
 */

public class TrailerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final String TAG = "TrailersAdapter";
    private List<Trailer> trailers;
    private TrailersItemClickListener listener;

    public TrailerAdapter(List<Trailer> trailers, TrailersItemClickListener listener){
        this.trailers = trailers;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //get context
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.trailer_item, parent, false);
        return new TrailerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof TrailerViewHolder) {
            TrailerViewHolder viewHolder = (TrailerViewHolder)holder;
            viewHolder.bindData(position);
        }
    }

    @Override
    public int getItemCount() {

        if(trailers == null)
            return 0;
        else
            return trailers.size();
    }


    //view holder of data items. Represents trailers
    public class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView textViewName;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            textViewName = (TextView)itemView.findViewById(R.id.trailer_name);

            itemView.setOnClickListener(this);
        }

        public void bindData(int position){
            textViewName.setText(trailers.get(position).getName());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            String key = trailers.get(position).getKey();
            listener.onClick(key);
        }
    }

    //manage click on data items
    //takes the key of the youtube video
    public interface TrailersItemClickListener{
        void onClick(String key);
    }
}
