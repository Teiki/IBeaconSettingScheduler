package fr.teiki.ibs.Module;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by antoinegaltier on 15/12/14.
 */
public class MyAdmin extends DeviceAdminReceiver {

    public class Controller extends Activity {

        public DevicePolicyManager mDPM;
        public ComponentName mDeviceAdminSample;

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
            mDeviceAdminSample = new ComponentName(Controller.this, MyAdmin.class);
        }
    }
}

