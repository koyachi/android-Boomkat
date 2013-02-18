package org.buffr.boomkat;

oneway interface ICommandCallback {
    void onSearchResponseStart(int count);
    void onSearchResponseEachRecotd(int index, String title);
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
