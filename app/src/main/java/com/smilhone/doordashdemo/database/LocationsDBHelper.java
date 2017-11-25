package com.smilhone.doordashdemo.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by smilhone on 11/20/2017.
 */

public class LocationsDBHelper extends BaseDBHelper {
    /**
     * Updates the location record for the given latitude/longitude.
     * @param db The database to use.
     * @param location The values to update.
     * @param latitude The latitude for the location.
     * @param longitude The longitude for the location.
     * @return The number of rows updated.
     */
    public static int updateRow(SQLiteDatabase db, ContentValues location, double latitude, double longitude) {
        String whereClause = MetadataDatabase.LocationsTableColumns.LATITUDE + " = ? AND " +
                MetadataDatabase.LocationsTableColumns.LONGITUDE + " = ?";
        String[] whereClauseArgs = {String.valueOf(latitude), String.valueOf(longitude)};
        return db.update(MetadataDatabase.LOCATIONS_TABLE_NAME, location, whereClause, whereClauseArgs);
    }

    /**
     * Gets a property cursor for the requested location.
     * @param db The database to use.
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     * @return
     */
    public static Cursor getPropertyCursor(SQLiteDatabase db, double latitude, double longitude) {
        String selection = MetadataDatabase.LocationsTableColumns.LATITUDE + " = ? AND " +
                MetadataDatabase.LocationsTableColumns.LONGITUDE + " = ?";
        String[] selectionArgs = {String.valueOf(latitude), String.valueOf(longitude)};
        return db.query(MetadataDatabase.LOCATIONS_TABLE_NAME, null, selection, selectionArgs, "", "", "");
    }

    public static long insertRow(SQLiteDatabase db, double latitude, double longitude) {
        ContentValues location = new ContentValues();
        location.put(MetadataDatabase.LocationsTableColumns.LATITUDE, latitude);
        location.put(MetadataDatabase.LocationsTableColumns.LONGITUDE, longitude);
        return db.insert(MetadataDatabase.LOCATIONS_TABLE_NAME, "", location);
    }
}
