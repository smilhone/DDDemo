package com.smilhone.doordashdemo.transport;

/**
 * Created by smilhone on 11/20/2017.
 */

public interface DataWriter {
    void beforeWriteData();

    void writeData(DataFetcher.FetchedData data);

    void afterWriteData(Throwable throwable);
}
