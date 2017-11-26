package com.smilhone.doordashdemo.transport;

/**
 * RefreshTask is responsible for refreshing data.
 *
 * Created by smilhone on 11/21/2017.
 */
public class RefreshTask {
    private DataFetcher mDataFetcher;
    private DataWriter mDataWriter;
    private DataFetcherCallback mFetcherCallback = new DataFetcherCallback();
    private String mRefreshTaskKey;
    private RefreshTaskCallback mCallback;

    interface RefreshTaskCallback {
        void success();
        void failure(Throwable ex);
    }

    /**
     * Constructor.
     * @param dataFetcher The data fetcher for the refresh task.
     * @param dataWriter The data writer for the refresh task.
     * @param refreshTaskKey The key for the refresh task.  Refresh tasks for the same item should use the same key.
     */
    public RefreshTask(DataFetcher dataFetcher, DataWriter dataWriter, String refreshTaskKey) {
        mDataFetcher = dataFetcher;
        mDataWriter = dataWriter;
        mRefreshTaskKey = refreshTaskKey;
    }

    /**
     * Starts the refresh task.
     */
    public void refresh(RefreshTaskCallback callback) {
        mCallback = callback;

        mDataWriter.beforeWriteData();
        mDataFetcher.fetchNextBatch(mFetcherCallback);
    }

    /**
     * Gets the key for the refresh task.
     */
    public String getRefreshTaskKey() {
        return mRefreshTaskKey;
    }

    /**
     * Passes data from the data fetcher to the data writer.  Schedules subsequent page requests if needed.
     * @param data The data from the data fetcher.
     */
    private void processData(DataFetcher.FetchedData data) {
        mDataWriter.writeData(data);
        if (data.shouldFetchNextPage()) {
            mDataFetcher.fetchNextBatch(mFetcherCallback);
        } else {
            mDataWriter.afterWriteData(null);
            mCallback.success();
        }
    }

    /**
     * Passes the error from the data fetcher to the data writer.
     * @param ex The exception encountered.
     */
    private void processError(Throwable ex) {
        mDataWriter.afterWriteData(ex);
        mCallback.failure(ex);
    }

    /**
     * Callback for the DataFetcher.
     */
    private class DataFetcherCallback implements DataFetcher.DataFetcherCallback {

        @Override
        public void success(DataFetcher.FetchedData data) {
            processData(data);
        }

        @Override
        public void failure(Throwable ex) {
            processError(ex);
        }
    }
}
