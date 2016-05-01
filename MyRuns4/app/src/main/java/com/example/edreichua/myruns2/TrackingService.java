package com.example.edreichua.myruns2;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by edreichua on 4/28/16.
 */
public class TrackingService extends Service implements LocationListener {

    // Location manager
    LocationManager locationManager;
    String provider;

    // Notification
    NotifyServiceReceiver notifyServiceReceiver;
    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE_BROADCAST_KEY="StopServiceBroadcastKey";
    final static int RQS_STOP_SERVICE = 1;
    private static boolean isRunning = false;

    //create an instance of TrackingBinder class
    private TrackingBinder trackingBinder = new TrackingBinder();

    // Database
    private ExerciseEntry entry;
    private ArrayList<LatLng> locList;


    @Override
    public void onCreate() {

        // Set up exercise entry
        initExerciseEntry();

        // Set up receiver
        Log.d("Testing", "tracking service is created");
        notifyServiceReceiver = new NotifyServiceReceiver();
        isRunning = true;

        Log.d("Testing", "tracking service created");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Set up location manager
        String svcName= Context.LOCATION_SERVICE;
        locationManager = (LocationManager)getSystemService(svcName);

        // Set up criteria
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        provider = locationManager.getBestProvider(criteria, true);

        // Get most recent location
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                Location l = locationManager.getLastKnownLocation(provider);
                startLocationUpdates(l);
            }
        }, 1);

        // Request for locations
        locationManager.requestLocationUpdates(provider, 5, 1,
                this);
        Log.d("Testing", "Location manager created");

        // Set up notification
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(notifyServiceReceiver, intentFilter);
        setUpNotification();

        return super.onStartCommand(intent, flags, startId);
    }


    //MyBinder class extending Binder
    public class TrackingBinder extends Binder {
        public TrackingService getReference() {
            return TrackingService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {

        return trackingBinder;
    }

    @Override
    public void onDestroy() {
        locationManager.removeUpdates(this);
        this.unregisterReceiver(notifyServiceReceiver);
        isRunning = false;
        super.onDestroy();
    }

    public void setUpNotification(){

        Context context = getApplicationContext();
        String notificationTitle = "MyRuns";
        String notificationText = "Recording your path now";

        Intent resultIntent = new Intent(context, MapDisplayActivity.class);

        resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resultIntent.setAction(Intent.ACTION_MAIN);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, resultIntent, 0);

        Notification notification = new Notification.Builder(this)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText).setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.flags = notification.flags
                | Notification.FLAG_ONGOING_EVENT;

        notificationManager.notify(0, notification);
    }

    public void initExerciseEntry(){
        entry = new ExerciseEntry();
        locList = new ArrayList<LatLng>();
        entry.setmLocationList(locList);
    }

    public ExerciseEntry getExerciseEntry(){
        return entry;
    }

    public void startLocationUpdates(Location location){
        if (location != null) {
            LatLng mLatLng = fromLocationToLatLng(location);
            entry.addmLocationList(mLatLng);
            Intent intent = new Intent();
            intent.setAction(MapDisplayActivity.ACTION);
            intent.putExtra(MapDisplayActivity.UPDATE_LOC_BROADCAST_KEY,
                    MapDisplayActivity.RQS_UPDATE_LOC);
            sendBroadcast(intent);
            Log.d("Testing", "sent broadcast");
        }
    }

    public void startActivityUpdate(){

    }

    public void onUpdate(){

    }





    public void notifyChange(){

    }

    public static boolean isRunning() {
        return isRunning;
    }


    public class NotifyServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int req = intent.getIntExtra(STOP_SERVICE_BROADCAST_KEY, 0);
            if (req == RQS_STOP_SERVICE){
                Log.d("Testing", "service stopped");
                stopSelf();
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                        .cancelAll();
            }
        }
    }

    /////////////////////// Updating location functionality ///////////////////////

    public static LatLng fromLocationToLatLng(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude());

    }

    public void onLocationChanged(Location location) {
        Log.d("Testing", "location change");
        startLocationUpdates(location);
    }

    public void onProviderDisabled(String provider) {}
    public void onProviderEnabled(String provider) {}
    public void onStatusChanged(String provider, int status, Bundle extras) {}


}

