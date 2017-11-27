package com.smilhone.doordashdemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;

import com.smilhone.doordashdemo.common.CursorUtils;
import com.smilhone.doordashdemo.database.MetadataDatabase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Helper methods for unit tests.
 *
 * Created by smilhone on 11/26/2017.
 */

public class UnitTestUtils {
    /**
     * Cleans the state of the database, deleting all rows from every table.
     */
    public static void cleanDatabase() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();

        // Delete the contents of the locations and restaurants table.  Rely on foreign key constraints to clear
        // the restaurants_list table.
        db.delete(MetadataDatabase.LOCATIONS_TABLE_NAME, null, null);
        db.delete(MetadataDatabase.RESTAURANTS_TABLE_NAME, null, null);
    }

    /**
     * Inserts a restaurant into the restaurants table.
     *
     * @param restId The restaurant id.
     * @param name The name of the restaurant.
     * @param description The description of the restaurant.
     *
     * @return The rowId of the inserted restaurant record.
     */
    public static long insertRestaurant(int restId, String name, String description) {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();

        ContentValues values = createRestaurant(restId, name, description);
        return db.insert(MetadataDatabase.RESTAURANTS_TABLE_NAME,"",  values);
    }

    /**
     * Creates a ContentValues object for the restaurant.
     *
     * param restId The restaurant's REST_ID.
     * @param name The name of the restaurant.
     * @param description The description of the restaurant.
     *
     * @return A ContentValues object for the restaurant.
     */
    public static ContentValues createRestaurant(int restId, String name, String description) {
        ContentValues values = new ContentValues();
        values.put(MetadataDatabase.RestaurantsTableColumns.REST_ID, restId);
        values.put(MetadataDatabase.RestaurantsTableColumns.NAME, name);
        values.put(MetadataDatabase.RestaurantsTableColumns.DESCRIPTION, description);
        return values;
    }

    /**
     * Inserts a location into the locations table.
     *
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     *
     * @return The rowId of the inserted locations record.
     */
    public static long insertLocation(double latitude, double longitude) {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MetadataDatabase.LocationsTableColumns.LATITUDE, latitude);
        values.put(MetadataDatabase.LocationsTableColumns.LONGITUDE, longitude);

        return db.insert(MetadataDatabase.LOCATIONS_TABLE_NAME,"",  values);
    }

    /**
     * Inserts an entry into the restaurant_list table.
     *
     * @param restRowId The rowId of the restaurant in the restaurants table.
     * @param locationRowId The rowId of the location in the locations table.
     *
     * @return The rowId of the inserted restaurant_list record.
     */
    public static long insertRestaurantList(long restRowId, long locationRowId) {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MetadataDatabase.RestaurantListTableColumns.RESTAURANT_ID, restRowId);
        values.put(MetadataDatabase.RestaurantListTableColumns.LOCATION_ID, locationRowId);

        return db.insert(MetadataDatabase.RESTAURANT_LIST_TABLE_NAME,"",  values);
    }

    /**
     * Verifies the RestaurantList.IS_DIRTY column for the given restaurant_list rowId.  Dirty rows should have a non-zero
     * value.
     *
     * @param db The database to use.
     * @param restListRowId The rowId of the record in the restaurant_list table.
     * @param isDirtyExpected The expected value of the isDirty flag.  True if the column should be flagged as dirty.
     */
    public static void verifyIsDirtyFlag(SQLiteDatabase db, long restListRowId, boolean isDirtyExpected) {
        String[] projection = {MetadataDatabase.RestaurantListTableColumns.IS_DIRTY};
        String selection = MetadataDatabase.PropertyTableColumns.ID + " = ?";
        String[] selectionArgs = {String.valueOf(restListRowId)};
        Cursor cursor = null;
        try {
            cursor = db.query(MetadataDatabase.RESTAURANT_LIST_TABLE_NAME, projection, selection, selectionArgs, null,
                              null, null);
            assertTrue(cursor.moveToFirst());

            // Dirty rows will have a value != 0.
            int isDirtyActual = cursor.getInt(0);
            assertEquals(isDirtyExpected, isDirtyActual != 0);
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
    }

    /**
     * Loads the record from the locations table and returns it inside of a ContentValues object.
     *
     * @param db The database to use.
     * @param locationRowId The rowId of the record in the locations table to load.
     *
     * @return A ContentValues object for the locations record being loaded into memory.
     */
    public static ContentValues getLocationFromDb(SQLiteDatabase db, long locationRowId) {
        String selection = MetadataDatabase.PropertyTableColumns.ID + " = ?";
        String[] selectionArgs = {String.valueOf(locationRowId)};
        ContentValues location = new ContentValues();
        Cursor cursor = null;
        try {
            cursor = db.query(MetadataDatabase.LOCATIONS_TABLE_NAME, null, selection, selectionArgs, null, null, null);
            assertTrue(cursor.moveToFirst());
            DatabaseUtils.cursorRowToContentValues(cursor, location);
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return location;
    }
}
