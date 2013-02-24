package org.buffr.boomkat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;

import org.buffr.boomkat.R;
import org.buffr.boomkat.data.Record;

public class RecordDetailActivity extends Activity {
    private static final String TAG = RecordDetailActivity.class.getSimpleName();

    public static final String PARAM_RECORD_ID = "record_id";

    private String recordId;

    // UI
    private Handler handler = new Handler();
    private ProgressDialog recordInfoWaitDialog;
    private boolean shouldShowRecordInfoWaitDialog;

    private IBoomkatService serviceStub = null;
    private ICommandCallback callback = new ICommandCallback.Stub() {
        @Override
        public void onSearchResponseStart(int count) {}
        @Override
        public void onSearchResponseEachRecord(int index, Record record) {}
        @Override
        public void onSearchResponseEnd() {}

        @Override
        public void onRecordInfoResponseStart() {
            Log.d(TAG, "onRecordInfoReponseStart");
        }
        @Override
        public void onRecordInfoResponseBody(int index, Record record) {
            Log.d(TAG, "onRecordInfoReponseBody");
        }
        @Override
        public void onRecordInfoResponseRecordsByTheSameLabel(int index, Record record) {
            Log.d(TAG, "onRecordInfoReponseRecordsByTheSameLabel");
        }
        @Override
        public void onRecordInfoResponseRecordsAlsoBought(int index, Record record) {
            Log.d(TAG, "onRecordInfoReponseRecordsAlsoBought");
        }
        @Override
        public void onRecordInfoResponseRecordsByTheSameArtist(int index, Record record) {
            Log.d(TAG, "onRecordInfoReponseRecordsByTheSameArtist");
        }
        @Override
        public void onRecordInfoResponseRecordsYouMightLike(int index, Record record) {
            Log.d(TAG, "onRecordInfoReponseRecordsYouMightLike");
        }
        @Override
        public void onRecordInfoResponseEnd() {
            Log.d(TAG, "onRecordInfoReponseEnd");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    initListView();
                    recordInfoWaitDialog.dismiss();
                }
            });
        }

        @Override
        public void onDownloadTrackResponseStart() {}
        @Override
        public void onDownloadTrackResponseEnd() {}

        @Override
        public void onDownloadRecordResponseStart() {}
        @Override
        public void onDownloadRecordResponseEachTrack(int index) {}
        @Override
        public void onDownloadRecordResponseEnd() {}
    };

    private boolean isServiceBound = false;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            if (shouldShowRecordInfoWaitDialog) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        recordInfoWaitDialog.show();
                        shouldShowRecordInfoWaitDialog = false;
                    }
                });
            }
            serviceStub = IBoomkatService.Stub.asInterface(service);
            try {
                serviceStub.registerCallback(callback);
                serviceStub.recordInfo(recordId);
            } catch(RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            serviceStub = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_detail);

        Intent intent = getIntent();
        if (intent != null) {
            recordId = intent.getStringExtra(PARAM_RECORD_ID);
        } else {
            recordId = "";
        }
        Log.d(TAG, "recordId = " + recordId);

        shouldShowRecordInfoWaitDialog = true;
        recordInfoWaitDialog = new ProgressDialog(this);
        recordInfoWaitDialog.setTitle("aa");
        recordInfoWaitDialog.setMessage("bb");
        recordInfoWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        isServiceBound = bindService(new Intent(this, BoomkatService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        try {
            if (serviceStub != null)
                serviceStub.unregisterCallback(callback);
        } catch(RemoteException e) {
            // igonre.
        }
        if (serviceConnection != null && isServiceBound)
            unbindService(serviceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        isServiceBound = false;
    }

}
