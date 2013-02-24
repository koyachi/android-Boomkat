package org.buffr.boomkat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.buffr.boomkat.R;
import org.buffr.boomkat.data.Record;

public class RecordDetailActivity extends Activity {
    private static final String TAG = RecordDetailActivity.class.getSimpleName();

    public static final String PARAM_RECORD = "record";

    private Record record;
    private String recordId;

    // UI
    private Handler handler = new Handler();
    private ProgressDialog recordInfoWaitDialog;
    private boolean shouldShowRecordInfoWaitDialog;
    private Bitmap coverBmp;

    private ArrayList<Record> recordsByTheSameLabelList = new ArrayList<Record>();
    private ListView recordsByTheSameLabelListView;
    private MyAdapter recordsByTheSameLabelAdapter;

    private ArrayList<Record> recordsAlsoBoughtList = new ArrayList<Record>();
    private ListView recordsAlsoBoughtListView;
    private MyAdapter recordsAlsoBoughtAdapter;

    private ArrayList<Record> recordsByTheSameArtistList = new ArrayList<Record>();
    private ListView recordsByTheSameArtistListView;
    private MyAdapter recordsByTheSameArtistAdapter;

    private ArrayList<Record> recordsYouMightLikeList = new ArrayList<Record>();
    private ListView recordsYouMightLikeListView;
    private MyAdapter recordsYouMightLikeAdapter;

    private static Bitmap loadThumbnail(String thumbnailUrl) {
        URL url = null;
        Bitmap bmp = null;
        try {
            url = new URL(thumbnailUrl);
        } catch(MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        try {
            bmp = BitmapFactory.decodeStream(url.openStream());
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        return bmp;
    }

    private void initView() {
        if (coverBmp != null) {
            ImageView coverImageView = (ImageView)findViewById(R.id.cover);
            coverImageView.setImageBitmap(coverBmp);
        }
        TextView titleTextView = (TextView)findViewById(R.id.title);
        titleTextView.setText(record.title);
        TextView artistTextView = (TextView)findViewById(R.id.artist);
        artistTextView.setText(record.artist);

        recordsByTheSameLabelAdapter.addAll(recordsByTheSameLabelList);
        recordsByTheSameLabelListView = (ListView)findViewById(R.id.lv_by_the_same_label);
        recordsByTheSameLabelListView.setAdapter(recordsByTheSameLabelAdapter);

        recordsAlsoBoughtAdapter.addAll(recordsAlsoBoughtList);
        recordsAlsoBoughtListView = (ListView)findViewById(R.id.lv_also_bought);
        recordsAlsoBoughtListView.setAdapter(recordsAlsoBoughtAdapter);

        recordsByTheSameArtistAdapter.addAll(recordsByTheSameArtistList);
        recordsByTheSameArtistListView = (ListView)findViewById(R.id.lv_by_the_same_artist);
        recordsByTheSameArtistListView.setAdapter(recordsByTheSameArtistAdapter);

        recordsYouMightLikeAdapter.addAll(recordsYouMightLikeList);
        recordsYouMightLikeListView = (ListView)findViewById(R.id.lv_you_might_like);
        recordsYouMightLikeListView.setAdapter(recordsYouMightLikeAdapter);
    }

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
            coverBmp = RecordDetailActivity.loadThumbnail(record.thumbnailUrl);
        }
        @Override
        public void onRecordInfoResponseRecordsByTheSameLabel(int index, Record record) {
            Log.d(TAG, "onRecordInfoReponseRecordsByTheSameLabel");
            recordsByTheSameLabelList.add(record);
            recordsByTheSameLabelAdapter.loadThumbnail(record.thumbnailUrl);
        }
        @Override
        public void onRecordInfoResponseRecordsAlsoBought(int index, Record record) {
            Log.d(TAG, "onRecordInfoReponseRecordsAlsoBought");
            recordsAlsoBoughtList.add(record);
            recordsAlsoBoughtAdapter.loadThumbnail(record.thumbnailUrl);
        }
        @Override
        public void onRecordInfoResponseRecordsByTheSameArtist(int index, Record record) {
            Log.d(TAG, "onRecordInfoReponseRecordsByTheSameArtist");
            recordsByTheSameArtistList.add(record);
            recordsByTheSameArtistAdapter.loadThumbnail(record.thumbnailUrl);
        }
        @Override
        public void onRecordInfoResponseRecordsYouMightLike(int index, Record record) {
            Log.d(TAG, "onRecordInfoReponseRecordsYouMightLike");
            recordsYouMightLikeList.add(record);
            recordsYouMightLikeAdapter.loadThumbnail(record.thumbnailUrl);
        }
        @Override
        public void onRecordInfoResponseEnd() {
            Log.d(TAG, "onRecordInfoReponseEnd");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    initView();
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
            record = intent.getParcelableExtra(PARAM_RECORD);
        } else {
            record = null;
        }
        recordId = record.id;
        Log.d(TAG, "recordId = " + recordId);
        Log.d(TAG, "record.title = " + record.title);
        Log.d(TAG, "record.artist = " + record.artist);

        shouldShowRecordInfoWaitDialog = true;
        recordInfoWaitDialog = new ProgressDialog(this);
        recordInfoWaitDialog.setTitle("Record detail");
        recordInfoWaitDialog.setMessage("fetching record information...");
        recordInfoWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        recordsByTheSameLabelAdapter = new MyAdapter(this, R.layout.activity_record_detail_records_row);
        recordsAlsoBoughtAdapter = new MyAdapter(this, R.layout.activity_record_detail_records_row);
        recordsByTheSameArtistAdapter = new MyAdapter(this, R.layout.activity_record_detail_records_row);
        recordsYouMightLikeAdapter = new MyAdapter(this, R.layout.activity_record_detail_records_row);

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

    private class MyAdapter extends ArrayAdapter {
        private LayoutInflater inflater;
        private int resource;
        private ArrayList<Bitmap> bmpList = new ArrayList<Bitmap>();

        public MyAdapter(Context context, int resource) {
            super(context, resource);
            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.resource = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = inflater.inflate(this.resource, null);
            }
            Record record = (Record)getItem(position);
            ImageView imageView = (ImageView)view.findViewById(R.id.cover);
            if (imageView != null && position <= bmpList.size()) {
                imageView.setImageBitmap(bmpList.get(position));
            }
            TextView titleTextView = (TextView)view.findViewById(R.id.title);
            if (titleTextView != null) {
                titleTextView.setText(record.title);
            }
            TextView artistTextView = (TextView)view.findViewById(R.id.artist);
            if (artistTextView != null) {
                artistTextView.setText(record.artist);
            }
            return view;
        }

        public void loadThumbnail(String thumbnailUrl) {
            bmpList.add(RecordDetailActivity.loadThumbnail(thumbnailUrl));
        }
    }
}
