package org.buffr.boomkat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

public class SearchResultsActivity extends Activity {
    private static final String TAG = SearchResultsActivity.class.getSimpleName();

    public static final String PARAM_SEARCH_WORD = "search_word";

    // UI
    private ArrayList<Record> list = new ArrayList<Record>();
    private ListView listView;
    private MyAdapter adapter;
    private Handler handler = new Handler();
    private ProgressDialog searchWaitDialog;
    private boolean shouldShowSearchWaitDialog;

    private String searchWord;

    private void initListView() {
        adapter.addAll(list);
        listView = (ListView)findViewById(R.id.list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                ListView lv = (ListView)parent;
                Record record = (Record)lv.getItemAtPosition(position);
                Log.d(TAG, "onItemClick, title = " + record.title);

                Intent intent = new Intent();
                intent.setClass(SearchResultsActivity.this, RecordDetailActivity.class);
                intent.putExtra(RecordDetailActivity.PARAM_RECORD_ID, record.id);
                startActivity(intent);
            }
        });
        listView.setAdapter(adapter);
    }

    private IBoomkatService serviceStub = null;
    private ICommandCallback callback = new ICommandCallback.Stub() {
        @Override
        public void onSearchResponseStart(int count) {
            Log.d(TAG, "onSearchResponseStart(" + count + ")");
        }
        @Override
        public void onSearchResponseEachRecord(int index, Record record) {
            Log.d(TAG, "onSearchResponseEachRecord[" + index + "]");
            list.add(record);
            adapter.loadThumbnail(record.thumbnailUrl);
        }
        @Override
        public void onSearchResponseEnd() {
            Log.d(TAG, "onSearchResponseEnd");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    initListView();
                    searchWaitDialog.dismiss();
                }
            });
        }
        @Override
        public void onRecordInfoResponseStart() {}
        @Override
        public void onRecordInfoResponseBody(int index, Record record) {}
        @Override
        public void onRecordInfoResponseRecordsByTheSameLabel(int index, Record record) {}
        @Override
        public void onRecordInfoResponseRecordsAlsoBought(int index, Record record) {}
        @Override
        public void onRecordInfoResponseRecordsByTheSameArtist(int index, Record record) {}
        @Override
        public void onRecordInfoResponseRecordsYouMightLike(int index, Record record) {}
        @Override
        public void onRecordInfoResponseEnd() {}

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
            if (shouldShowSearchWaitDialog) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        searchWaitDialog.show();
                        shouldShowSearchWaitDialog = false;
                    }
                });
            }
            serviceStub = IBoomkatService.Stub.asInterface(service);
            try {
                serviceStub.registerCallback(callback);
                serviceStub.search(searchWord);
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
        setContentView(R.layout.activity_search_results);

        Intent intent = getIntent();
        if (intent != null) {
            searchWord = intent.getStringExtra(PARAM_SEARCH_WORD);
        } else {
            searchWord = "";
        }

        shouldShowSearchWaitDialog = true;
        searchWaitDialog = new ProgressDialog(this);
        searchWaitDialog.setTitle(R.string.activity_search_results_dlg_search_wait_title);
        searchWaitDialog.setMessage(getString(R.string.activity_search_results_dlg_search_wait_message));
        searchWaitDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        adapter = new MyAdapter(this, R.layout.activity_search_results_row);

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
            URL url = null;
            Bitmap bmp = null;
            try {
                url = new URL(thumbnailUrl);
            } catch(MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                bmp = BitmapFactory.decodeStream(url.openStream());
            } catch(IOException e) {
                e.printStackTrace();
                return;
            }
            bmpList.add(bmp);
        }
    }
}
