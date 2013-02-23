package org.buffr.boomkat;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import org.buffr.boomkat.R;

public class RecordDetailActivity extends Activity {
    private static final String TAG = RecordDetailActivity.class.getSimpleName();

    public static final String PARAM_RECORD_ID = "record_id";

    private String recordId;

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
