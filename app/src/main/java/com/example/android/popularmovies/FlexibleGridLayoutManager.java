package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

/**
 * Created by abde3445 on 27/01/2017.
 */
public class FlexibleGridLayoutManager extends GridLayoutManager {

        private static final String TAG = "FlexGridLayoutManager";
        private float minItemWidth;
        private Context context;

        public FlexibleGridLayoutManager(Context context, float minItemWidth) {
            super(context, 1);
            this.minItemWidth = minItemWidth;
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler,
                                     RecyclerView.State state) {
            updateSpanCount();
            super.onLayoutChildren(recycler, state);
        }

        private void updateSpanCount() {
            int spanCount = Math.round(getWidth() / minItemWidth);
            if (spanCount < 1) {
                spanCount = 1;
            }
            this.setSpanCount(spanCount);
        }


}