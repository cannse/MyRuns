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
import android.net.Uri;
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

    NotifyServiceReceiver notifyServiceReceiver;

    @Override
    public void onCreate() {
        notifyServiceReceiver = new NotifyServiceReceiver();
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
        String notificationText = "Recording your path now!";
        Intent resultIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.cs.dartmouth.edu/~campbell/cs65/cs65.html"));
        PendingIntent pendingIntent
                = PendingIntent.getActivity(getBaseContext(),
                0, resultIntent,
                Intent.FLAG_ACTIVITY_NEW_TASK);


        Notification notification = new Notification.Builder(this)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText).setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent).build();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notification.flags = notification.flags
                | Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
//        this.unregisterReceiver(notifyServiceReceiver);
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

