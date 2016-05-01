package com.example.edreichua.myruns2;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by edreichua on 4/28/16.
 */
public class TrackingService extends Service {

    final static String ACTION = "NotifyServiceAction";
    final static String STOP_SERVICE_BROADCAST_KEY="StopServiceBroadcastKey";
    final static int RQS_STOP_SERVICE = 1;

    private static boolean isRunning = false;


    NotifyServiceReceiver notifyServiceReceiver;

    @Override
    public void onCreate() {
        Log.d("Testing", "tracking service is created");
        notifyServiceReceiver = new NotifyServiceReceiver();
        isRunning = true;
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        registerReceiver(notifyServiceReceiver, intentFilter);

        // Send Notification
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
//        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Testing", "tracking service is bound");
        return null;
    }

    @Override
    public void onDestroy() {
//        this.unregisterReceiver(notifyServiceReceiver);
        isRunning = false;
        super.onDestroy();
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
}

