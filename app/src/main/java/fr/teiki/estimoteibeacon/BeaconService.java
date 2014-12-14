package fr.teiki.estimoteibeacon;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Created by antoinegaltier on 30/11/14.
 */
public class BeaconService extends Service {

    private static final String TAG = "BeaconService";

    private NotificationManager nm;
    private static boolean isRunning = false;

    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    int mValue = 0; // Holds last value set by a client.
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_LIST = 3;
    static final int MSG_STOP_RANGING = 4;
    static final int MSG_ADD_UUID = 5;
    static final int MSG_ENTER_REGION = 6;
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    static final String MSG_SET_LIST_KEY = "list";
    static final String MSG_ADD_UUID_KEY = "uuid";
    static final String MSG_ENTER_REGION_KEY = "region";

    private BeaconManager beaconManager;
    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
    private static final int notificationID = 0;

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    class IncomingHandler extends Handler { // Handler of incoming messages from clients.

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;
                case MSG_STOP_RANGING:
                    try {
                        beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }




    private void sendMessageToUI(List< Beacon > beacons) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            try {
                if(beacons.size()>0) {
                    Bundle b = new Bundle();
                    b.putParcelable(MSG_SET_LIST_KEY, beacons.get(0));
                    //b.putParcelableArrayList(MSG_SET_LIST_KEY, (ArrayList<Beacon>) beacons);
                    Message msg = Message.obtain(null, MSG_SET_LIST);
                    msg.setData(b);
                    mClients.get(i).send(msg);
                }

            } catch (RemoteException e) {
                // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
                mClients.remove(i);
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        showNotification();

        isRunning = true;

        beaconManager = new BeaconManager(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override public void onServiceReady() {
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);
                    beaconManager.startMonitoring(ALL_ESTIMOTE_BEACONS);
                } catch (RemoteException e) {
                    Log.e(TAG, "Cannot start ranging", e);
                }
            }
        });

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List< Beacon > beacons) {
                sendMessageToUI(beacons);

                if (!beacons.isEmpty()){
                    for (Beacon b : beacons){
                        MyPreferenceManager.executeActions(getApplicationContext(),b);
                    }
                }
            }

        });
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> beacons) {
                if (!beacons.isEmpty() && Utils.computeAccuracy(beacons.get(0)) < 3.0){
                    NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(getApplicationContext())
                            .setSmallIcon(R.drawable.ic_launcher) // notification icon
                            .setContentTitle("iBeacon") // title for notification
                            .setContentText("A iBeacon is next!") // message for notification
                            .setOngoing(true);
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.notify(notificationID, mBuilder.build());
                }
                else{
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(notificationID);
                }





//                for (int i = mClients.size() - 1; i >= 0; i--) {
//                    try {
//                        if(beacons.size()>0) {
//                            Bundle b = new Bundle();
//                            b.putParcelable(MSG_ENTER_REGION_KEY, beacons.get(0));
//                            Message msg = Message.obtain(null, MSG_ENTER_REGION);
//                            msg.setData(b);
//                            mClients.get(i).send(msg);
//                        }
//
//                    } catch (RemoteException e) {
//                        // The client is dead. Remove it from the list; we are going through the list from back to front so this is safe to do inside the loop.
//                        mClients.remove(i);
//                    }
//                }
            }

            @Override
            public void onExitedRegion(Region region) {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(notificationID);
            }
        });


    }

    @SuppressWarnings("deprecation")
    private void showNotification() {
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.servicestarted);
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_launcher, text, System.currentTimeMillis());
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.servicelabel), text, contentIntent);
        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        nm.notify(R.string.servicestarted, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyService", "Received start id " + startId + ": " + intent);
        return START_STICKY; // run until explicitly stopped.
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nm.cancel(R.string.servicestarted); // Cancel the persistent notification.
        Log.i("MyService", "Service Stopped.");
        isRunning = false;
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
        } catch (RemoteException e) {
            Log.e(TAG, "Cannot stop but it does not matter now", e);
        }
        beaconManager.disconnect();
    }
}