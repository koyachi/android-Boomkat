package org.buffr.boomkat;

import org.buffr.boomkat.data.Record;

oneway interface ICommandCallback {
    void onSearchResponseStart(int count);
    void onSearchResponseEachRecord(int index, in Record record);
    void onSearchResponseEnd();
    // void onError();

    void onRecordInfoResponseStart();
    void onRecordInfoResponseBody(int index, in Record record);
    void onRecordInfoResponseRecordsByTheSameLabel(int index, in Record record);
    void onRecordInfoResponseRecordsAlsoBought(int index, in Record record);
    void onRecordInfoResponseRecordsByTheSameArtist(int index, in Record record);
    void onRecordInfoResponseRecordsYouMightLike(int index, in Record record);
    void onRecordInfoResponseEnd();

    void onDownloadTrackResponseStart();
    void onDownloadTrackResponseEnd();

    void onDownloadRecordResponseStart();
    void onDownloadRecordResponseEachTrack(int index);
    void onDownloadRecordResponseEnd();
}
