package com.smilhone.doordashdemo;

import android.content.ContentValues;
import android.support.test.runner.AndroidJUnit4;

import com.smilhone.doordashdemo.database.MetadataDatabase;
import com.smilhone.doordashdemo.transport.DataFetcher;
import com.smilhone.doordashdemo.transport.fetchers.RestaurantListDataFetcher;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.*;
/**
 * Created by smilhone on 11/20/2017.
 */
@RunWith(AndroidJUnit4.class)
public class DoorDashServiceTest {
    @Test
    public void testDataFetcher() throws IOException {
        ContentValues values = new ContentValues();
        values.put(MetadataDatabase.LocationsTableColumns.LATITUDE, 37.422740);
        values.put(MetadataDatabase.LocationsTableColumns.LONGITUDE, -122.139956);
        RestaurantListDataFetcher dataFetcher = new RestaurantListDataFetcher(values);
        dataFetcher.fetchNextBatch(new DataFetcher.DataFetcherCallback() {
            @Override
            public void success(DataFetcher.FetchedData data) {

            }

            @Override
            public void failure(Throwable ex) {
                assertTrue(false);
            }
        });
    }
}
