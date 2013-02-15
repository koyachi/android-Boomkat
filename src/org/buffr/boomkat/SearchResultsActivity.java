package org.buffr.boomkat;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

import java.io.IOException;
import java.util.ArrayList;

public class SearchResultsActivity extends Activity {
    private String TAG = SearchResultsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        // should start before come to this activity... => Service?
        Command command = new Command(this, getAssets());

        try {
            Runtime.getRuntime().exec("chmod 0755 " + getCacheDir().getAbsolutePath());
        } catch (IOException e) {
            Log.d(TAG, "failed to chmod cache dir");
        }

        ArrayList<String> args = new ArrayList<String>();
        args.add("search");
        args.add("'Tim Hecker'");
        command.start(args);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
}
