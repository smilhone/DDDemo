package com.smilhone.doordashdemo.database;

/**
 * Sync state for PropertyTableColumn.SYNC_STATE
 *
 * Created by smilhone on 11/21/2017.
 */

public enum PropertySyncState {
    /**
     * Nothing in cache, default state.
     */
    NOT_IN_CACHE(0),

    /**
     * Refresh in progress.
     */
    REFRESHING(1),

    /**
     * Refresh completed.
     */
    REFRESH_COMPLETE(2),

    /**
     * Refresh failed.
     */
    REFRESH_FAILED(3);

    private final int mSyncState;

    /**
     * Constructor.
     *
     * @param syncState The sync state.
     */
    PropertySyncState(int syncState) {
        mSyncState = syncState;
    }

    /**
     * Converts an int into PropertySyncState.
     *
     * @param syncState The sync state.
     */
    public static PropertySyncState fromInt(int syncState) {
        switch (syncState) {
            case 0:
                return NOT_IN_CACHE;
            case 1:
                return REFRESHING;
            case 2:
                return REFRESH_COMPLETE;
            case 3:
                return REFRESH_FAILED;
            default:
                return NOT_IN_CACHE;
        }
    }

    /**
     * Gets the integer value.
     *
     * @return The integer value for the PropertySyncState enum.
     */
    public Integer integerValue() {
        return mSyncState;
    }
}
