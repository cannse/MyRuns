package com.example.edreichua.myruns2;

import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edreichua on 4/22/16.
 */

public class MapDisplayActivity extends FragmentActivity implements ServiceConnection{

    // Variables dealing with the map
    private GoogleMap mMap;
    private Marker startLoc, endLoc;
    private double currSpeed;
    public final static String CURR_SPEED = "CurrentSpeed";

    // Variables dealing with database
    private ExerciseEntryDbHelper entryHelper;
    private ExerciseEntry entry;
    private String activityType;
    private String inputType;

    // Variable dealing with service connection
    private ServiceConnection mConnection = this;
    private TrackingService trackingService;
    private Intent serviceIntent;

    // Variables dealing with broadcast
    public final static String ACTION = "NotifyLocationUpdate";
    public final static String UPDATE_LOC_BROADCAST_KEY="UpdateBroadcastKey";
    public final static int RQS_UPDATE_LOC = 1;
    private UpdateLocationReceiver updateLocationReceiver;
    private boolean mIsBound;

    // Variables dealing with history
    public final static String NOT_DRAWN = "NotDrawn";
    private boolean isHistory, notDrawn, updateMap;
    private long rowId;

    /////////////////////// Override core functionality ///////////////////////

    /**
     * Handle creating of activity
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Create main layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_display);

        // Set up the map
        setUpMapIfNeeded();

        // Get Bundle
        Bundle bundle;
        if(savedInstanceState == null) {
            bundle = getIntent().getExtras();
        }else{
            bundle = savedInstanceState;
            updateMap = true;
        }

        isHistory = bundle.getBoolean(HistoryFragment.FROM_HISTORY,false);
        rowId = bundle.getLong(HistoryFragment.ROW_INDEX, 0);
        notDrawn = bundle.getBoolean(NOT_DRAWN,true);

        if(!isHistory) {
            // Start service
            startService();

            // Start broadcast receiver
            updateLocationReceiver = new UpdateLocationReceiver();

            // Bind service
            mIsBound = false;
            bindService();

        }else{
            // Remove button
            (findViewById(R.id.button_save_gps)).setVisibility(View.GONE);
            (findViewById(R.id.button_cancel_gps)).setVisibility(View.GONE);

            // Retrieve entry
            entry = MainActivity.DBhelper.fetchEntryByIndex(rowId);

            // Draw trace and update stats
            drawHistoryOnMap();
            updateStat();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(HistoryFragment.FROM_HISTORY, isHistory);
        outState.putLong(HistoryFragment.ROW_INDEX, rowId);
        outState.putBoolean(NOT_DRAWN, notDrawn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        if(!isHistory) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTION);
            registerReceiver(updateLocationReceiver, intentFilter);
        }
    }

    protected void onPause(){
        if(!isHistory) {
            unregisterReceiver(updateLocationReceiver);
        }
        super.onPause();
    }

    /////////////////////// Broadcast receiver ///////////////////////

    public class UpdateLocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (trackingService != null ) {
                Log.d("Testing", "received");
                getExerciseEntryFromService();
                currSpeed = intent.getFloatExtra(CURR_SPEED,0);
                drawTraceOnMap();
                updateStat();
            }
        }
    }



    /////////////////////// Binding with Tracking Service ///////////////////////

    public void startService(){
        serviceIntent = new Intent(this, TrackingService.class);
        if(getParentActivityIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            activityType = StartFragment.ID_TO_ACTIVITY[bundle.getInt(StartFragment.ACTIVITY_TYPE, 0)];
            inputType = StartFragment.ID_TO_INPUT[bundle.getInt(StartFragment.INPUT_TYPE, 0)];
            serviceIntent.putExtras(bundle);
        }
        startService(serviceIntent);
    }


    public void bindService(){

        if(!mIsBound) {
            bindService(this.serviceIntent, mConnection,
                    Context.BIND_AUTO_CREATE);
            mIsBound = true;
            Log.d("Testing", "Binding: " + mIsBound);
        }
    }

    public void unbindService(){

        if(mIsBound) {
            unbindService(this.mConnection);
            mIsBound = false;
            Log.d("Testing", "Unbinding: " + mIsBound);
        }
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        trackingService =  ((TrackingService.TrackingBinder) service).getReference();
        if(updateMap) {
            getExerciseEntryFromService();
            drawHistoryOnMap();
        }
        Log.d("Testing", "Service connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        stopService(serviceIntent);
        trackingService = null;
    }

    @Override
    protected void onDestroy() {
        if(!isHistory) {
            // Destroy notification and tracking service
            Log.d("Testing", "start destroy");
            Intent intent = new Intent();
            intent.setAction(TrackingService.ACTION);
            intent.putExtra(TrackingService.STOP_SERVICE_BROADCAST_KEY, TrackingService.RQS_STOP_SERVICE);
            sendBroadcast(intent);
            Log.d("Testing", "destroyed");

            // Destroy broadcast receiver
            if (trackingService != null) {
                unbindService();
                stopService(serviceIntent);
            }
        }

        super.onDestroy();
    }

    /////////////////////// Handle Selection of buttons ///////////////////////

    /**
     * Handle the selection of the save button
     * @param v
     */
    public void selectGPSSave(View v) {
        saveEntryToDb();

        // Close the activity
        finish();
    }


    /**
     * Handle the selection of the cancel button
     * @param v
     */
    public void selectGPSCancel(View v) {

        // Inform user that the profile information is discarded
        Toast.makeText(getApplicationContext(), getString(R.string.ui_toast_cancel),
                Toast.LENGTH_SHORT).show();

        // Close the activity
        finish();
    }

    // To delete data entry
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if(isHistory) {
            menu.add(Menu.NONE, 0, 0, "DELETE").
                    setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
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
                            MainActivity.DBhelper.removeEntry(rowId);
                            HistoryFragment.adapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /////////////////////// Updating location functionality ///////////////////////

    public void drawHistoryOnMap(){
        // Get location list
        ArrayList<LatLng> latLngList = entry.getmLocationList();

        // Get start location
        LatLng latlng = latLngList.get(0);
        startLoc = mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_GREEN)));

        // Animate if not drawn
        if(notDrawn){
            // Zoom in
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,
                    17));
            notDrawn = false;
        }

        // Draw polyline
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.BLACK);
        polylineOptions.width(5);
        polylineOptions.addAll(latLngList);
        mMap.addPolyline(polylineOptions);

        // Draw the end marker
        endLoc = mMap.addMarker(new MarkerOptions().position(latLngList.get(latLngList.size()-1))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

    }

    public void drawTraceOnMap(){

        if(entry.getmLocationList().size() == 1 && !updateMap) {

            // Set input type and activity type
            Bundle bundle = getIntent().getExtras();
            entry.setmInputType(bundle.getInt(StartFragment.INPUT_TYPE,0));
            entry.setmActivityType(bundle.getInt(StartFragment.ACTIVITY_TYPE, 0));

            // Animate
            LatLng latlng = entry.getmLocationList().get(0);
            startLoc = mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_GREEN)));
            // Zoom in
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,
                    17));
            notDrawn = false;
        }else{

            // Draw polyline
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(Color.BLACK);
            polylineOptions.width(5);
            ArrayList<LatLng> latLngList = entry.getmLocationList();
            polylineOptions.addAll(latLngList);
            mMap.addPolyline(polylineOptions);

            // Remove the end marker
            if(endLoc != null){
                endLoc.remove();
            }

            // Draw the end marker
            endLoc = mMap.addMarker(new MarkerOptions().position(latLngList.get(latLngList.size()-1))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

    }




    /////////////////////// Updating stats functionality ///////////////////////


    public void getExerciseEntryFromService(){
        entry = trackingService.getExerciseEntry();
    }

    public void saveEntryToDb(){
        Log.d("Testing", "saving entry...");
        entry.setmDuration(trackingService.getTimePassed());

        // Execute writing to database
        new WriteToDB().execute();
    }

    /**
     * Update stats on text views
     */
    public void updateStat(){

        // Get unit pref
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String unitPref = pref.getString(getString(R.string.unit_preference), getString(R.string.unit_km));

        // Set the text view for type
        TextView gpsType = (TextView) findViewById(R.id.gps_type);
        gpsType.setText(formatType(StartFragment.ID_TO_ACTIVITY[entry.getmActivityType()]));

        // Set the text view for average speed
        TextView gpsAvgSpeed = (TextView) findViewById(R.id.gps_avg_speed);
        gpsAvgSpeed.setText(formatAvgSpeed(entry.getmAvgSpeed(),unitPref)); // hardcoded for now

        // Set the text view for current speed
        TextView gpsCurSpeed = (TextView) findViewById(R.id.gps_cur_speed);
        gpsCurSpeed.setText(formatCurSpeed(currSpeed,unitPref)); // hardcoded for now

        // Set the text view for climb
        TextView gpsClimb = (TextView) findViewById(R.id.gps_climb);
        gpsClimb.setText(formatClimb(entry.getmClimb(),unitPref)); // hardcoded for now

        // Set the text view for calorie
        TextView gpsCalorie = (TextView) findViewById(R.id.gps_calories);
        gpsCalorie.setText(formatCalories(entry.getmCalorie())); // hardcoded for now

        // Set the text view for distance
        TextView gpsDistance = (TextView) findViewById(R.id.gps_distance);
        gpsDistance.setText(formatDistance(entry.getmDistance(),unitPref)); // hardcoded for now
    }

    /**
     * format activity type
     * @param activity
     * @return
     */
    public static String formatType(String activity){
        return "Type: "+activity;
    }

    /**
     * format average speed
     * @param speed
     * @param unitPref
     * @return
     */
    public static String formatAvgSpeed(Double speed, String unitPref){
        String unit = "km/h";
        if (unitPref.equals("Miles")) {
            speed /= ManualEntryActivity.MILES2KM; // converts from km to miles
            unit = "m/h";
        }
        return "Avg speed: "+String.format("%.2f", speed)+" "+unit;
    }

    /**
     * format current speed
     * @param speed
     * @param unitPref
     * @return
     */
    public static String formatCurSpeed(Double speed, String unitPref){
        String unit = "km/h";
        if (unitPref.equals("Miles")) {
            speed /= ManualEntryActivity.MILES2KM; // converts from km to miles
            unit = "m/h";
        }
        return "Cur speed: "+String.format("%.2f", speed)+" "+unit;
    }

    /**
     * format climb
     * @param climb
     * @param unitPref
     * @return
     */
    public static String formatClimb(Double climb, String unitPref){
        return "Climb: "+String.format("%.2f", climb)+" "+unitPref;
    }

    /**
     * format calories
     * @param cal
     * @return
     */
    public static String formatCalories(int cal){
        return "Calorie: "+cal;
    }

    /**
     * format distance
     * @param distance
     * @param unitPref
     * @return
     */
    public static String formatDistance(double distance, String unitPref) {
        if (unitPref.equals("Miles")) {
            distance /= ManualEntryActivity.MILES2KM; // converts from km to miles
        }
        return "Distance: "+String.format("%.2f", distance)+" "+unitPref;
    }



    /////////////////////// Map functionality ///////////////////////

    /**
     * Function to set up map
     */
    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * Setting up map, with a point new Africa for visual effect
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Africa"));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
            Toast.makeText(getApplicationContext(), "Entry #" + id + " saved.",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
