package org.buffr.boomkat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

public class BoomkatService extends Service {
    private String TAG = BoomkatService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return service;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "onStart");
    }

    private ICommandCallback callback;
    private final IBoomkatService.Stub service = new IBoomkatService.Stub() {
        @Override
        public void registerCallback(ICommandCallback cb) {
            synchronized(BoomkatService.this) {
                // TODO: dummyCallback?
                callback = cb;
            }
        }

        @Override
        public void unregisterCallback(ICommandCallback cb) {
            synchronized(BoomkatService.this) {
                // TODO: dummyCallback?
                callback = null;
            }
        }

        @Override
        public void search(String word) {
            Log.d(TAG, "search(" + word + ")");
            synchronized(BoomkatService.this) {
                Command command = new Command();
                ArrayList<String> args = new ArrayList<String>();
                args.add("search");
                //args.add("'Tim Hecker'");
                args.add("'" + word + "'");
                command.start(args);
            }
        }

        @Override
        public void recordInfo(String recordId) {
        }

        @Override
        public void downloadTrack(String recordId, String trackId) {
        }

        @Override
        public void downloadRecord(String recordId) {
        }
    };

    /*
    public void onSearchResponseStart(int count) {
        if (callback != null) callback.onSearchResponStart();
    }
    public void onSearchResponseEachRecotd(int index, String title) {
    }
    public void onSearchResponseEnd() {
    }
    // void onError();

    public void onRecordInfoReponseStart() {
    }
    public void onRecordInfoReponseEnd() {
    }

    public void onDownloadTrackResponseStart() {
    }
    public void onDownloadTrackResponseEnd() {
    }

    public void onDownloadRecordResponseStart() {
    }
    public void onDownloadRecordResponseEachTrack(int index) {
    }
    public void onDownloadRecordResponseEnd() {
    }
    */
}
