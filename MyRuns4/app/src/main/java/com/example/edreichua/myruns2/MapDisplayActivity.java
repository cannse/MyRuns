package com.example.edreichua.myruns2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

/**
 * Created by edreichua on 4/22/16.
 */

public class MapDisplayActivity extends FragmentActivity implements ServiceConnection{

    // Variables dealing with the map
    private GoogleMap mMap;
    public Marker startLoc;

    // Variables dealing with database
    public ExerciseEntryDbHelper entry;
    private String activityType;
    private String inputType;

    // Variable dealing with service connection
    private ServiceConnection mConnection = this;

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

        // Get the location
        LocationManager locationManager;
        String svcName= Context.LOCATION_SERVICE;
        locationManager = (LocationManager)getSystemService(svcName);

        // Set up criteria
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setCostAllowed(true);
        String provider = locationManager.getBestProvider(criteria, true);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);

        // Find the latitude and longitude
        Location l = locationManager.getLastKnownLocation(provider);
        LatLng latlng = fromLocationToLatLng(l);

        startLoc = mMap.addMarker(new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_GREEN)));
        // Zoom in
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,
                17));

        updateWithNewLocation(l);

        locationManager.requestLocationUpdates(provider, 2000, 10,
                locationListener);

        // Start service
        startService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /////////////////////// Binding with Tracking Service ///////////////////////

    public void startService(){

        Bundle bundle = getIntent().getExtras();
        activityType = StartFragment.ID_TO_ACTIVITY[bundle.getInt(StartFragment.ACTIVITY_TYPE, 0)];
        inputType = StartFragment.ID_TO_INPUT[bundle.getInt(StartFragment.INPUT_TYPE, 0)];

        Intent mIntent = new Intent(this, TrackingService.class);
        mIntent.putExtras(bundle);
        startService(mIntent);
    }

    public void bindService(){

        bindService(new Intent(this, TrackingService.class), mConnection,
                Context.BIND_AUTO_CREATE);

    }

    public void getExerciseEntryFromService(){

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    @Override
    protected void onDestroy() {
        Log.d("Testing", "start destroy");
        Intent intent = new Intent();
        intent.setAction(TrackingService.ACTION);
        intent.putExtra(TrackingService.STOP_SERVICE_BROADCAST_KEY, TrackingService.RQS_STOP_SERVICE);
        sendBroadcast(intent);
        Log.d("Testing", "destroyed");
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

    }




    /////////////////////// Updating stats functionality ///////////////////////


    public void onMessageRecv(){

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

    /**
     * Set up location listener
     */
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {}
    };


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
