package com.smilhone.doordashdemo.transport;

import com.smilhone.doordashdemo.transport.serialization.RestaurantListItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by smilhone on 11/17/2017.
 */

public interface DoorDashService {

    @Headers("Accept: application/json")
    @GET("/v2/restaurant/")
    Call<List<RestaurantListItem>> getRestaurantList(@Query("lat") double latitude, @Query("lng") double longitude);
}
