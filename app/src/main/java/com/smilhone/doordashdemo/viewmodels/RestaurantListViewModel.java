package com.smilhone.doordashdemo.viewmodels;

import android.app.Application;
import android.app.LoaderManager;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.smilhone.doordashdemo.R;
import com.smilhone.doordashdemo.content.MetadataContentProvider;

/**
 * View model for the main activity.  Loads the list of restaurants for the current location.
 *
 * Created by smilhone on 11/24/2017.
 */

public class RestaurantListViewModel extends AndroidViewModel implements LoaderManager.LoaderCallbacks<Cursor> {
    private MutableLiveData<Cursor> mListCursor = new MutableLiveData<>();
    private MutableLiveData<Cursor> mPropertyCursor = new MutableLiveData<>();

    public RestaurantListViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<Cursor> getListCursor() {
        return mListCursor;
    }

    public MutableLiveData<Cursor> getPropertyCursor() {
        return mPropertyCursor;
    }

    public void query(Context context, LoaderManager loaderManager) {
        loaderManager.initLoader(R.id.metadata_restaurant_list_cursor_id, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this.getApplication().getApplicationContext(),
                                MetadataContentProvider.getLocationListUri(37.422740, -122.139956),
                                null,
                                null,
                                null,
                                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mListCursor.setValue(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mListCursor.setValue(null);
    }
}
