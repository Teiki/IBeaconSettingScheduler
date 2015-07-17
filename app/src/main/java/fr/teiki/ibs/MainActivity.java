package fr.teiki.ibs;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import fr.teiki.ibs.util.ScannedDevice;


public class MainActivity extends ActionBarActivity implements BeaconConsumer{

    private static final String TAG = "MainActivity";

    private BluetoothAdapter mBTAdapter;
    private boolean mIsScanning = false;
    private ArrayList<ScannedDevice> mList = new ArrayList<>();

//    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
//    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);

//    private BeaconManager beaconManager;

    private ListView list;
    private MyiBeaconAdapter adapter;

    Messenger mService = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    boolean mIsBound;

    private BeaconManager beaconManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = (ListView) findViewById(R.id.list);

        adapter = new MyiBeaconAdapter(this, mList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(createOnItemClickListener());

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);

        adapter.addDeconnectedBeacons(getApplicationContext());
        startService(new Intent(MainActivity.this, BeaconService.class));
        doBindService();
    }


    void doBindService() {
        if (!mIsBound)
            mIsBound= bindService(new Intent(this, BeaconService.class), mConnection, Context.BIND_AUTO_CREATE);
    }


//    void doUnbindService() {
//        if (mIsBound) {
//            // If we have received the service, and hence registered with it, then now is the time to unregister.
//            if (mService != null) {
//                try {
//                    Message msg = Message.obtain(null, BeaconService.MSG_UNREGISTER_CLIENT);
//                    msg.replyTo = mMessenger;
//                    mService.send(msg);
//                } catch (RemoteException e) {
//                    // There is nothing special we need to do if the service has crashed.
//                }
//            }
//            // Detach our existing connection.
//            unbindService(mConnection);
//            mIsBound = false;
//            //textStatus.setText("Unbinding.");
//            Toast.makeText(this, "Unbinding", Toast.LENGTH_SHORT).show();
//        }
//    }


    private void updateList(BluetoothDevice device, int rssi, byte[] scanRecord) {

        long now = System.currentTimeMillis();

        boolean contains = false;
        for (ScannedDevice sd : mList) {
            if (device.getAddress().equals(sd.getDevice().getAddress())) {
                contains = true;
                // update
                sd.setRssi(rssi);
                sd.setLastUpdatedMs(now);
                sd.setScanRecord(scanRecord);
                break;
            }
        }
        if (!contains) {
            // add new BluetoothDevice
            ScannedDevice sd = new ScannedDevice(device, rssi, scanRecord, now);
            if (sd.getIBeacon() != null)
                mList.add(sd);
        }

        // sort by RSSI
        Collections.sort(mList, new Comparator<ScannedDevice>() {
            @Override
            public int compare(ScannedDevice lhs, ScannedDevice rhs) {
                if (lhs.getRssi() == 0) {
                    return 1;
                } else if (rhs.getRssi() == 0) {
                    return -1;
                }
                if (lhs.getRssi() > rhs.getRssi()) {
                    return -1;
                } else if (lhs.getRssi() < rhs.getRssi()) {
                    return 1;
                }
                return 0;
            }
        });


        adapter.addDeconnectedBeacons(getApplicationContext());
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onBeaconServiceConnect() {

    }


    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BeaconService.MSG_SET_LIST:
                    BluetoothDevice device = msg.getData().getParcelable(BeaconService.MSG_DEVICE);
                    int rssi = msg.getData().getInt(BeaconService.MSG_RSSI);
                    byte[] scanRecord = msg.getData().getByteArray(BeaconService.MSG_SCAN_RECORD);
                    updateList(device,rssi,scanRecord);
                    break;
                case BeaconService.MSG_ENTER_REGION:
                    //TODO
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }




    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            try {
                Message msg = Message.obtain(null, BeaconService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even do anything with it
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
            mService = null;
            //textStatus.setText("Disconnected.");
        }
    };


    private AdapterView.OnItemClickListener createOnItemClickListener() {
        return new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, BeaconSettingsActivity.class);
                intent.putExtra(BeaconSettingsActivity.KEY_IBEACON, adapter.getItem(position).getDevice().getAddress());
                //TODO ANG Choose what replace mac adress to identify ibeacons into PreferenceManager
                // Use adress from bluetooth device and change all Beacon reference from preferencemanager by scanneddevice and macadress by device adress
                startActivity(intent);
            }
        };
    }
}
