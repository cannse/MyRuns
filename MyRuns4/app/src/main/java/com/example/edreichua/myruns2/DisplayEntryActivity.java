package com.example.edreichua.myruns2;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

/**
 * Created by edreichua on 4/22/16.
 */
public class DisplayEntryActivity extends Activity {

    private long rowid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Create
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_entry);

        // Obtain row index and entry
        Bundle bundle = getIntent().getExtras();
        rowid = bundle.getLong(HistoryFragment.ROW_INDEX);
        ExerciseEntry entry = MainActivity.DBhelper.fetchEntryByIndex(rowid);

        // Display EditText for Input type
        EditText displayInput = (EditText) findViewById(R.id.display_input);
        displayInput.setText(StartFragment.ID_TO_INPUT[entry.getmInputType()]);

        // Display EditText for Activity type
        EditText displayActivity = (EditText) findViewById(R.id.display_activity);
        displayActivity.setText(StartFragment.ID_TO_ACTIVITY[entry.getmActivityType()]);

        // Display EditText for Date and Time
        EditText displayDateTime = (EditText) findViewById(R.id.display_datetime);
        displayDateTime.setText(HistoryFragment.formatDateTime(entry.getmDateTime()));

        // Display EditText for Duration
        EditText displayDuration = (EditText) findViewById(R.id.display_duration);
        displayDuration.setText(HistoryFragment.formatDuration(entry.getmDuration()));

        // Display EditText for Distance
        EditText displayDistance = (EditText) findViewById(R.id.display_distance);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String unitPref = pref.getString(getString(R.string.unit_preference), getString(R.string.unit_km));
        String distance = HistoryFragment.formatDistance(entry.getmDistance(),unitPref);
        displayDistance.setText(distance);

        // Display EditText for Calories
        EditText displayCalories = (EditText) findViewById(R.id.display_calories);
        displayCalories.setText(entry.getmCalorie()+" cals");

        // Display EditText for Heart Rate
        EditText displayHeartrate = (EditText) findViewById(R.id.display_heartrate);
        displayHeartrate.setText(entry.getmHeartRate()+" bpm");
    }

    // To delete data entry
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, 0, 0, "DELETE").
                setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    // Perform the removal on a thread
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        runThread();
        finish();
        return true;
    }

    // Extracredit: use a background thread to delete information
    private void runThread(){
        new Thread(){
            public void run(){
                try {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            MainActivity.DBhelper.removeEntry(rowid);
                            HistoryFragment.adapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}