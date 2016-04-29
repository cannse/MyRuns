package com.example.edreichua.myruns2;

/**
 * Created by edreichua on 4/22/16.
 */

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class ManualEntryActivity extends ListActivity {


    public static final String[] MANUAL_OPTIONS = new String[]{"Date", "Time", "Duration", "Distance",
            "Calories", "Heart Rate", "Comment"};

    // Constants for case statement
    private static final int MANUAL_DATE = 0;
    private static final int MANUAL_TIME = 1;
    private static final int MANUAL_DURATION = 2;
    private static final int MANUAL_DISTANCE = 3;
    private static final int MANUAL_CALORIES = 4;
    private static final int MANUAL_HEARTRATE = 5;
    private static final int MANUAL_COMMENT = 6;

    // For date and time functionality
    public Calendar mDateAndTime;

    // Unit preference conversion
    public static final double MILES2KM = 1.60934;

    public static ExerciseEntry entry;


    /////////////////////// Override core functionality ///////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_manual);
        Bundle bundle = getIntent().getExtras();

        // Initialise the database
        entry = new ExerciseEntry();
        //DBhelper = new ExerciseEntryDbHelper(this);

        // Set input type and activity type
        entry.setmInputType(bundle.getInt(StartFragment.INPUT_TYPE,0));
        entry.setmActivityType(bundle.getInt(StartFragment.ACTIVITY_TYPE, 0));

        // Set time and date
        mDateAndTime = Calendar.getInstance();
        entry.setmDateTime(mDateAndTime.getTimeInMillis());
        Log.d("date",mDateAndTime.getTimeInMillis()+"");

        // Set distance
        entry.setmDuration(0);
        entry.setmDistance(0);
        entry.setmCalorie(0);
        entry.setmHeartRate(0);

        // Create a new adapter
        ArrayAdapter<String> manualAdapter = new ArrayAdapter<String>(this, R.layout.list_manual,
                MANUAL_OPTIONS);
        setListAdapter(manualAdapter);

        // Get the ListView and wired the listener
        final ListView manualListView = getListView();

        // Define the listener interface
        OnItemClickListener mListener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                switch(position){

                    case MANUAL_DATE:
                        selectManualDate(manualListView);
                        break;

                    case MANUAL_TIME:
                        selectManualTime(manualListView);
                        break;

                    case MANUAL_DURATION:
                        DialogFragment durationPickerWindow =
                                MyRunsDialogFragment.newInstance(MyRunsDialogFragment.DURATION_PICKER_ID);
                        durationPickerWindow.show(getFragmentManager(), getString(R.string.ui_manual_duration));
                        break;

                    case MANUAL_DISTANCE:
                        DialogFragment distancePickerWindow =
                                MyRunsDialogFragment.newInstance(MyRunsDialogFragment.DISTANCE_PICKER_ID);
                        distancePickerWindow.show(getFragmentManager(), getString(R.string.ui_manual_distance));
                        break;

                    case MANUAL_CALORIES:
                        DialogFragment caloresPickerWindow =
                                MyRunsDialogFragment.newInstance(MyRunsDialogFragment.CALORIES_PICKER_ID);
                        caloresPickerWindow.show(getFragmentManager(), getString(R.string.ui_manual_calories));
                        break;

                    case MANUAL_HEARTRATE:
                        DialogFragment heartPickerWindow =
                                MyRunsDialogFragment.newInstance(MyRunsDialogFragment.HEARTRATE_PICKER_ID);
                        heartPickerWindow.show(getFragmentManager(), getString(R.string.ui_manual_heartrate));
                        break;

                    case MANUAL_COMMENT:
                        DialogFragment commentPickerWindow =
                                MyRunsDialogFragment.newInstance(MyRunsDialogFragment.COMMENT_PICKER_ID);
                        commentPickerWindow.show(getFragmentManager(), getString(R.string.ui_manual_comment));
                        break;

                    default:
                        break;
                }

            }
        };

        manualListView.setOnItemClickListener(mListener);
    }


    /////////////////////// Helper Functions ///////////////////////

    /**
     * Select the time using the Android widget
     * @param v
     */
    public void selectManualTime(View v) {

        TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mDateAndTime.set(Calendar.MINUTE, minute);
                mDateAndTime.set(Calendar.SECOND,0);
                entry.setmDateTime(mDateAndTime.getTimeInMillis());
            }
        };

        new TimePickerDialog(ManualEntryActivity.this, mTimeListener,
                mDateAndTime.get(Calendar.HOUR_OF_DAY),
                mDateAndTime.get(Calendar.MINUTE), true).show();
    }


    /**
     * Select the date using the Android widget
     * @param v
     */
    public void selectManualDate(View v) {

        DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mDateAndTime.set(Calendar.YEAR, year);
                mDateAndTime.set(Calendar.MONTH, monthOfYear);
                mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                entry.setmDateTime(mDateAndTime.getTimeInMillis());
            }
        };

        new DatePickerDialog(ManualEntryActivity.this, mDateListener,
                mDateAndTime.get(Calendar.YEAR),
                mDateAndTime.get(Calendar.MONTH),
                mDateAndTime.get(Calendar.DAY_OF_MONTH)).show();
    }


    /////////////////////// Handle Selection of buttons ///////////////////////

    /**
     * Handle the selection of the save button
     * @param v
     */
    public void selectManualSave(View v) {

        // Execute writing to database
        new WriteToDB().execute();

        // Close the activity
        finish();
    }

    /**
     * Handle the selection of the cancel button
     * @param v
     */
    public void selectManualCancel(View v) {

        // Inform user that the profile information is discarded
        Toast.makeText(getApplicationContext(), getString(R.string.ui_toast_cancel),
                Toast.LENGTH_SHORT).show();

        // Close the activity
        finish();
    }


    /////////////////////// Use AsyncTask to write to database ///////////////////////

    private class WriteToDB extends AsyncTask<Void, Integer, Void> {
        private Long id;

        @Override
        protected Void doInBackground(Void... params) {
            // Insert entry to database
            id = MainActivity.DBhelper.insertEntry(entry);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

            HistoryFragment.adapter.notifyDataSetChanged();

            // Inform user that the entry has been saved
            Toast.makeText(getApplicationContext(), "Entry #"+id+" saved.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
