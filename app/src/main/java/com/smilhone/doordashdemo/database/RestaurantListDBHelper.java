package com.smilhone.doordashdemo.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.smilhone.doordashdemo.common.CursorUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Database helper methods for the restaurant_list table.
 * Created by smilhone on 11/21/2017.
 */

public class RestaurantListDBHelper extends BaseDBHelper {
    private static String[] RESTAURANTS_RESTAURANT_LIST_PROJECTION;

    private static Map<String, String> RESTAURANTS_LIST_PROJECTION_COLUMN_LOOKUP = new HashMap<>();

    private static String[] RESTAURANTS_COLUMNS_IN_PROJECTION = new String[] {
            MetadataDatabase.PropertyTableColumns.ID,
            MetadataDatabase.RestaurantsTableColumns.NAME,
            MetadataDatabase.RestaurantsTableColumns.IS_FAVORITE,
            MetadataDatabase.RestaurantsTableColumns.COVER_IMG_URL,
            MetadataDatabase.RestaurantsTableColumns.DESCRIPTION,
            MetadataDatabase.RestaurantsTableColumns.REST_ID,
            MetadataDatabase.RestaurantsTableColumns.PRICE_RANGE,
            MetadataDatabase.RestaurantsTableColumns.IS_NEWLY_ADDED,
            MetadataDatabase.RestaurantsTableColumns.NUM_RATINGS,
            MetadataDatabase.RestaurantsTableColumns.IS_TIME_SURGING,
            MetadataDatabase.RestaurantsTableColumns.STATUS
    };

    static {
        // Create a projection for the restaurant - restaurant_list inner join.
        addColumnsToLookup(MetadataDatabase.RESTAURANTS_TABLE_NAME, RESTAURANTS_COLUMNS_IN_PROJECTION,
                           RESTAURANTS_LIST_PROJECTION_COLUMN_LOOKUP);
        RESTAURANTS_RESTAURANT_LIST_PROJECTION = new String[RESTAURANTS_LIST_PROJECTION_COLUMN_LOOKUP.size()];
        RESTAURANTS_LIST_PROJECTION_COLUMN_LOOKUP.values()
                                                 .toArray(RESTAURANTS_RESTAURANT_LIST_PROJECTION);
    }

    /**
     * Inserts a row into the restaurant_list table.
     * @param db The database to use.
     * @param restaurantList A restaurant_list item to insert.
     * @return The rowId of the inserted restaurant_list record.
     */
    public static long insert(SQLiteDatabase db, ContentValues restaurantList) {
        return db.insert(MetadataDatabase.RESTAURANT_LIST_TABLE_NAME, "", restaurantList);
    }

    /**
     * Inserts or updates the record in the restaurant_list table.
     * @param db The database to use.
     * @param restaurantList A restaurant_list item to insert / update.
     * @return The rowId of the restaurant_list record.
     */
    public static long insertOrUpdate(SQLiteDatabase db, ContentValues restaurantList) {
        long restRowId = restaurantList.getAsLong(MetadataDatabase.RestaurantListTableColumns.RESTAURANT_ID);
        long locationRowId = restaurantList.getAsLong(MetadataDatabase.RestaurantListTableColumns.LOCATION_ID);
        final String whereClause = MetadataDatabase.RestaurantListTableColumns.RESTAURANT_ID + " = ? AND " +
                MetadataDatabase.RestaurantListTableColumns.LOCATION_ID + " = ?";
        final String[] whereArgs = {String.valueOf(restRowId), String.valueOf(locationRowId)};
        long rowsUpdated = db.update(MetadataDatabase.RESTAURANT_LIST_TABLE_NAME, restaurantList, whereClause,
                                     whereArgs);
        if (rowsUpdated > 0) {
            return findRowId(db, restRowId, locationRowId);
        } else {
            return insert(db, restaurantList);
        }
    }

    /**
     * Finds the rowId for a given restaurant_list record defined by the restaurantRowId and locationRowId.
     * @param db The database to use.
     * @param restListId The rowId of the restaurant record.
     * @param locationRowId The rowId of the locations record
     * @return The rowId of the restaurant_list item.
     */
    public static long findRowId(SQLiteDatabase db, long restListId, long locationRowId) {
        long rowId = BaseDBHelper.ROW_NOT_FOUND;
        final String selection = MetadataDatabase.RestaurantListTableColumns.RESTAURANT_ID + " = ? AND " +
                MetadataDatabase.RestaurantListTableColumns.LOCATION_ID + " = ?";
        final String[] selectionArgs = {String.valueOf(restListId), String.valueOf(locationRowId)};
        final String[] projection = {MetadataDatabase.PropertyTableColumns.ID};
        Cursor cursor = null;
        try {
            cursor = db.query(MetadataDatabase.RESTAURANT_LIST_TABLE_NAME, projection, selection, selectionArgs, "", "",
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
     * Marks all records for the given locationRowId as dirty.
     * @param db The database to use.
     * @param locationRowId The rowId of the location record used for marking entries dirty.
     * @return The number of records marked dirty.
     */
    public static long markRowsDiry(SQLiteDatabase db, long locationRowId) {
        ContentValues values = new ContentValues();
        values.put(MetadataDatabase.RestaurantListTableColumns.IS_DIRTY, true);
        final String whereClause = MetadataDatabase.RestaurantListTableColumns.LOCATION_ID + " = ?";
        final String[] whereClauseArgs = {String.valueOf(locationRowId)};
        return db.update(MetadataDatabase.RESTAURANT_LIST_TABLE_NAME, values, whereClause, whereClauseArgs);
    }

    /**
     * Deletes all dirty records for a given locationRowId.
     * @param db The database to use.
     * @param locationRowId The rowId of the location record used for deleting dirty records.
     * @return The number of dirty records.
     */
    public static long deleteDirtyRows(SQLiteDatabase db, long locationRowId) {
        final String whereClause = MetadataDatabase.RestaurantListTableColumns.LOCATION_ID + " = ? AND " +
                MetadataDatabase.RestaurantListTableColumns.IS_DIRTY + " = 1";
        final String[] whereClauseArgs = {String.valueOf(locationRowId)};
        return db.delete(MetadataDatabase.RESTAURANT_LIST_TABLE_NAME, whereClause, whereClauseArgs);
    }

    /**
     * Gets a list cursor for restaurants available at the given location.
     * @param db The database to use.
     * @param projection The projection to return.
     * @param locationRowId The rowId of the location.
     * @return A list cursor containing information of the restaurants available at the given location.
     */
    public static Cursor getRestaurantListListCursor(SQLiteDatabase db, String[] projection, long locationRowId) {
        String[] projectionToUse = projection.length == 0 ? RESTAURANTS_RESTAURANT_LIST_PROJECTION : projection;
        String tableName = BaseDBHelper.innerJoin(MetadataDatabase.RESTAURANTS_TABLE_NAME,
                                                  MetadataDatabase.RESTAURANT_LIST_TABLE_NAME,
                                                  MetadataDatabase.PropertyTableColumns.ID,
                                                  MetadataDatabase.RestaurantListTableColumns.RESTAURANT_ID);
        String selection = MetadataDatabase.RestaurantListTableColumns.LOCATION_ID + " = ?";
        String[] selectionArgs = {String.valueOf(locationRowId)};
        String orderBy = MetadataDatabase.RestaurantsTableColumns.IS_FAVORITE + " DESC";
        return db.query(tableName, projectionToUse, selection, selectionArgs, "", "", orderBy);
    }
}
