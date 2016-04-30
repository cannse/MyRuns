package com.example.edreichua.myruns2;

/**
 * Created by edreichua on 4/22/16.
 */

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<ExerciseEntry>>  {

    public static HistoryAdapter adapter;

    // Constant tag for position
    public static final String ROW_INDEX = "Row_Index";


    /////////////////////// Override onCreate ///////////////////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getActivity() != null) {

            adapter = new HistoryAdapter(getActivity(), new ArrayList<ExerciseEntry>());
            Log.d("adapter created", "adapter created");

            if(getLoaderManager().getLoader(0) == null) {
                getLoaderManager().initLoader(0, null, this).forceLoad();
            } else {
                getLoaderManager().restartLoader(0, null, this).forceLoad();
            }
            setListAdapter(adapter);
        }

        if(savedInstanceState != null){
            adapter.notifyDataSetChanged();
            setListAdapter(adapter);
        }
    }



    /////////////////////// Override ListFragment functionality ///////////////////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onListItemClick(ListView parent, View v, int position, long id) {
        super.onListItemClick(parent, v, position, id);

        Intent mIntent = new Intent(getActivity(), DisplayEntryActivity.class);

        // Find the row id by accessing the value in the invisible row
        TextView tv = (TextView) v.findViewById(R.id.rowid);
        long rowid = Long.parseLong(tv.getText().toString());
        mIntent.putExtra(ROW_INDEX, rowid);

        getActivity().startActivity(mIntent);
    }


    /////////////////////// Override loader functionality ///////////////////////

    @Override
    public Loader<List<ExerciseEntry>> onCreateLoader(int id, Bundle args) {
        Log.d("Create", "Create");

        return new HistoryLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<ExerciseEntry>> loader, List<ExerciseEntry> data) {
        Log.d("finish", "finish");
        adapter.setHistory(data);
    }

    @Override
    public void onLoaderReset(Loader<List<ExerciseEntry>> loader) {
        adapter.setHistory(new ArrayList<ExerciseEntry>());
    }


    /////////////////////// Use AsyncTaskLoader to read from database ///////////////////////

    public static class HistoryLoader extends AsyncTaskLoader<List<ExerciseEntry>> {

        public HistoryLoader(Context context){
            super(context);
        }

        @Override
        public List<ExerciseEntry> loadInBackground() {
            Log.d("loading","loading");
            return MainActivity.DBhelper != null ?
                    MainActivity.DBhelper.fetchEntries() : null;
        }
    }


    /////////////////////// Adapter for listview ///////////////////////

    public class HistoryAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private List<ExerciseEntry> entries;

        public HistoryAdapter(Context context, List<ExerciseEntry> entries) {
            this.entries = entries;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {

            if (view == null) {
                view = inflater.inflate(R.layout.list_history, parent, false);
            }

            ExerciseEntry entry = entries.get(position);

            // Set the first line
            TextView firstLine = (TextView) view.findViewById(R.id.history_list_first_line);
            firstLine.setText(getFirstLine(entry));

            // Set the second line
            TextView secondLine = (TextView) view.findViewById(R.id.history_list_second_line);
            secondLine.setText(getSecondLine(entry));

            //Set the id
            TextView thirdLine = (TextView) view.findViewById(R.id.rowid);
            thirdLine.setText(entry.getmId()+"");

            return view;
        }

        @Override
        public void notifyDataSetChanged(){
            List<ExerciseEntry> list = MainActivity.DBhelper.fetchEntries();
            adapter = new HistoryAdapter(getActivity(),list);
            setListAdapter(adapter);
            super.notifyDataSetChanged();
        }

        @Override
        public Object getItem(int position) {
            return entries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return entries.size();
        }

        public void setHistory(List<ExerciseEntry> data) {
            if (data != null) entries.addAll(data);
            notifyDataSetChanged();
        }


        /////////////////////// Helper function ///////////////////////

        /**
         * Helper function to get the first line in the history list
         */
        private String getFirstLine(ExerciseEntry entry) {

            String input = StartFragment.ID_TO_INPUT[entry.getmInputType()];
            String activity = StartFragment.ID_TO_ACTIVITY[entry.getmActivityType()];
            String dateTime = formatDateTime(entry.getmDateTime());
            return input + ": " + activity + ", " + dateTime;
        }

        /**
         * Helper function to get the second line in the history list
         */
        private String getSecondLine(ExerciseEntry entry) {

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unitPref = pref.getString(getString(R.string.unit_preference), getString(R.string.unit_km));
            String distance = formatDistance(entry.getmDistance(),unitPref);
            String duration = formatDuration(entry.getmDuration());
            return distance + ", " + duration;
        }
    }

    // Convert the date and time from milliseconds to the proper format
    public static String formatDateTime(long dateTime) {
        Date date = new Date(dateTime);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd yyyy");

        return timeFormat.format(date) + " " + dateFormat.format(date);
    }

    // Convert the duration from seconds to the proper format
    public static String formatDuration(int duration) {
        int minutes = duration/60;
        int seconds = duration%60;
        if (minutes == 0 && seconds == 0) return "0secs";
        return String.valueOf(minutes) + "min " + String.valueOf(seconds) + "secs";
    }

    // Convert the distance from kilometers to the proper format
    public static String formatDistance(double distance, String unitPref) {

        if (unitPref.equals("Miles")) {
            distance /= ManualEntryActivity.MILES2KM; // converts from km to miles
        }
        return String.format("%.2f", distance)+" "+unitPref;
    }

}