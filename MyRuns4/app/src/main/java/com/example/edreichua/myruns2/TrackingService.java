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

    // Notification
    NotifyServiceReceiver notifyServiceReceiver;
    public ExerciseEntry entry = new ExerciseEntry();
    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE_BROADCAST_KEY="StopServiceBroadcastKey";
    final static int RQS_STOP_SERVICE = 1;
    private static boolean isRunning = false;

    //create an instance of TrackingBinder class
    private Context context;
    private TrackingBinder trackingBinder = new TrackingBinder();


    @Override
    public void onCreate() {
        notifyServiceReceiver = new NotifyServiceReceiver();
        isRunning = true;
        context = this;
        initExerciseEntry();

        Log.d("Testing", "tracking service created");

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String svcName= Context.LOCATION_SERVICE;
        locationManager = (LocationManager)getSystemService(svcName);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                this);
        Log.d("Testing","Location manager created");

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

        PendingIntent pendingIntent
                = PendingIntent.getActivity(context, 0, resultIntent, 0);

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
    }

    public ExerciseEntry getExerciseEntry(){
        return entry;
    }

    public void startLocationUpdates(Location location){
        if (location != null) {
            LatLng mLatLng = fromLocationToLatLng(location);
            ArrayList<LatLng> arr = new ArrayList<LatLng>();
            arr.add(mLatLng);
            entry.setmLocationList(arr);

            Intent intent = new Intent();
            intent.setAction(MapDisplayActivity.ACTION);
            intent.putExtra(MapDisplayActivity.UPDATE_LOC_BROADCAST_KEY,
                    MapDisplayActivity.RQS_UPDATE_LOC);
            this.context.sendBroadcast(intent);
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
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            int rqs = arg1.getIntExtra(STOP_SERVICE_BROADCAST_KEY, 0);

            if (rqs == RQS_STOP_SERVICE){
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

