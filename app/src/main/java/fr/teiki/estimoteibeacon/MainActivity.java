package fr.teiki.estimoteibeacon;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.utils.L;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "MainActivity";

//    private static final String ESTIMOTE_PROXIMITY_UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
//    private static final Region ALL_ESTIMOTE_BEACONS = new Region("regionId", ESTIMOTE_PROXIMITY_UUID, null, null);

//    private BeaconManager beaconManager;

    private ListView list;
    private MyiBeaconAdapter adapter;

    Messenger mService = null;
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    boolean mIsBound;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = (ListView) findViewById(R.id.list);

        L.enableDebugLogging(true);
        adapter = new MyiBeaconAdapter(this);
        list.setAdapter(adapter);
        list.setOnItemClickListener(createOnItemClickListener());


        startService(new Intent(MainActivity.this, BeaconService.class));
        doBindService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == 0 && resultCode==RESULT_OK) {
//            String tmp = data.getExtras().getString("clickedFile");
//            Bitmap ImageToChange= BitmapFactory.decodeFile(tmp);
//            process_image(ImageToChange);
//        }
    }


    private void rangeUUID(String uuid){
        Bundle b = new Bundle();
        b.putString(BeaconService.MSG_ADD_UUID_KEY, uuid);
        Message msg = Message.obtain(null, BeaconService.MSG_ADD_UUID);
        msg.setData(b);
        msg.replyTo = mMessenger;
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void CheckIfServiceIsRunning() {
        //If the service is running when the activity starts, we want to automatically bind to it.
        if (BeaconService.isRunning()) {
            doBindService();
        }
    }

    void doBindService() {
        mIsBound= bindService(new Intent(this, BeaconService.class), mConnection, Context.BIND_AUTO_CREATE);
        //= true;
        //textStatus.setText("Binding.");
    }


    void doUnbindService() {
        if (mIsBound) {
            // If we have received the service, and hence registered with it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null, BeaconService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
            //textStatus.setText("Unbinding.");
            Toast.makeText(this, "Unbinding", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
//        try {
//            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS);
//        } catch (RemoteException e) {
//            Log.e(TAG, "Cannot stop but it does not matter now", e);
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //beaconManager.disconnect();
        try {
            doUnbindService();
        } catch (Throwable t) {
            Log.e("MainActivity", "Failed to unbind from the service", t);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private void setlist(List<Beacon> beacons){
//        String[] array = new String[beacons.size()];
//        for(int i=0; i<array.length; i++){
//            array[i] = beacons.get(i).getProximityUUID();
//        }
//        ArrayAdapter<String> itemsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, array);
//        list.setAdapter(itemsAdapter);
        if (beacons != null) {
//            getActionBar().setSubtitle("Found beacons: " + beacons.size());
            adapter.replaceWith(getApplicationContext(),beacons);
        }
    }



    @SuppressLint("HandlerLeak")
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BeaconService.MSG_SET_LIST:
                    Beacon b = msg.getData().getParcelable(BeaconService.MSG_SET_LIST_KEY);
                    //ArrayList<Parcelable> list = msg.getData().getParcelableArrayList(BeaconService.MSG_SET_LIST_KEY);
                    ArrayList<Beacon> mlist = new ArrayList<Beacon>();
                    mlist.add(b);
                    setlist(mlist);
                    break;
                case BeaconService.MSG_ENTER_REGION:
                    Beacon b2 = msg.getData().getParcelable(BeaconService.MSG_ENTER_REGION_KEY);
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
                intent.putExtra(BeaconSettingsActivity.KEY_IBEACON, adapter.getItem(position));
                startActivity(intent);
            }
        };
    }
}
