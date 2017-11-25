package com.smilhone.doordashdemo.transport.serialization;

import com.google.gson.annotations.SerializedName;

import java.util.Collection;
import java.util.List;

/**
 * Created by smilhone on 11/17/2017.
 */

public class RestaurantList {
    @SerializedName("")
    public List<RestaurantListItem> Restaurants;
}
