package com.smilhone.doordashdemo.transport;

import com.smilhone.doordashdemo.transport.serialization.RestaurantListItem;
import com.smilhone.doordashdemo.transport.serialization.TokenResponse;
import com.smilhone.doordashdemo.transport.serialization.TokenRequestBody;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * The DoorDashService API
 *
 * Created by smilhone on 11/17/2017.
 */

public interface DoorDashService {

    @Headers("Accept: application/json")
    @GET("/v2/restaurant/")
    Call<List<RestaurantListItem>> getRestaurantList(@Query("lat") double latitude, @Query("lng") double longitude);

    @Headers("Accept: application/json")
    @POST("/v2/auth/token/")
    Call<TokenResponse> getAuthToken(@Body TokenRequestBody body);
}
