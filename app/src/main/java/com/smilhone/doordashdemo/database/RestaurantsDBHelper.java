package com.smilhone.doordashdemo.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smilhone.doordashdemo.common.CursorUtils;

/**
 * DB Helper methods for the restaurants table.
 *
 * Created by smilhone on 11/20/2017.
 */

public class RestaurantsDBHelper extends BaseDBHelper {
    /**
     * Inserts a restaurant into the restarants table.
     *
     * @param db The database to use.
     * @param restaurant The restaurant values to insert.
     *
     * @return The rowId of the inserted record in the restaurants table.
     */
    public static long insertRestaurant(SQLiteDatabase db, ContentValues restaurant) {
        return db.insert(MetadataDatabase.RESTAURANTS_TABLE_NAME, "", restaurant);
    }

    /**
     * Inserts or updates a restaurant.  Performs an update first, and inserts if no records were updated.
     *
     * @param db The database to use.
     * @param restaurant The restaurant to insert/update.
     *
     * @return The rowId of the restaurant that was inserted / updated.
     */
    public static long insertOrUpdateRestaurant(SQLiteDatabase db, ContentValues restaurant) {
        long restRowId = restaurant.getAsLong(MetadataDatabase.RestaurantsTableColumns.REST_ID);
        final String whereClause = MetadataDatabase.RestaurantsTableColumns.REST_ID + " = ?";
        final String[] whereArgs = {String.valueOf(restRowId)};
        long rowsUpdated = db.update(MetadataDatabase.RESTAURANTS_TABLE_NAME, restaurant, whereClause, whereArgs);
        if (rowsUpdated > 0) {
            return findRestaurantRowId(db, restRowId);
        } else {
            return insertRestaurant(db, restaurant);
        }
    }

    /**
     * Finds the rowId of the restaurant.
     *
     * @param db The database to use.
     * @param restId the REST_ID of the restaurant.
     *
     * @return The rowId in the restaurant table for the given REST_ID.
     */
    public static long findRestaurantRowId(SQLiteDatabase db, long restId) {
        long rowId = BaseDBHelper.ROW_NOT_FOUND;
        final String selection = MetadataDatabase.RestaurantsTableColumns.REST_ID + " = ?";
        final String[] selectionArgs = {String.valueOf(restId)};
        final String[] projection = {MetadataDatabase.PropertyTableColumns.ID};
        Cursor cursor = null;
        try {
            cursor = db.query(MetadataDatabase.RESTAURANTS_TABLE_NAME, projection, selection, selectionArgs, "", "",
                              "");
            if (cursor.moveToFirst()) {
                rowId = cursor.getLong(0);
            }
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
        return rowId;
    }

    /**
     * Updates the restaurant defined by the given REST_ID.
     *
     * @param db The database to use.
     * @param values The values to update in the restaurants table.
     * @param restId The REST_ID of the restaurant to update.
     *
     * @return The number of records updated.
     */
    public static int updateRestaurant(SQLiteDatabase db, ContentValues values, long restId) {
        final String whereClause = MetadataDatabase.RestaurantsTableColumns.REST_ID + " = ?";
        final String[] whereClauseArgs = {String.valueOf(restId)};
        return db.update(MetadataDatabase.RESTAURANTS_TABLE_NAME, values, whereClause, whereClauseArgs);
    }
}
