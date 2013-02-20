package org.buffr.boomkat;

import org.buffr.boomkat.data.Record;

oneway interface ICommandCallback {
    void onSearchResponseStart(int count);
    void onSearchResponseEachRecord(int index, in Record record);
    void onSearchResponseEnd();
    // void onError();

    void onRecordInfoReponseStart();
    void onRecordInfoReponseEnd();

    void onDownloadTrackResponseStart();
    void onDownloadTrackResponseEnd();

    void onDownloadRecordResponseStart();
    void onDownloadRecordResponseEachTrack(int index);
    void onDownloadRecordResponseEnd();
}
