package com.example.edreichua.myruns2;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * Created by edreichua on 4/22/16.
 */

public class MapDisplayActivity extends FragmentActivity implements ServiceConnection{

    // Variables dealing with the map
    private GoogleMap mMap;
    public Marker startLoc, endLoc;

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
    final static String ACTION = "NotifyLocationUpdate";
    final static String UPDATE_LOC_BROADCAST_KEY="UpdateBroadcastKey";
    final static int RQS_UPDATE_LOC = 1;
    UpdateLocationReceiver updateLocationReceiver;

    boolean mIsBound;

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

        // Start service
        startService();

        // Start broadcast receiver
        updateLocationReceiver = new UpdateLocationReceiver();
        serviceIntent = new Intent(this, TrackingService.class);

        // Bind service
        mIsBound = false;
        bindService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(updateLocationReceiver, intentFilter);
    }

    protected void onPause(){
        unregisterReceiver(updateLocationReceiver);
        super.onPause();
    }

    /////////////////////// Broadcast receiver ///////////////////////

    public class UpdateLocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (trackingService != null ) {
                Log.d("Testing", "received");
                getExerciseEntryFromService();
                drawTraceOnMap();
            }
        }
    }



    /////////////////////// Binding with Tracking Service ///////////////////////

    public void startService(){

        Intent mIntent = new Intent(this, TrackingService.class);
        if(getParentActivityIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            activityType = StartFragment.ID_TO_ACTIVITY[bundle.getInt(StartFragment.ACTIVITY_TYPE, 0)];
            inputType = StartFragment.ID_TO_INPUT[bundle.getInt(StartFragment.INPUT_TYPE, 0)];
            mIntent.putExtras(bundle);
        }
        startService(mIntent);
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
        Log.d("Testing", "Service connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        stopService(serviceIntent);
        trackingService = null;
    }

    @Override
    protected void onDestroy() {

        // Destroy notification and tracking service
        Log.d("Testing", "start destroy");
        Intent intent = new Intent();
        intent.setAction(TrackingService.ACTION);
        intent.putExtra(TrackingService.STOP_SERVICE_BROADCAST_KEY, TrackingService.RQS_STOP_SERVICE);
        sendBroadcast(intent);
        Log.d("Testing", "destroyed");

        // Destroy broadcast receiver
        if(trackingService != null){
            unbindService();
            stopService(serviceIntent);
        }

        super.onDestroy();
    }

    /////////////////////// Handle Selection of buttons ///////////////////////

    /**
     * Handle the selection of the save button
     * @param v
     */
    public void selectGPSSave(View v) {
        // Close the activity
        finish();
    }

    /**
     * Handle the selection of the cancel button
     * @param v
     */
    public void selectGPSCancel(View v) {
        // Close the activity
        finish();
    }


    /////////////////////// Updating location functionality ///////////////////////

    public static LatLng fromLocationToLatLng(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude());

    }

    private void updateWithNewLocation(Location location) {
        updateStat();
    }

    public void drawTraceOnMap(){

        if(entry.getmLocationList().size() == 1) {
            LatLng latlng = entry.getmLocationList().get(0);
            startLoc = mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_GREEN)));
            // Zoom in
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,
                    17));
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

    }

    /**
     * Update stats on text views
     */
    public  void updateStat(){

        // Set the text view for type
        TextView gpsType = (TextView) findViewById(R.id.gps_type);
        gpsType.setText(formatType("Running")); // hardcoded for now

        // Set the text view for average speed
        TextView gpsAvgSpeed = (TextView) findViewById(R.id.gps_avg_speed);
        gpsAvgSpeed.setText(formatAvgSpeed(0.0,"Kilometers")); // hardcoded for now

        // Set the text view for current speed
        TextView gpsCurSpeed = (TextView) findViewById(R.id.gps_cur_speed);
        gpsCurSpeed.setText(formatCurSpeed(0.0, "Kilometers")); // hardcoded for now

        // Set the text view for climb
        TextView gpsClimb = (TextView) findViewById(R.id.gps_climb);
        gpsClimb.setText(formatClimb(0.0, "Kilometers")); // hardcoded for now

        // Set the text view for calorie
        TextView gpsCalorie = (TextView) findViewById(R.id.gps_calories);
        gpsCalorie.setText(formatCalories(0)); // hardcoded for now

        // Set the text view for distance
        TextView gpsDistance = (TextView) findViewById(R.id.gps_distance);
        gpsDistance.setText(formatDistance(0.0,"Kilometers")); // hardcoded for now
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
}
