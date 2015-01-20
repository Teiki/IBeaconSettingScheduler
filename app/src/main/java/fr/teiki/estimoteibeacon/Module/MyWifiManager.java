package fr.teiki.estimoteibeacon.Module;

import android.content.Context;
import android.net.wifi.WifiManager;

import fr.teiki.estimoteibeacon.R;

/**
 * Created by antoinegaltier on 14/12/14.
 */
public class MyWifiManager {

    private static int old_mode = -1;

    public static void changeWifiState(Context ctx, Boolean b, int id){
        WifiManager manager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if (b) {
            if (old_mode == -1) {
                old_mode = manager.getWifiState();
                manager.setWifiEnabled(id == R.id.wifi_activate);
            }
        }
        else{
            if (old_mode != -1){
                manager.setWifiEnabled(old_mode==WifiManager.WIFI_STATE_ENABLED);
                old_mode = -1;
            }

        }
    }


}
