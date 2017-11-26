package com.smilhone.doordashdemo.transport;

import android.content.ContentValues;

import java.util.List;

/**
 * Interface for fetching data.
 *
 * Created by smilhone on 11/20/2017.
 */

public interface DataFetcher {
    /**
     * Fetch the next batch of data.
     *
     * @param callback The DataFetcherCallback to invoke when the data has been retrieved.
     */
    void fetchNextBatch(DataFetcherCallback callback);

    /**
     * Callback interface when data has been retrieved.
     */
    interface DataFetcherCallback {
        /**
         * Callback invoked when data is successfully retrieved.
         *
         * @param data The data.
         */
        void success(FetchedData data);

        /**
         * Callback invoked when an error occurred fetching data.
         *
         * @param ex The error that occurred.
         */
        void failure(Throwable ex);
    }

    /**
     * The data that was fetched.  Contains information about the item that was refreshed, any child data associated with it,
     * and a flag indicating if there's another page of data to retrieve.
     */
    class FetchedData {
        private ContentValues mItemToUpdate;
        private List<ContentValues> mChildren;
        private boolean mFetchNextPage;

        /**
         * Constructor.
         *
         * @param itemToUpdate The item that was refreshed.
         * @param children Any child items that were retrieved as part of refreshing itemToUpdate.
         * @param fetchNextPage Flag indicating if there is another page of data to retrieve.
         */
        public FetchedData(ContentValues itemToUpdate, List<ContentValues> children, boolean fetchNextPage) {
            mItemToUpdate = itemToUpdate;
            mChildren = children;
            mFetchNextPage = fetchNextPage;
        }

        /**
         * Gets a flag indicating if there's another page to fetch.
         *
         * @return True if there's another page to fetch, false otherwise.
         */
        public boolean shouldFetchNextPage() {
            return mFetchNextPage;
        }

        /**
         * Gets the item that was refreshed.
         *
         * @return The item that was refreshed.
         */
        public ContentValues getItemToUpdate() {
            return mItemToUpdate;
        }

        /**
         * Gets the list of children that were fetched as part of refreshing the parent item.
         *
         * @return The children items that were fetched.
         */
        public List<ContentValues> getChildren() {
            return mChildren;
        }
    }
}
