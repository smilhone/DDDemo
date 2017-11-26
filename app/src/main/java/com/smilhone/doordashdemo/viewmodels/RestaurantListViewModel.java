package com.smilhone.doordashdemo.viewmodels;

import android.app.Application;
import android.app.LoaderManager;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.smilhone.doordashdemo.R;
import com.smilhone.doordashdemo.content.MetadataContentProvider;
import com.smilhone.doordashdemo.database.MetadataDatabase;
import com.smilhone.doordashdemo.database.PropertySyncState;

/**
 * View model for the main activity.  Loads the list of restaurants for the current location.
 *
 * Created by smilhone on 11/24/2017.
 */
public class RestaurantListViewModel extends AndroidViewModel implements LoaderManager.LoaderCallbacks<Cursor> {
    private MutableLiveData<Cursor> mListCursor = new MutableLiveData<>();
    private MutableLiveData<Cursor> mPropertyCursor = new MutableLiveData<>();
    private MutableLiveData<RestaurantListViewState> mViewState = new MutableLiveData<>();

    public RestaurantListViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Gets the list of restaurants to display.
     *
     * @return The list of restaurants to display.
     */
    public MutableLiveData<Cursor> getListCursor() {
        return mListCursor;
    }

    /**
     * Gets the view state of the restaurant list activity.  Indicates if the UI should show the list of restaurants, a
     * progress bar, or an error message.
     *
     * @return The view state for the restaurant list activity.
     */
    public MutableLiveData<RestaurantListViewState> getViewState() {
        return mViewState;
    }

    /**
     * Starts loading data for the view model.
     *
     * @param loaderManager The loader manager responsible for loading data.
     */
    public void query(LoaderManager loaderManager) {
        loaderManager.initLoader(R.id.metadata_restaurant_list_cursor_id, null, this);
        loaderManager.initLoader(R.id.metadata_restaurant_list_proprty_cursor_id, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // Hard-coded latitude/longitude to test multiple locations.
        final double SAN_FRAN_LATITUDE = 37.422740;
        final double SAN_FRAN_LONGITUDE = -122.139956;
        final double REDMOND_LATITUDE = 47.67858;
        final double REDMOND_LONGITUDE = -122.1316;

        if (id == R.id.metadata_restaurant_list_cursor_id) {
            return new CursorLoader(getApplication().getApplicationContext(),
                                    MetadataContentProvider.getLocationListUri(SAN_FRAN_LATITUDE, SAN_FRAN_LONGITUDE),
                                    null,
                                    null,
                                    null,
                                    null);
        } else if (id == R.id.metadata_restaurant_list_proprty_cursor_id) {
            return new CursorLoader(getApplication().getApplicationContext(),
                                    MetadataContentProvider.getLocationPropertyUri(SAN_FRAN_LATITUDE, SAN_FRAN_LONGITUDE),
                                    null,
                                    null,
                                    null,
                                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == R.id.metadata_restaurant_list_cursor_id) {
            mListCursor.setValue(cursor);
        } else if (loader.getId() == R.id.metadata_restaurant_list_proprty_cursor_id) {
            mPropertyCursor.setValue(cursor);
        }
        // After a cursor has been loaded, update the view state.
        computeViewState();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mListCursor.setValue(null);
        mPropertyCursor.setValue(null);
        // reset the view state.
        computeViewState();
    }

    /**
     * Compute the proper view state for the list of restaurants.
     */
    private void computeViewState() {
        Cursor propertyCursor = mPropertyCursor.getValue();
        Cursor listCursor = mListCursor.getValue();
        if (propertyCursor == null || listCursor == null) {
            mViewState.setValue(RestaurantListViewState.LOADING);
            return;
        }

        // Default to a loading state.
        RestaurantListViewState viewState = RestaurantListViewState.LOADING;

        // If we have cached data, always show that over a progress bar / error message.
        if (listCursor.getCount() > 0) {
            viewState = RestaurantListViewState.SHOW_LIST;
        } else {
            // Since we don't have cached data, check the sync state on the property cursor.
            PropertySyncState syncState = PropertySyncState.fromInt(propertyCursor.getInt(
                    propertyCursor.getColumnIndex(MetadataDatabase.PropertyTableColumns.SYNC_STATUS)));
            switch (syncState) {
                case NOT_IN_CACHE:
                case REFRESHING:
                    viewState = RestaurantListViewState.LOADING;
                    break;
                case REFRESH_COMPLETE:
                case REFRESH_FAILED:
                    viewState = RestaurantListViewState.SHOW_EMPTY_MESSAGE;
                    break;
            }
        }
        mViewState.setValue(viewState);
    }
}
