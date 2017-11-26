package com.smilhone.doordashdemo;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.TextView;

import com.smilhone.doordashdemo.adapters.RestaurantListAdapter;
import com.smilhone.doordashdemo.content.MetadataContentProvider;
import com.smilhone.doordashdemo.database.MetadataDatabase;
import com.smilhone.doordashdemo.viewmodels.RestaurantListViewModel;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RestaurantListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FavoriteButtonListener mFavoriteButtonListener = new FavoriteButtonListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // create adapter.
        mAdapter = new RestaurantListAdapter();
        mAdapter.setFavoriteButtonListener(mFavoriteButtonListener);
        mRecyclerView.setAdapter(mAdapter);

        RestaurantListViewModel viewModel = ViewModelProviders.of(this).get(RestaurantListViewModel.class);
        viewModel.getListCursor().observe(this, new Observer<Cursor>() {
            @Override
            public void onChanged(@Nullable Cursor cursor) {
                mAdapter.swapCursor(cursor);
            }
        });
        viewModel.query(getApplicationContext(), getLoaderManager());
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
