package com.smilhone.doordashdemo.viewmodels;

/**
 * State depicting the state of the list of restaurants.
 *
 * Created by smilhone on 11/26/2017.
 */

public enum RestaurantListViewState {
    /**
     * Indicates the progress bar should be shown.
     */
    LOADING,
    /**
     * Indicates the empty message text should be shown.
     */
    SHOW_EMPTY_MESSAGE,
    /**
     * Indicates the list of restaurants should be shown.
     */
    SHOW_LIST
}
