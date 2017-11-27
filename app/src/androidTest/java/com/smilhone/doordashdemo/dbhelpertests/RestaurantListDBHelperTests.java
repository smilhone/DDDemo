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
import com.smilhone.doordashdemo.database.RestaurantListDBHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for RestaurantsListDBHelper
 *
 * Created by smilhone on 11/26/2017.
 */
@RunWith(AndroidJUnit4.class)
public class RestaurantListDBHelperTests {
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

        long locationRowId = UnitTestUtils.insertLocation(44.994, -110.324);
        long restRowId1 = UnitTestUtils.insertRestaurant(123, "Taco Bell", "Fast food");
        long restRowId2 = UnitTestUtils.insertRestaurant(453, "Jimmy John's", "Sandwiches");

        ContentValues restList = new ContentValues();
        restList.put(MetadataDatabase.RestaurantListTableColumns.RESTAURANT_ID, restRowId1);
        restList.put(MetadataDatabase.RestaurantListTableColumns.LOCATION_ID, locationRowId);
        long restListRowId1 = RestaurantListDBHelper.insert(db, restList);
        assertTrue(restListRowId1 > 0);

        ContentValues restList2 = new ContentValues();
        restList2.put(MetadataDatabase.RestaurantListTableColumns.RESTAURANT_ID, restRowId2);
        restList2.put(MetadataDatabase.RestaurantListTableColumns.LOCATION_ID, locationRowId);
        long restListRowId2 = RestaurantListDBHelper.insert(db, restList2);
        assertTrue(restListRowId2 > 0);
        assertTrue(restListRowId2 > restListRowId1);
    }

    @Test
    public void insertOrUpdateTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();

        long locationRowId = UnitTestUtils.insertLocation(44.994, -110.324);
        long restRowId = UnitTestUtils.insertRestaurant(123, "Taco Bell", "Fast food");

        ContentValues restList = new ContentValues();
        restList.put(MetadataDatabase.RestaurantListTableColumns.RESTAURANT_ID, restRowId);
        restList.put(MetadataDatabase.RestaurantListTableColumns.LOCATION_ID, locationRowId);
        restList.putNull(MetadataDatabase.RestaurantListTableColumns.IS_DIRTY);

        // First call should be an insert, isDirty should be false.
        long restListRowId1 = RestaurantListDBHelper.insertOrUpdate(db, restList);
        assertTrue(restListRowId1 > 0);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId1, false);

        // Set isDirty to true and do insertOrUpdate.
        restList.put(MetadataDatabase.RestaurantListTableColumns.IS_DIRTY, true);
        long restListRowId2 = RestaurantListDBHelper.insertOrUpdate(db, restList);
        assertEquals(restListRowId1, restListRowId2);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId2, true);
    }

    @Test
    public void markRowsDirtyTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();

        long locationRowId1 = UnitTestUtils.insertLocation(44.994, -110.324);
        long locationRowId2 = UnitTestUtils.insertLocation(30.224, -90.482);
        long restRowId1 = UnitTestUtils.insertRestaurant(123, "Taco Bell", "Fast food");
        long restRowId2 = UnitTestUtils.insertRestaurant(453, "Jimmy John's", "Sandwiches");
        long restRowId3 = UnitTestUtils.insertRestaurant(584, "Panera Bread", "Soups, Salads, and Sandwiches");
        long restListRowId1 = UnitTestUtils.insertRestaurantList(restRowId1, locationRowId1);
        long restListRowId2 = UnitTestUtils.insertRestaurantList(restRowId2, locationRowId1);
        long restListRowId3 = UnitTestUtils.insertRestaurantList(restRowId3, locationRowId2);

        // Verify that all rows aren't marked as dirty before calling RestaurantListDBHelper.markRowsDiry().
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId1, false);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId2, false);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId3, false);

        RestaurantListDBHelper.markRowsDiry(db, locationRowId1);
        // Verify that the 2 entries under locationRowId1 are dirty, but the one under locationRowId2 isn't.
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId1, true);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId2, true);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId3, false);
    }

    @Test
    public void deleteRowsDirtyTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();

        long locationRowId1 = UnitTestUtils.insertLocation(44.994, -110.324);
        long locationRowId2 = UnitTestUtils.insertLocation(30.224, -90.482);
        long restRowId1 = UnitTestUtils.insertRestaurant(123, "Taco Bell", "Fast food");
        long restRowId2 = UnitTestUtils.insertRestaurant(453, "Jimmy John's", "Sandwiches");
        long restRowId3 = UnitTestUtils.insertRestaurant(584, "Panera Bread", "Soups, Salads, and Sandwiches");
        long restListRowId1 = UnitTestUtils.insertRestaurantList(restRowId1, locationRowId1);
        long restListRowId2 = UnitTestUtils.insertRestaurantList(restRowId2, locationRowId1);
        long restListRowId3 = UnitTestUtils.insertRestaurantList(restRowId3, locationRowId2);

        // Verify that all rows aren't marked as dirty before calling RestaurantListDBHelper.markRowsDiry().
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId1, false);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId2, false);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId3, false);

        long rowsUpdated = RestaurantListDBHelper.markRowsDiry(db, locationRowId1);
        assertEquals(2, rowsUpdated);
        // Verify that the 2 entries under locationRowId1 are dirty, but the one under locationRowId2 isn't.
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId1, true);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId2, true);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId3, false);

        long rowsDeleted = RestaurantListDBHelper.deleteDirtyRows(db, locationRowId1);
        assertEquals(2, rowsDeleted);
        assertEquals(BaseDBHelper.ROW_NOT_FOUND, RestaurantListDBHelper.findRowId(db, restRowId1, locationRowId1));
        assertEquals(BaseDBHelper.ROW_NOT_FOUND, RestaurantListDBHelper.findRowId(db, restRowId2, locationRowId1));
        assertEquals(restListRowId3, RestaurantListDBHelper.findRowId(db, restRowId3, locationRowId2));
    }


}
