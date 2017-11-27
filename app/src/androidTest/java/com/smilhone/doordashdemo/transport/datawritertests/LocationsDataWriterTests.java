package com.smilhone.doordashdemo.transport.datawritertests;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.smilhone.doordashdemo.UnitTestUtils;
import com.smilhone.doordashdemo.database.BaseDBHelper;
import com.smilhone.doordashdemo.database.MetadataDatabase;
import com.smilhone.doordashdemo.database.RestaurantListDBHelper;
import com.smilhone.doordashdemo.database.RestaurantsDBHelper;
import com.smilhone.doordashdemo.transport.DataFetcher;
import com.smilhone.doordashdemo.transport.writers.LocationsDataWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for LocationsDataWriter.
 *
 * Created by smilhone on 11/26/2017.
 */
@RunWith(AndroidJUnit4.class)
public class LocationsDataWriterTests {
    @Before
    public void setup() {
        UnitTestUtils.cleanDatabase();
    }

    @After
    public void cleanup() {
        UnitTestUtils.cleanDatabase();
    }

    /**
     * Tests a simple end-to-end flow
     */
    @Test
    public void simpleWriteDataTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        MetadataDatabase metadataDatabase = MetadataDatabase.getInstance(appContext);
        SQLiteDatabase db = metadataDatabase.getWritableDatabase();
        long locationRowId = UnitTestUtils.insertLocation(44.994, -110.324);

        ContentValues location = UnitTestUtils.getLocationFromDb(db, locationRowId);
        LocationsDataWriter dataWriter = new LocationsDataWriter(metadataDatabase, location);
        dataWriter.beforeWriteData();

        ContentValues rest1 = UnitTestUtils.createRestaurant(123, "Taco Bell", "Fast Food");
        ContentValues rest2 = UnitTestUtils.createRestaurant(345, "McDonald's", "Fast Food");
        ContentValues rest3 = UnitTestUtils.createRestaurant( 567, "Chipotle", "Fast Food");
        List<ContentValues> restaurants = new ArrayList<>();
        restaurants.add(rest1);
        restaurants.add(rest2);
        restaurants.add(rest3);

        // Write data, we should see the three restaurants inserted.
        DataFetcher.FetchedData data = new DataFetcher.FetchedData(location, restaurants, false);
        dataWriter.writeData(data);

        // Verify that restaurants and restaurant_list entries have been inserted.
        long restRowId1 = RestaurantsDBHelper.findRestaurantRowId(db, 123);
        long restRowId2 = RestaurantsDBHelper.findRestaurantRowId(db, 345);
        long restRowId3 = RestaurantsDBHelper.findRestaurantRowId(db, 567);
        assertTrue(restRowId1 > 0);
        assertTrue(restRowId2 > 0);
        assertTrue(restRowId3 > 0);

        long restListRowId1 = RestaurantListDBHelper.findRowId(db, restRowId1, locationRowId);
        long restListRowId2 = RestaurantListDBHelper.findRowId(db, restRowId2, locationRowId);
        long restListRowId3 = RestaurantListDBHelper.findRowId(db, restRowId3, locationRowId);
        assertTrue(restListRowId1 > 0);
        assertTrue(restListRowId2 > 0);
        assertTrue(restListRowId3 > 0);

        dataWriter.afterWriteData(null);

        // Verify that afterDataWrite didn't delete the records that were inserted.
        assertEquals(restListRowId1, RestaurantListDBHelper.findRowId(db, restRowId1, locationRowId));
        assertEquals(restListRowId2, RestaurantListDBHelper.findRowId(db, restRowId2, locationRowId));
        assertEquals(restListRowId3, RestaurantListDBHelper.findRowId(db, restRowId3, locationRowId));
    }

    /**
     * Test beforeDataWrite marks restaurant_list items dirty and afterDataWrite deletes dirty records.
     * Verifies that locations unrelated to the one LocationDataWriter is updating are unaffected.
     */
    @Test
    public void testDeletingDirtyItems() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        MetadataDatabase metadataDatabase = MetadataDatabase.getInstance(appContext);
        SQLiteDatabase db = metadataDatabase.getWritableDatabase();
        long locationRowId1 = UnitTestUtils.insertLocation(44.994, -110.324);
        long locationRowId2 = UnitTestUtils.insertLocation(30.224, -90.482);
        long restRowId1 = UnitTestUtils.insertRestaurant(123, "Taco Bell", "Fast food");
        long restRowId2 = UnitTestUtils.insertRestaurant(453, "Jimmy John's", "Sandwiches");
        long restRowId3 = UnitTestUtils.insertRestaurant(584, "Panera Bread", "Soups, Salads, and Sandwiches");
        long restListRowId1 = UnitTestUtils.insertRestaurantList(restRowId1, locationRowId1);
        long restListRowId2 = UnitTestUtils.insertRestaurantList(restRowId2, locationRowId1);
        long restListRowId3 = UnitTestUtils.insertRestaurantList(restRowId3, locationRowId2);

        ContentValues location = UnitTestUtils.getLocationFromDb(db, locationRowId1);
        LocationsDataWriter dataWriter = new LocationsDataWriter(metadataDatabase, location);

        // beforeDataWrite should mark restListRowId1 and restListRowId2 as dirty.
        dataWriter.beforeWriteData();
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId1, true);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId2, true);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId3, false);

        // No children for the location
        DataFetcher.FetchedData fetchedData = new DataFetcher.FetchedData(location, new ArrayList<ContentValues>(), false);
        dataWriter.writeData(fetchedData);

        // No records were updated, we should see 2 records deleted.  restListRowId3 should be untouched.
        dataWriter.afterWriteData(null);
        assertEquals(BaseDBHelper.ROW_NOT_FOUND, RestaurantListDBHelper.findRowId(db, restRowId1, locationRowId1));
        assertEquals(BaseDBHelper.ROW_NOT_FOUND, RestaurantListDBHelper.findRowId(db, restRowId2, locationRowId1));
        assertEquals(restListRowId3, RestaurantListDBHelper.findRowId(db, restRowId3, locationRowId2));
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId3, false);
    }

    /**
     * Verify that afterDataWrite doesn't delete dirty records if an error occurred.
     */
    @Test
    public void afterWriteDataWithErrorTest() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        MetadataDatabase metadataDatabase = MetadataDatabase.getInstance(appContext);
        SQLiteDatabase db = metadataDatabase.getWritableDatabase();
        long locationRowId1 = UnitTestUtils.insertLocation(44.994, -110.324);
        long locationRowId2 = UnitTestUtils.insertLocation(30.224, -90.482);
        long restRowId1 = UnitTestUtils.insertRestaurant(123, "Taco Bell", "Fast food");
        long restRowId2 = UnitTestUtils.insertRestaurant(453, "Jimmy John's", "Sandwiches");
        long restRowId3 = UnitTestUtils.insertRestaurant(584, "Panera Bread", "Soups, Salads, and Sandwiches");
        long restListRowId1 = UnitTestUtils.insertRestaurantList(restRowId1, locationRowId1);
        long restListRowId2 = UnitTestUtils.insertRestaurantList(restRowId2, locationRowId1);
        long restListRowId3 = UnitTestUtils.insertRestaurantList(restRowId3, locationRowId2);

        ContentValues location = UnitTestUtils.getLocationFromDb(db, locationRowId1);
        LocationsDataWriter dataWriter = new LocationsDataWriter(metadataDatabase, location);

        // beforeDataWrite should mark restListRowId1 and restListRowId2 as dirty.
        dataWriter.beforeWriteData();
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId1, true);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId2, true);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId3, false);

        // No children for the location
        DataFetcher.FetchedData fetchedData = new DataFetcher.FetchedData(location, new ArrayList<ContentValues>(), false);
        dataWriter.writeData(fetchedData);

        dataWriter.afterWriteData(new Exception("Random exception"));
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId1, true);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId2, true);
        UnitTestUtils.verifyIsDirtyFlag(db, restListRowId3, false);
    }

}
