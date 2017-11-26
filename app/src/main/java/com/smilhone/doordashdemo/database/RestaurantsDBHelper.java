package com.smilhone.doordashdemo.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smilhone.doordashdemo.common.CursorUtils;

/**
 * Created by smilhone on 11/20/2017.
 */

public class RestaurantsDBHelper extends BaseDBHelper {
    public static long insertRestaurant(SQLiteDatabase db, ContentValues restaurant) {
        return db.insert(MetadataDatabase.RESTAURANTS_TABLE_NAME, "", restaurant);
    }

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

    public static long findRestaurantRowId(SQLiteDatabase db, long restRowId) {
        long rowId = BaseDBHelper.ROW_NOT_FOUND;
        final String selection = MetadataDatabase.RestaurantsTableColumns.REST_ID + " = ?";
        final String[] selectionArgs = {String.valueOf(restRowId)};
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

    public static int updateRestuarant(SQLiteDatabase db, ContentValues values, long restRowId) {
        final String whereClause = MetadataDatabase.RestaurantsTableColumns.REST_ID + " = ?";
        final String[] whereClauseArgs = {String.valueOf(restRowId)};
        return db.update(MetadataDatabase.RESTAURANTS_TABLE_NAME, values, whereClause, whereClauseArgs);
    }
}
