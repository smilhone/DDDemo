package com.smilhone.doordashdemo.transport.writers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smilhone.doordashdemo.database.BaseDBHelper;
import com.smilhone.doordashdemo.database.MetadataDatabase;
import com.smilhone.doordashdemo.database.RestaurantListDBHelper;
import com.smilhone.doordashdemo.database.RestaurantsDBHelper;
import com.smilhone.doordashdemo.transport.DataFetcher;
import com.smilhone.doordashdemo.transport.DataWriter;

import java.util.List;

/**
 * Created by smilhone on 11/20/2017.
 */

public class LocationsDataWriter implements DataWriter {
    private ContentValues mItemToUpdate;
    private long mLocationRowId;
    private final String TAG = LocationsDataWriter.class.getName();

    /**
     * Constructor.
     * @param itemToUpdate The item being refreshed.  This should be a row from the locations table.
     */
    public LocationsDataWriter(ContentValues itemToUpdate) {
        mItemToUpdate = itemToUpdate;
        mLocationRowId = mItemToUpdate.getAsLong(MetadataDatabase.PropertyTableColumns.ID);
    }

    @Override
    public void beforeWriteData() {
        // Mark all restaurant_list entries for the current location as dirty.  After refreshing the list of restuarants
        // for the location, we will delete the entries not returned.
        SQLiteDatabase db = MetadataDatabase.getInstance(null).getWritableDatabase();
        long numDirtyRows = RestaurantListDBHelper.markRowsDiry(db, mLocationRowId);
        Log.v(TAG, "marked " + numDirtyRows + " dirty.");
    }

    @Override
    public void writeData(DataFetcher.FetchedData data) {
        // TODO: Update access mechanism for this.  DataWriter should have access to an object which exposes the database, don't rely on it already being created.
        SQLiteDatabase db = MetadataDatabase.getInstance(null).getWritableDatabase();
        try {
            db.beginTransactionNonExclusive();
            ContentValues itemToUpdate = data.getItemToUpdate();
            List<ContentValues> restaurants = data.getChildren();
            for (ContentValues restaurant : restaurants) {
                long restRowId = RestaurantsDBHelper.insertOrUpdateRestaurant(db, restaurant);

                // If we inserted a record, update the restaurant_list table.
                if (restRowId != BaseDBHelper.ROW_NOT_FOUND) {
                    ContentValues restaurantList = new ContentValues();
                    restaurantList.put(MetadataDatabase.RestaurantListTableColumns.LOCATION_ID, mLocationRowId);
                    restaurantList.put(MetadataDatabase.RestaurantListTableColumns.RESTAURANT_ID, restRowId);
                    restaurantList.putNull(MetadataDatabase.RestaurantListTableColumns.IS_DIRTY);
                    RestaurantListDBHelper.insertOrUpdate(db, restaurantList);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void afterWriteData(Throwable throwable) {
        // Only delete dirty records if we were able to successfully refresh the list of restaurants for the location.
        if (throwable == null) {
            // Delete all dirty records not returned in the list of restaurants for the location.
            SQLiteDatabase db = MetadataDatabase.getInstance(null).getWritableDatabase();
            long numRowsDeleted = RestaurantListDBHelper.deleteDirtyRows(db, mLocationRowId);
            Log.v(TAG, "deleted " + numRowsDeleted + " dirty rows");
        }
    }
}
