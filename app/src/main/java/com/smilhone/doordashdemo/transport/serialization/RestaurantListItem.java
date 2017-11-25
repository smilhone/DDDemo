package com.smilhone.doordashdemo.transport.serialization;

import android.content.ContentValues;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import com.smilhone.doordashdemo.database.MetadataDatabase;

/**
 * Created by stephmil on 11/17/2017.
 */

public class RestaurantListItem {
    @SerializedName("id")
    public String Id;

    @SerializedName("name")
    public String Name;

    @SerializedName("description")
    public String Description;

    @SerializedName("cover_img_url")
    public String CoverImageUrl;

    @SerializedName("is_newly_added")
    public String IsNewlyAdded;

    @SerializedName("is_time_surging")
    public String IsTimeSurging;

    @SerializedName("status")
    public String Status;

    @SerializedName("delivery_fee")
    public String DeliveryFee;

    @SerializedName("number_of_ratings")
    public String NumberOfRatings;

    @SerializedName("price_range")
    public String PriceRange;

    public ContentValues toContentValues() {
        if (TextUtils.isEmpty(Name)) {
            return null;
        }

        ContentValues values = new ContentValues();
        values.put(MetadataDatabase.RestaurantsTableColumns.REST_ID, Id);
        values.put(MetadataDatabase.RestaurantsTableColumns.NAME, Name);
        values.put(MetadataDatabase.RestaurantsTableColumns.DESCRIPTION, Description);
        values.put(MetadataDatabase.RestaurantsTableColumns.COVER_IMG_URL, CoverImageUrl);
        values.put(MetadataDatabase.RestaurantsTableColumns.IS_NEWLY_ADDED, IsNewlyAdded);
        values.put(MetadataDatabase.RestaurantsTableColumns.IS_TIME_SURGING, IsTimeSurging);
        values.put(MetadataDatabase.RestaurantsTableColumns.NUM_RATINGS, NumberOfRatings);
        values.put(MetadataDatabase.RestaurantsTableColumns.PRICE_RANGE, PriceRange);

        return values;
    }
}
