package com.smilhone.doordashdemo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Property;

/**
 * Created by smilhone on 11/16/2017.
 */

public class MetadataDatabase extends SQLiteOpenHelper {
    private static final Object sSingletonLock = new Object();
    private static MetadataDatabase sInstance = null;
    private static int DB_VERSION = 1;
    private static final String DB_NAME = "metadata";

    private static final String SQL_TYPE_TEXT = "TEXT";
    private static final String SQL_TYPE_INT = "INTEGER";
    private static final String SQL_TYPE_BOOLEAN = "BOOLEAN";
    private static final String SQL_TYPE_REAL = "REAL";

    public static final String RESTAURANTS_TABLE_NAME = "restaurants";
    public static final String RESTAURANT_LIST_TABLE_NAME = "restaurant_list";
    public static final String LOCATIONS_TABLE_NAME = "locations";

    private static final String LOCATIONS_TABLE_COLUMNS =
            LocationsTableColumns.LONGITUDE + " " + SQL_TYPE_REAL + ", " +
                    LocationsTableColumns.LATITUDE + " " + SQL_TYPE_REAL;

    private static final String RESTAURANTS_TABLE_COLUMNS =
            RestaurantsTableColumns.REST_ID + " " + SQL_TYPE_TEXT + ", " +
                    RestaurantsTableColumns.IS_FAVORITE + " " + SQL_TYPE_BOOLEAN + ", " +
                    RestaurantsTableColumns.NAME + " " + SQL_TYPE_TEXT + ", " +
                    RestaurantsTableColumns.DESCRIPTION + " " + SQL_TYPE_TEXT + ", " +
                    RestaurantsTableColumns.NUM_RATINGS + " " + SQL_TYPE_INT + ", " +
                    RestaurantsTableColumns.PRICE_RANGE + " " + SQL_TYPE_INT + ", " +
                    RestaurantsTableColumns.COVER_IMG_URL + " " + SQL_TYPE_TEXT + ", " +
                    RestaurantsTableColumns.IS_NEWLY_ADDED + " " + SQL_TYPE_BOOLEAN + ", " +
                    RestaurantsTableColumns.IS_TIME_SURGING + " " + SQL_TYPE_BOOLEAN + ", " +
                    RestaurantsTableColumns.STATUS + " " + SQL_TYPE_TEXT;

    private static final String RESTAURANT_LIST_TABLE_COLUMNS =
            RestaurantListTableColumns.LOCATION_ID + " " + SQL_TYPE_INT + ", " +
                    RestaurantListTableColumns.RESTAURANT_ID + " " + SQL_TYPE_INT + ", " +
                    RestaurantListTableColumns.IS_DIRTY + " " + SQL_TYPE_BOOLEAN + ", " +
                    "FOREIGN KEY(" + RestaurantListTableColumns.LOCATION_ID + ") REFERENCES " +
                    LOCATIONS_TABLE_NAME + "(" + PropertyTableColumns.ID + ") ON DELETE CASCADE "+
                    "FOREIGN KEY(" + RestaurantListTableColumns.RESTAURANT_ID + ") REFERENCES " +
                    RESTAURANTS_TABLE_NAME + "(" + PropertyTableColumns.ID + ") ON DELETE CASCADE";

    /**
     * Gets the static instance of the database.
     *
     * @param context The application context.
     *
     * @return An instance to the database.
     */
    public static MetadataDatabase getInstance(Context context) {
        synchronized (sSingletonLock) {
            if (sInstance == null) {
                sInstance = new MetadataDatabase(context, DB_NAME, null, DB_VERSION);
            }
        }
        return sInstance;
    }

    private MetadataDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createPropertyTable(db, LOCATIONS_TABLE_NAME, LOCATIONS_TABLE_COLUMNS);
        createPropertyTable(db, RESTAURANTS_TABLE_NAME, RESTAURANTS_TABLE_COLUMNS);
        createTable(db, RESTAURANT_LIST_TABLE_NAME, RESTAURANT_LIST_TABLE_COLUMNS);
        Log.v("MetadataDatabase", db.getPath());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Creates a property table.
     * @param db The database to use.
     * @param tableName The name of the table.
     * @param columns The additional columns to create for the table.  Adds the property columns
     *                automatically for you.
     */
    private void createPropertyTable(SQLiteDatabase db, String tableName, String columns) {
        String propertyColumns = PropertyTableColumns.LAST_SYNC_TIME + " " + SQL_TYPE_INT + ", " +
                    PropertyTableColumns.SYNC_STATUS + " " + SQL_TYPE_INT + ", " +
                    PropertyTableColumns.ERROR + " " + SQL_TYPE_INT ;
        createTable(db, tableName, TextUtils.isEmpty(columns) ? propertyColumns : propertyColumns + ", " + columns);
    }

    /**
     * Creates a table.
     * @param db The database to create a table in.
     * @param tableName The name of the table to create.
     * @param columns The additional columns to create for the table.  The primary key _id is
     *                created automatically.
     */
    private void createTable(SQLiteDatabase db, String tableName, String columns) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + PropertyTableColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT "
                + (TextUtils.isEmpty(columns) ? "" : ", " + columns) + ");");
    }

    public static class PropertyTableColumns {
        public static final String ID = "_id";
        public static final String LAST_SYNC_TIME = "_property_last_sync_time";
        public static final String SYNC_STATUS = "_property_sync_status";
        public static final String ERROR = "_property_error";
    }

    public static class LocationsTableColumns {
        public static final String LONGITUDE = "longitude";
        public static final String LATITUDE = "latitude";
    }

    public static class RestaurantsTableColumns {
        public static final String REST_ID = "restId";
        public static final String IS_FAVORITE = "isFavorite";
        public static final String COVER_IMG_URL = "coverImgUrl";
        public static final String DESCRIPTION = "description";
        public static final String NAME = "name";
        public static final String NUM_RATINGS = "numRatings";
        public static final String PRICE_RANGE = "priceRange";
        public static final String IS_NEWLY_ADDED = "isNewlyAdded";
        public static final String IS_TIME_SURGING = "isTimeSurging";
        public static final String STATUS = "status";
    }

    public static class RestaurantListTableColumns {
        public static final String LOCATION_ID = "locationId";
        public static final String RESTAURANT_ID = "restaurantId";
        public static final String IS_DIRTY = "isDirty";
    }
}
