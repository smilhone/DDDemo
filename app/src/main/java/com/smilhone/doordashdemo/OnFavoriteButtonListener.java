package com.smilhone.doordashdemo;

import android.content.ContentValues;
import android.content.Context;

/**
 * Defines a click listener for the "favorite button" on restaurant_item.
 *
 * Created by smilhone on 11/25/2017.
 */

public interface OnFavoriteButtonListener {
    /**
     * Invoked when an item is clicked.
     *
     * @param context The application context.
     * @param itemClicked The item clicked.
     */
    void onFavoriteButtonClicked(Context context, ContentValues itemClicked);
}
