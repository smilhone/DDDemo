package com.smilhone.doordashdemo.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.smilhone.doordashdemo.common.CursorUtils;
import com.smilhone.doordashdemo.database.LocationsDBHelper;
import com.smilhone.doordashdemo.database.MetadataDatabase;
import com.smilhone.doordashdemo.database.RestaurantListDBHelper;
import com.smilhone.doordashdemo.database.RestaurantsDBHelper;
import com.smilhone.doordashdemo.transport.DataFetcher;
import com.smilhone.doordashdemo.transport.DataWriter;
import com.smilhone.doordashdemo.transport.RefreshManager;
import com.smilhone.doordashdemo.transport.RefreshTask;
import com.smilhone.doordashdemo.transport.fetchers.RestaurantListDataFetcher;
import com.smilhone.doordashdemo.transport.writers.LocationsDataWriter;

import java.util.Set;

/**
 * MetadataContentProvider is responsible for handling all internal requests for metadata.
 *
 * Created by smilhone on 11/20/2017.
 */

public class MetadataContentProvider extends ContentProvider {
    private static final int LOCATION_PROPERTY = 1;
    private static final int LOCATION_LIST = 2;
    private static final int RESTAURANT_PROPERTY = 3;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(Contract.AUTHORITY, Contract.LOCATION + "/" + Contract.PROPERTY, LOCATION_PROPERTY);
        sUriMatcher.addURI(Contract.AUTHORITY, Contract.LOCATION + "/" + Contract.LIST, LOCATION_LIST);
        sUriMatcher.addURI(Contract.AUTHORITY, Contract.RESTAURANT + "/#/" + Contract.PROPERTY, RESTAURANT_PROPERTY);
    }

    public static Uri getLocationPropertyUri(double latitude, double longitude) {
        return new Uri.Builder().scheme("content")
                                .authority(Contract.AUTHORITY)
                                .appendPath(Contract.LOCATION)
                                .appendPath(Contract.PROPERTY)
                                .appendQueryParameter(Contract.LATITUDE, String.valueOf(latitude))
                                .appendQueryParameter(Contract.LONGITUDE, String.valueOf(longitude))
                                .build();
    }

    public static Uri getLocationListUri(double latitude, double longitude) {
        return new Uri.Builder().scheme("content")
                                .authority(Contract.AUTHORITY)
                                .appendPath(Contract.LOCATION)
                                .appendPath(Contract.LIST)
                                .appendQueryParameter(Contract.LATITUDE, String.valueOf(latitude))
                                .appendQueryParameter(Contract.LONGITUDE, String.valueOf(longitude))
                                .build();
    }

    public static Uri getRestaurantPropertyUri(long restRowId) {
        return new Uri.Builder().scheme("content")
                                .authority(Contract.AUTHORITY)
                                .appendPath(Contract.RESTAURANT)
                                .appendPath(String.valueOf(restRowId))
                                .appendPath(Contract.PROPERTY)
                                .build();
    }

    @Override
    public boolean onCreate() {
        // Create the database.
        MetadataDatabase.getInstance(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionARgs, @Nullable String sortOrder) {
        int match = sUriMatcher.match(uri);
        Cursor result = null;
        switch (match) {
            case LOCATION_PROPERTY : {
                result = getLocationPropertyCursorAndScheduleRefresh(uri);
                if (result != null) {
                    result.setNotificationUri(getContext().getContentResolver(), Uri.parse(Contract.FLAT_NOTIFICATION_URI));
                }
                break;
            }
            case LOCATION_LIST : {
                Cursor propertyCursor = getLocationPropertyCursorAndScheduleRefresh(uri);
                if (propertyCursor != null && propertyCursor.moveToFirst()) {
                    result = getLocationListCursor(propertyCursor);
                    if (result != null) {
                        result.setNotificationUri(getContext().getContentResolver(), Uri.parse(Contract.FLAT_NOTIFICATION_URI));
                    }
                }
                CursorUtils.closeQuietly(propertyCursor);
                break;
            }
            case RESTAURANT_PROPERTY : {
                result = null;
                break;
            }
            default:
                return null;
        }
        return result;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case LOCATION_PROPERTY : {
                Set<String> queryParameterNames = uri.getQueryParameterNames();
                if (!queryParameterNames.contains(Contract.LATITUDE) || !queryParameterNames.contains(Contract.LONGITUDE)){
                    // Don't update if we the query parameters aren't there.
                    return 0;
                }
                Double latitude = Double.parseDouble(uri.getQueryParameter(Contract.LATITUDE));
                Double longitude = Double.parseDouble(uri.getQueryParameter(Contract.LONGITUDE));
                SQLiteDatabase db = MetadataDatabase.getInstance(getContext()).getWritableDatabase();
                int rowsUpdated = LocationsDBHelper.updateRow(db, contentValues, latitude, longitude);
                if (rowsUpdated > 0) {
                    getContext().getContentResolver().notifyChange(Uri.parse(Contract.FLAT_NOTIFICATION_URI), null);
                }
                return rowsUpdated;
            }
            case LOCATION_LIST : {
                // Updating the location list is not supported.
                return 0;
            }
            case RESTAURANT_PROPERTY : {
                SQLiteDatabase db = MetadataDatabase.getInstance(getContext()).getWritableDatabase();
                long restRowId = Long.parseLong(uri.getPathSegments().get(1));
                int rowsUpdated = RestaurantsDBHelper.updateRestaurant(db, contentValues, restRowId);
                if (rowsUpdated > 0) {
                    getContext().getContentResolver().notifyChange(Uri.parse(Contract.FLAT_NOTIFICATION_URI), null);
                }
                break;
            }
            default:
                return 0;
        }
        return 0;
    }

    /**
     * Gets a location property cursor and schedules a refresh.  If there isn't a location record, one will be created.
     *
     * @param uri The uri for the location.
     *
     * @return A cursor containing information about the requested location.
     */
    private Cursor getLocationPropertyCursorAndScheduleRefresh(Uri uri) {
        Set<String> queryParameterNames = uri.getQueryParameterNames();
        if (!queryParameterNames.contains(Contract.LATITUDE) || !queryParameterNames.contains(Contract.LONGITUDE)){
            // Don't update if we the query parameters aren't there.
            return null;
        }
        Double latitude = Double.parseDouble(uri.getQueryParameter(Contract.LATITUDE));
        Double longitude = Double.parseDouble(uri.getQueryParameter(Contract.LONGITUDE));

        Cursor cursor = getLocationPropertyCursor(latitude, longitude);
        // Use a property uri for refreshing the list of restaurants so the sync state gets updated correctly.
        Uri refreshUri = getLocationPropertyUri(latitude, longitude);
        boolean refreshSchedule = RefreshManager.getInstance().scheduleRefresh(getLocationRefreshTask(cursor, latitude, longitude), refreshUri, this);

        if (refreshSchedule) {
            CursorUtils.closeQuietly(cursor);
            cursor = getLocationPropertyCursor(latitude, longitude);
        }
        return cursor;
    }

    /**
     * Gets a property cursor for the location defined by the given latitude / longitude.  The cursor will be positioned
     * at the first record.  If a record doesn't exist, one will be created.
     *
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     * @return
     */
    private Cursor getLocationPropertyCursor(double latitude, double longitude) {
        Cursor cursor;
        SQLiteDatabase db = MetadataDatabase.getInstance(getContext()).getWritableDatabase();
        try {
            db.beginTransaction();
            cursor = LocationsDBHelper.getPropertyCursor(db, latitude, longitude);
            if (!cursor.moveToFirst()) {
                // Since there's no record, close the cursor, insert a record, and re-query.
                CursorUtils.closeQuietly(cursor);
                LocationsDBHelper.insertRow(db, latitude, longitude);
                cursor = LocationsDBHelper.getPropertyCursor(db, latitude, longitude);

                // Move the cursor to the first position.
                cursor.moveToFirst();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return cursor;
    }

    private RefreshTask getLocationRefreshTask(Cursor locationCursor, double latitude, double longitude) {
        ContentValues location = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(locationCursor, location);
        DataFetcher dataFecther = new RestaurantListDataFetcher(location);
        DataWriter dataWriter = new LocationsDataWriter(location);
        String refreshTaskKey = "location_" + String.valueOf(latitude) + "," + String.valueOf(longitude);
        return new RefreshTask(dataFecther, dataWriter, refreshTaskKey);
    }

    private Cursor getLocationListCursor(Cursor locationPropertyCursor) {
        SQLiteDatabase db = MetadataDatabase.getInstance(getContext()).getReadableDatabase();
        long locationRowId = locationPropertyCursor.getLong(
                locationPropertyCursor.getColumnIndex(MetadataDatabase.PropertyTableColumns.ID));
        return RestaurantListDBHelper.getRestaurantListListCursor(db, new String[]{} , locationRowId);
    }

    public static final class Contract {
        public static final String AUTHORITY = "com.smilhone.doordashdemo.content.metadata";
        public static final String PROPERTY = "property";
        public static final String LIST = "list";
        public static final String LOCATION = "location";
        public static final String RESTAURANT = "restaurant";
        public static final String LATITUDE = "lat";
        public static final String LONGITUDE = "lng";

        public static final String FLAT_NOTIFICATION_URI = "content://" + AUTHORITY  + "/" + "allItems";
    }
}
