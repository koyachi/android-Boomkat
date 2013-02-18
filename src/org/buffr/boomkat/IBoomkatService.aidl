package org.buffr.boomkat;

import org.buffr.boomkat.ICommandCallback;

interface IBoomkatService {
    void registerCallback(ICommandCallback cb);
    void unregisterCallback(ICommandCallback cb);

    void search(String word);
    void recordInfo(String recordId);
    void downloadTrack(String recordId, String trackId);
    void downloadRecord(String recordId);
}
