package com.smilhone.doordashdemo.transport.fetchers;

import android.content.ContentValues;

import com.smilhone.doordashdemo.database.MetadataDatabase;
import com.smilhone.doordashdemo.transport.DataFetcher;
import com.smilhone.doordashdemo.transport.DoorDashService;
import com.smilhone.doordashdemo.transport.serialization.RestaurantListItem;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * RestaurantListDataFetcher loads the list of restaurants for a given location.
 *
 * Created by stephmil on 11/20/2017.
 */

public class RestaurantListDataFetcher implements DataFetcher {
    private ContentValues mLocationToRefresh;

    /**
     * Constructor.
     *
     * @param locationToRefresh The location to refresh.  Must contain the location's longitude/latitude.
     */
    public RestaurantListDataFetcher(ContentValues locationToRefresh) {
        mLocationToRefresh = locationToRefresh;
    }

    @Override
    public void fetchNextBatch(DataFetcherCallback callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .client(getOkHttpClient())
                .baseUrl("https://api.doordash.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        DoorDashService service = retrofit.create(DoorDashService.class);
        double latitutde = mLocationToRefresh.getAsDouble(MetadataDatabase.LocationsTableColumns.LATITUDE);
        double longitude = mLocationToRefresh.getAsDouble(MetadataDatabase.LocationsTableColumns.LONGITUDE);
        service.getRestaurantList(latitutde, longitude).enqueue(new RestaurantListCallback(callback));
    }

    /**
     * Gets the OkHttp client.
     * @return
     */
    private OkHttpClient getOkHttpClient() {
        final OkHttpClient httpClient = new OkHttpClient();
        return httpClient;
    }

    /**
     * Callback for the network task fetching the list of restaurants.
     */
    class RestaurantListCallback implements Callback<List<RestaurantListItem>> {
        private DataFetcherCallback mCallback;
        RestaurantListCallback(DataFetcherCallback callback) {
            mCallback = callback;
        }

        @Override
        public void onResponse(Call<List<RestaurantListItem>> call, Response<List<RestaurantListItem>> response) {
            List<RestaurantListItem> restaurants = response.body();
            List<ContentValues> children = new ArrayList<>();

            if (restaurants != null) {
                for (RestaurantListItem restaurant : restaurants) {
                    ContentValues child = restaurant.toContentValues();
                    if (child != null) {
                        children.add(child);
                    }
                }
            }
            FetchedData fetchedData = new FetchedData(mLocationToRefresh, children, false);
            mCallback.success(fetchedData);
        }

        @Override
        public void onFailure(Call<List<RestaurantListItem>> call, Throwable t) {
            mCallback.failure(t);
        }
    }
}
