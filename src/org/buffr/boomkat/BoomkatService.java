package org.buffr.boomkat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import org.buffr.boomkat.data.Record;

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
                Command command = new Command(BoomkatService.this);
                ArrayList<String> args = new ArrayList<String>();
                args.add("search");
                //args.add("'Tim Hecker'");
                args.add("'" + word + "'");
                command.start(args);
            }
        }

        @Override
        public void recordInfo(String recordId) {
            Log.d(TAG, "recordInfo(" + recordId + ")");
            synchronized(BoomkatService.this) {
                Command command = new Command(BoomkatService.this);
                ArrayList<String> args = new ArrayList<String>();
                args.add("info");
                //args.add("467910");
                args.add(recordId);
                command.start(args);
            }
        }

        @Overridep
        public void downloadTrack(String recordId, String trackId) {
        }

        @Override
        public void downloadRecord(String recordId) {
        }
    };

    public void onSearchResponseStart(int count) {
        if (callback == null) {
            return;
        }
        try {
            callback.onSearchResponseStart(count);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void onSearchResponseEachRecord(int index, Record record) {
        if (callback == null) {
            return;
        }
        try {
            callback.onSearchResponseEachRecord(index, record);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void onSearchResponseEnd() {
        if (callback == null) {
            return;
        }
        try {
            callback.onSearchResponseEnd();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    // void onError();

    public void onRecordInfoResponseStart() {
        if (callback == null) {
            return;
        }
        try {
            callback.onRecordInfoResponseStart();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void onRecordInfoResponseBody(int index, Record record) {
        if (callback == null) {
            return;
        }
        try {
            callback.onRecordInfoResponseBody(index, record);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void onRecordInfoResponseRecordsByTheSameLabel(int index, Record record) {
        if (callback == null) {
            return;
        }
        try {
            callback.onRecordInfoResponseRecordsByTheSameLabel(index, record);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void onRecordInfoResponseRecordsAlsoBought(int index, Record record) {
        if (callback == null) {
            return;
        }
        try {
            callback.onRecordInfoResponseRecordsAlsoBought(index, record);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void onRecordInfoResponseRecordsByTheSameArtist(int index, Record record) {
        if (callback == null) {
            return;
        }
        try {
            callback.onRecordInfoResponseRecordsByTheSameArtist(index, record);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void onRecordInfoResponseRecordsYouMightLike(int index, Record record) {
        if (callback == null) {
            return;
        }
        try {
            callback.onRecordInfoResponseRecordsYouMightLike(index, record);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    public void onRecordInfoResponseEnd() {
        if (callback == null) {
            return;
        }
        try {
            callback.onRecordInfoResponseEnd();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
}
