package com.smilhone.doordashdemo.transport;

/**
 * DataWriter is an interface for writing data from a refresh task.
 *
 * Created by smilhone on 11/20/2017.
 */
public interface DataWriter {
    /**
     * Called before any data is written.
     */
    void beforeWriteData();

    /**
     * Called as data is retrieved from the refresh task.
     *
     * @param data The data that should be written.
     */
    void writeData(DataFetcher.FetchedData data);

    /**
     * Called after all data has been written.
     *
     * @param throwable Any error that occurred.
     */
    void afterWriteData(Throwable throwable);
}
