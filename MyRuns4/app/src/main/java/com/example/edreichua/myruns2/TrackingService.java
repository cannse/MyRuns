package com.example.edreichua.myruns2;


import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by edreichua on 4/28/16.
 */
public class TrackingService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {

    }

    public void setUpNotification(){

    }

    public void initExerciseEntry(){

    }

    public void startLocationUpdates(){

    }

    public void startActivityUpdate(){

    }

    public void onUpdate(){

    }

    // Or put under a different method name
    public void onLocationChanged(Location location) {
        if (location != null) {
            LatLng mLatLng = MapDisplayActivity.fromLocationToLatLng(location);
        }
    }

    public void notifyChange(){

    }



}

