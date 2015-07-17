package fr.teiki.ibs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

import fr.teiki.ibs.util.BleUtil;
import fr.teiki.ibs.util.ScannedDevice;

/**
 * Created by antoinegaltier on 30/11/14.
 */
public class BeaconService extends Service implements BluetoothAdapter.LeScanCallback {

    private static final String TAG = "BeaconService";

    private NotificationManager nm;
    private static boolean isRunning = false;
    private BluetoothAdapter mBTAdapter;
    private boolean mIsScanning = false;
    private ArrayList<ScannedDevice> mList = new ArrayList<>();

    ArrayList<Messenger> mClients = new ArrayList<Messenger>(); // Keeps track of all current registered clients.
    int mValue = 0; // Holds last value set by a client.
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_SET_LIST = 3;
    static final int MSG_STOP_RANGING = 4;
    static final int MSG_ADD_UUID = 5;
    static final int MSG_ENTER_REGION = 6;
    final Messenger mMessenger = new Messenger(new IncomingHandler()); // Target we publish for clients to send messages to IncomingHandler.

    static final String MSG_DEVICE = "device";
    static final String MSG_RSSI = "rssi";
    static final String MSG_SCAN_RECORD = "scanrecord";

//    private BeaconManager beaconManager;
//    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
//    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);
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
                    stopScan();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }




    private void sendMessageToUI(BluetoothDevice device, int rssi, byte[] scanRecord) {
        for (int i = mClients.size() - 1; i >= 0; i--) {
            Bundle b = new Bundle();
            b.putParcelable(MSG_DEVICE, device);
            b.putInt(MSG_RSSI, rssi);
            b.putByteArray(MSG_SCAN_RECORD, scanRecord);
            Message msg = Message.obtain(null, MSG_SET_LIST);
            msg.setData(b);
            try {
                mClients.get(i).send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        showNotification();

        isRunning = true;

        init();
    }

    private void init() {
        // BLE check
        if (!BleUtil.isBLESupported(this)) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        // BT check
        BluetoothManager manager = BleUtil.getManager(this);
        if (manager != null) {
            mBTAdapter = manager.getAdapter();
        }
        if (mBTAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            return;
        }

        startScan();
    }

    private void startScan() {
        if ((mBTAdapter != null) && (!mIsScanning)) {
            mBTAdapter.startLeScan(this);
            mIsScanning = true;
        }
    }

    private void stopScan() {
        if (mBTAdapter != null) {
            mBTAdapter.stopLeScan(this);
        }
        mIsScanning = false;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        final BluetoothDevice newDevice = device;
        final int newrssi = rssi;
        final byte[] newscanRecord = scanRecord;
        sendMessageToUI(newDevice, newrssi, newscanRecord);
        MyPreferenceManager.executeActions(getApplicationContext(), device.getAddress(), rssi, scanRecord);
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
        stopScan();
    }
}