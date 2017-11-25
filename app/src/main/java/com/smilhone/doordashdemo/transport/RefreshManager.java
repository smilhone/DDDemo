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
     * @param refreshTask The refresh task to schedule.
     * @param uriToNotify The content Uri to notify when the refresh completes.
     * @param contentProvider The ContentProvider to update when the refresh completes.
     * @return
     */
    public boolean scheduleRefresh(RefreshTask refreshTask, Uri uriToNotify, ContentProvider contentProvider) {
        if (refreshTask == null) {
            return false;
        }

        boolean scheduleRefresh = false;
        String refreshTaskKey = refreshTask.getRefreshTaskKey();
        synchronized (sLockObject) {
            if (!mRunningRefreshTasks.containsKey(refreshTaskKey)) {
                scheduleRefresh = true;
                mRunningRefreshTasks.put(refreshTaskKey, refreshTask);
            }
        }

        if (scheduleRefresh) {
            refreshTask.refresh(new RefreshManagerCallback(refreshTaskKey, uriToNotify, contentProvider));

            ContentValues values = new ContentValues();
            values.put(MetadataDatabase.PropertyTableColumns.LAST_SYNC_TIME, System.currentTimeMillis());
            values.put(MetadataDatabase.PropertyTableColumns.SYNC_STATUS, PropertySyncState.REFRESHING.integerValue());
            contentProvider.update(uriToNotify, values, null, null);
        }
        return scheduleRefresh;
    }

    private class RefreshManagerCallback implements RefreshTask.RefreshTaskCallback {
        private String mRefreshTaskKey;
        private Uri mUriToNotify;
        private ContentProvider mContentProvider;

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
