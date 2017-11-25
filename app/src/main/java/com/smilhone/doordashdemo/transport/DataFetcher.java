package com.smilhone.doordashdemo.transport;

import android.content.ContentValues;

import java.util.List;

/**
 * Created by smilhone on 11/20/2017.
 */

public interface DataFetcher {
    void fetchNextBatch(DataFetcherCallback callback);

    interface DataFetcherCallback {
        void success(FetchedData data);
        void failure(Throwable ex);
    }

    class FetchedData {
        private ContentValues mItemToUpdate;
        private List<ContentValues> mChildren;
        private boolean mFetchNextPage;

        public FetchedData(ContentValues itemToUpdate, List<ContentValues> children, boolean fetchNextPage) {
            mItemToUpdate = itemToUpdate;
            mChildren = children;
            mFetchNextPage = fetchNextPage;
        }

        public boolean shouldFetchNextPage() {
            return mFetchNextPage;
        }

        public ContentValues getItemToUpdate() {
            return mItemToUpdate;
        }

        public List<ContentValues> getChildren() {
            return mChildren;
        }
    }
}
