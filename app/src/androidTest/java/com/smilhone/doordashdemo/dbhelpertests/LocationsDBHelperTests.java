package com.smilhone.doordashdemo.dbhelpertests;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.smilhone.doordashdemo.UnitTestUtils;
import com.smilhone.doordashdemo.common.CursorUtils;
import com.smilhone.doordashdemo.database.LocationsDBHelper;
import com.smilhone.doordashdemo.database.MetadataDatabase;
import com.smilhone.doordashdemo.database.PropertySyncState;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for LocationsDBHelper
 *
 * Created by smilhone on 11/26/2017.
 */
@RunWith(AndroidJUnit4.class)
public class LocationsDBHelperTests {

    @Before
    public void setup() {
        UnitTestUtils.cleanDatabase();
    }

    @After
    public void cleanup() {
        UnitTestUtils.cleanDatabase();
    }

    @Test
    public void testInsertLocation() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();
        double latitude = 37.422740;
        double longitude = -122.339956;

        long locationRowId = LocationsDBHelper.insertRow(db, latitude, longitude);
        assertTrue(locationRowId > 0);
    }

    @Test
    public void testUpdateLocation() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = MetadataDatabase.getInstance(appContext).getWritableDatabase();
        double latitude = 37.422740;
        double longitude = -122.339956;

        long locationRowId = LocationsDBHelper.insertRow(db, latitude, longitude);
        assertTrue(locationRowId > 0);

        ContentValues values = new ContentValues();
        values.put(MetadataDatabase.PropertyTableColumns.SYNC_STATUS, PropertySyncState.REFRESHING.integerValue());
        long rowsUpdated = LocationsDBHelper.updateRow(db, values, latitude, longitude);
        assertEquals(1, rowsUpdated);

        Cursor cursor = null;
        try {
            cursor = LocationsDBHelper.getPropertyCursor(db, latitude, longitude);
            assertTrue(cursor.moveToFirst());
            PropertySyncState syncState = PropertySyncState.fromInt(
                    cursor.getInt(cursor.getColumnIndex(MetadataDatabase.PropertyTableColumns.SYNC_STATUS)));
            assertEquals(PropertySyncState.REFRESHING, syncState);
        } finally {
            CursorUtils.closeQuietly(cursor);
        }
    }
}
