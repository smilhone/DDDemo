package com.smilhone.doordashdemo.transport;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;

import com.smilhone.doordashdemo.database.MetadataDatabase;
import com.smilhone.doordashdemo.database.PropertySyncState;

import java.util.HashMap;
import java.util.Map;

/**
 * RefreshManager is responsible for managing refresh tasks and ensuring only 1 task is running for a given key at a time.
 *
 * Created by smilhone on 11/21/2017.
 */
public class RefreshManager {
    private static final Object sLockObject = new Object();
    private static RefreshManager sInstance;
    private Map<String, RefreshTask> mRunningRefreshTasks = new HashMap<>();

    /**
     * Gets the singleton instance of RefreshManager.
     * @return
     */
    public static RefreshManager getInstance() {
        synchronized (sLockObject) {
            if (sInstance == null) {
                sInstance = new RefreshManager();
            }
        }
        return sInstance;
    }

    /**
     * Schedules the refresh task if one isn't already running.
     *
     * @param refreshTask The refresh task to schedule.
     * @param uriToNotify The content Uri to notify when the refresh completes.
     * @param contentProvider The ContentProvider to update when the refresh completes.
     *
     * @return True if the refresh was scheduled, false otherwise.
     */
    public boolean scheduleRefresh(RefreshTask refreshTask, Uri uriToNotify, ContentProvider contentProvider) {
        if (refreshTask == null) {
            return false;
        }

        boolean scheduleRefresh = false;
        String refreshTaskKey = refreshTask.getRefreshTaskKey();
        synchronized (sLockObject) {
            // Only schedule a refresh if one isn't already running for the same key.
            if (!mRunningRefreshTasks.containsKey(refreshTaskKey)) {
                scheduleRefresh = true;
                mRunningRefreshTasks.put(refreshTaskKey, refreshTask);
            }
        }

        if (scheduleRefresh) {
            refreshTask.refresh(new RefreshManagerCallback(refreshTaskKey, uriToNotify, contentProvider));

            // After scheduling the refresh, update the property columns for the item being refreshed.
            ContentValues values = new ContentValues();
            values.put(MetadataDatabase.PropertyTableColumns.LAST_SYNC_TIME, System.currentTimeMillis());
            values.put(MetadataDatabase.PropertyTableColumns.SYNC_STATUS, PropertySyncState.REFRESHING.integerValue());
            contentProvider.update(uriToNotify, values, null, null);
        }
        return scheduleRefresh;
    }

    /**
     * Callback implementation of RefreshTask.RefreshTaskCallback.
     */
    private class RefreshManagerCallback implements RefreshTask.RefreshTaskCallback {
        private String mRefreshTaskKey;
        private Uri mUriToNotify;
        private ContentProvider mContentProvider;

        /**
         * Constructor.
         *
         * @param refreshTaskKey The key for the refresh task.
         * @param uriToNotify The Uri to update when the refresh task finishes.
         * @param contentProvider The content provider to update.
         */
        RefreshManagerCallback(String refreshTaskKey, Uri uriToNotify, ContentProvider contentProvider) {
            mRefreshTaskKey = refreshTaskKey;
            mUriToNotify = uriToNotify;
            mContentProvider = contentProvider;
        }

        @Override
        public void success() {
            synchronized (sLockObject) {
                mRunningRefreshTasks.remove(mRefreshTaskKey);
            }
            ContentValues values = new ContentValues();
            values.put(MetadataDatabase.PropertyTableColumns.LAST_SYNC_TIME, System.currentTimeMillis());
            values.put(MetadataDatabase.PropertyTableColumns.SYNC_STATUS, PropertySyncState.REFRESH_COMPLETE.integerValue());
            mContentProvider.update(mUriToNotify, values, null, null);
        }

        @Override
        public void failure(Throwable ex) {
            synchronized (sLockObject) {
                mRunningRefreshTasks.remove(mRefreshTaskKey);
            }

            ContentValues values = new ContentValues();
            values.put(MetadataDatabase.PropertyTableColumns.LAST_SYNC_TIME, System.currentTimeMillis());
            values.put(MetadataDatabase.PropertyTableColumns.SYNC_STATUS, PropertySyncState.REFRESH_FAILED.integerValue());
            mContentProvider.update(mUriToNotify, values, null, null);
        }
    }
}
