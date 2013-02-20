package org.buffr.boomkat;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import org.buffr.boomkat.R;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText editTextView = (EditText)findViewById(R.id.search_word);
        Button button = (Button)findViewById(R.id.search_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, SearchResultsActivity.class);
                intent.putExtra(SearchResultsActivity.PARAM_SEARCH_WORD, ((SpannableStringBuilder)editTextView.getText()).toString());
                startActivity(intent);
            }
        });

        Command.init(this, getAssets());
        try {
            Runtime.getRuntime().exec("chmod 0755 " + getCacheDir().getAbsolutePath());
        } catch (IOException e) {
            Log.d(TAG, "failed to chmod cache dir");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
