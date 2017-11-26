package com.smilhone.doordashdemo;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.smilhone.doordashdemo.adapters.RestaurantListAdapter;
import com.smilhone.doordashdemo.content.MetadataContentProvider;
import com.smilhone.doordashdemo.database.MetadataDatabase;
import com.smilhone.doordashdemo.viewmodels.RestaurantListViewModel;
import com.smilhone.doordashdemo.viewmodels.RestaurantListViewState;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RestaurantListAdapter mAdapter;
    private FavoriteButtonListener mFavoriteButtonListener = new FavoriteButtonListener();
    private ProgressBar mLoadingProgressBar;
    private TextView mErrorMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLoadingProgressBar = findViewById(R.id.loading_spinner);
        mErrorMessageTextView = findViewById(R.id.error_message);
        mRecyclerView = findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // create adapter.
        mAdapter = new RestaurantListAdapter();
        mAdapter.setFavoriteButtonListener(mFavoriteButtonListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), mLayoutManager.getOrientation()));

        RestaurantListViewModel viewModel = ViewModelProviders.of(this).get(RestaurantListViewModel.class);
        viewModel.getListCursor().observe(this, new Observer<Cursor>() {
            @Override
            public void onChanged(@Nullable Cursor cursor) {
                mAdapter.swapCursor(cursor);
            }
        });

        viewModel.getViewState().observe(this, new Observer<RestaurantListViewState>() {
            @Override
            public void onChanged(@Nullable RestaurantListViewState restaurantListViewState) {
                if (restaurantListViewState != null) {
                    switch (restaurantListViewState) {
                        case LOADING:
                            mRecyclerView.setVisibility(View.INVISIBLE);
                            mErrorMessageTextView.setVisibility(View.INVISIBLE);
                            mLoadingProgressBar.setVisibility(View.VISIBLE);
                            break;
                        case SHOW_EMPTY_MESSAGE:
                            mRecyclerView.setVisibility(View.INVISIBLE);
                            mErrorMessageTextView.setVisibility(View.VISIBLE);
                            mLoadingProgressBar.setVisibility(View.INVISIBLE);
                            break;
                        case SHOW_LIST:
                            mRecyclerView.setVisibility(View.VISIBLE);
                            mErrorMessageTextView.setVisibility(View.INVISIBLE);
                            mLoadingProgressBar.setVisibility(View.INVISIBLE);
                            break;
                    }
                }
            }
        });
        viewModel.query(getLoaderManager());
    }

    private class FavoriteButtonListener implements OnFavoriteButtonListener {
        @Override
        public void onFavoriteButtonClicked(Context context, ContentValues itemClicked) {
            MarkAsFavoriteAsyncTask task = new MarkAsFavoriteAsyncTask(context, itemClicked);
            task.execute();
        }
    }

    private class MarkAsFavoriteAsyncTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        private ContentValues item;
        MarkAsFavoriteAsyncTask(Context context, ContentValues values) {
            mContext = context;
            item = values;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            Uri uri = MetadataContentProvider.getRestaurantPropertyUri(item.getAsLong(MetadataDatabase.RestaurantsTableColumns.REST_ID));
            ContentValues values = new ContentValues();
            Integer isFavorite = item.getAsInteger(MetadataDatabase.RestaurantsTableColumns.IS_FAVORITE);
            if (isFavorite != null && isFavorite == 1) {
                values.putNull(MetadataDatabase.RestaurantsTableColumns.IS_FAVORITE);
            } else {
                values.put(MetadataDatabase.RestaurantsTableColumns.IS_FAVORITE, true);
            }
            mContext.getContentResolver().update(uri, values, null, null);
            return null;
        }
    }
}
