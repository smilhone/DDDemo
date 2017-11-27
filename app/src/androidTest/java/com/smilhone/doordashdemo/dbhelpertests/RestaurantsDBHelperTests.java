package com.smilhone.doordashdemo.dbhelpertests;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.smilhone.doordashdemo.UnitTestUtils;
import com.smilhone.doordashdemo.common.CursorUtils;
import com.smilhone.doordashdemo.database.BaseDBHelper;
import com.smilhone.doordashdemo.database.MetadataDatabase;
import com.smilhone.doordashdemo.database.RestaurantsDBHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for RestaurantsDBHelper
 *
 * Created by smilhone on 11/26/2017.
 */
@RunWith(AndroidJUnit4.class)
public class RestaurantsDBHelperTests {

    @Before
    public void setup() {
        UnitTestUtils.cleanDatabase();
    }

    @After
    public void cleanup() {
        UnitTestUtils.cleanDatabase();
    }

    @Test
    public void insertRestaurantTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();

        ContentValues restaurant = new ContentValues();
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.REST_ID, 123);
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.IS_FAVORITE, false);
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.NAME, "McDonald's");
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.DESCRIPTION, "Fast Food");

        // Insert a restaurant.
        long restRowId = RestaurantsDBHelper.insertRestaurant(db, restaurant);
        assertTrue(restRowId > 0);
        verifyName(db, restRowId, "McDonald's");

        ContentValues restaurant2 = new ContentValues();
        restaurant2.put(MetadataDatabase.RestaurantsTableColumns.REST_ID, 456);
        restaurant2.put(MetadataDatabase.RestaurantsTableColumns.NAME, "Taco Bell");
        restaurant2.put(MetadataDatabase.RestaurantsTableColumns.DESCRIPTION, "Fast food");

        // Insert a second restaurant
        long restRowId2 = RestaurantsDBHelper.insertRestaurant(db, restaurant2);
        assertTrue(restRowId2 > 0);
        assertTrue(restRowId2 > restRowId);
        verifyName(db, restRowId2, "Taco Bell");
    }

    @Test
    public void findRestaurantRowIdTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();

        ContentValues restaurant = new ContentValues();
        int restId = 123;
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.REST_ID, restId);
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.IS_FAVORITE, false);
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.NAME, "McDonald's");
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.DESCRIPTION, "Fast Food");

        // Verify that the restaurant isn't there since it hasn't been inserted yet.
        long restRowId = RestaurantsDBHelper.findRestaurantRowId(db, restId);
        assertEquals(restRowId, BaseDBHelper.ROW_NOT_FOUND);

        restRowId = RestaurantsDBHelper.insertRestaurant(db, restaurant);
        assertTrue(restRowId > 0);

        long foundRowId = RestaurantsDBHelper.findRestaurantRowId(db, restId);
        assertEquals(restRowId, foundRowId);
    }

    @Test
    public void insertOrUpdateRestaurantTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();

        ContentValues restaurant = new ContentValues();
        int restId = 123;
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.REST_ID, restId);
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.IS_FAVORITE, false);
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.NAME, "McDonald's");
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.DESCRIPTION, "Fast Food");

        // Insert the restaurant.
        long restRowId = RestaurantsDBHelper.insertOrUpdateRestaurant(db, restaurant);
        assertTrue(restRowId > 0);
        verifyName(db, restRowId, "McDonald's");

        // Replace the name of the restaurant.
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.NAME, "Taco Bell");
        long restRowId2 = RestaurantsDBHelper.insertOrUpdateRestaurant(db, restaurant);
        assertEquals(restRowId, restRowId2);
        verifyName(db, restRowId2, "Taco Bell");
    }

    @Test
    public void updateRestaurantTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();

        ContentValues restaurant = new ContentValues();
        int restId = 123;
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.REST_ID, restId);
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.IS_FAVORITE, false);
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.NAME, "McDonald's");
        restaurant.put(MetadataDatabase.RestaurantsTableColumns.DESCRIPTION, "Fast Food");

        // Verify the update fails because there isn't a record.
        long updatedRows = RestaurantsDBHelper.updateRestaurant(db, restaurant, restId);
        assertEquals(0, updatedRows);

        // Insert the restaurant.
        long restRowId = RestaurantsDBHelper.insertRestaurant(db, restaurant);
        assertTrue(restRowId > 0);
        verifyName(db, restRowId, "McDonald's");

        // Update the restaurant's name.
        ContentValues values = new ContentValues();
        values.put(MetadataDatabase.RestaurantsTableColumns.NAME, "Taco Bell");
        updatedRows = RestaurantsDBHelper.updateRestaurant(db, values, restId);
        assertEquals(1, updatedRows);
        verifyName(db, restRowId, "Taco Bell");
    }

    /**
     * Verify the restaurant's name.  The method does a case-insensitive comparison.
     *
     * @param db The database to use.
     * @param restRowId The rowId of the restaurant to verify.
     * @param expectedRestaurantName The expected name of the restaurant.
     */
    private void verifyName(SQLiteDatabase db, long restRowId, String expectedRestaurantName) {
        String[] projection = {MetadataDatabase.RestaurantsTableColumns.NAME};
        String selection = MetadataDatabase.PropertyTableColumns.ID + " = ?";
        String[] selectionArgs = {String.valueOf(restRowId)};
        Cursor cursor = null;
        try {
            cursor = db.query(MetadataDatabase.RESTAURANTS_TABLE_NAME, projection, selection, selectionArgs, null, null, null);
            assertTrue(cursor.moveToFirst());
            String actualRestaurantName = cursor.getString(0);
            assertTrue(actualRestaurantName.equalsIgnoreCase(expectedRestaurantName));
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
    }
}
